package com.ssomar.score.data;

import com.ssomar.score.SCore;
import com.ssomar.score.features.custom.conditions.placeholders.group.PlaceholderConditionGroupFeature;
import com.ssomar.score.features.custom.cooldowns.Cooldown;
import com.ssomar.score.utils.logging.Utils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CooldownsQuery {

    private final static String TABLE_COOLDOWNS = "cooldowns";
    private final static String TABLE_COOLDOWNS_NAME = "Cooldowns";

    private final static String COL_ID = "id";
    private final static String COL_UUID = "UUID";
    private final static String COL_COOLDOWN = "cooldown";
    private final static String COL_IS_IN_TICK = "isInTick";
    private final static String COL_IS_GLOBAL = "isGlobal";
    private final static String COL_TIME = "time";
    private final static String COL_PAUSED = "paused";
    private final static String COL_PAUSE_OFFLINE = "pauseOffline";
    private final static String COL_PAUSE_PLACEHOLDERS_CONDITIONS = "pausePlaceholdersConditions";

    public final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COOLDOWNS + " (" + COL_ID + " TEXT NOT NULL, "
            + COL_UUID + " TEXT NOT NULL, "
            + COL_COOLDOWN + " INTEGER NOT NULL, "
            + COL_IS_IN_TICK + " BOOLEAN NOT NULL, "
            + COL_IS_GLOBAL + " BOOLEAN NOT NULL, "
            + COL_TIME + " LONG NOT NULL, "
            + COL_PAUSED + " BOOLEAN NOT NULL,"
            + COL_PAUSE_OFFLINE + " BOOLEAN NOT NULL,"
            + COL_PAUSE_PLACEHOLDERS_CONDITIONS + " TEXT NOT NULL);";

    /* Fix a typo 25/02/2025 */
    public final static String RENAME_TABLE_IF_EXIST = "ALTER TABLE cooldows RENAME TO "+TABLE_COOLDOWNS+";";

    public final static String CHECK_BEFORE_UPDATE_4_24_1_4 = "SELECT `COLUMN_NAME` " +
            "FROM `INFORMATION_SCHEMA`.`COLUMNS`" +
            "WHERE `TABLE_SCHEMA`= DATABASE()" +
            "AND `TABLE_NAME`='"+TABLE_COOLDOWNS+"'" +
            "AND `COLUMN_NAME`='"+COL_PAUSE_OFFLINE+"';";

    /* INFORMATION_SCHEMA doesnt exist in SQLITE */
    public final static String CHECK_BEFORE_UPDATE_4_24_1_4_SQLITE = "SELECT name " +
            "FROM pragma_table_info('"+TABLE_COOLDOWNS+"')" +
            "WHERE name='"+COL_PAUSE_OFFLINE+"';";


    public static void createNewTable(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("CooldownsQuery createNewTable");
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Creating table &6" + TABLE_COOLDOWNS_NAME + "&7 if not exists...");

            // Fix a typo 25/02/2025
            try {
                stmt.execute(RENAME_TABLE_IF_EXIST);
            }catch (SQLException ignored) {}

            stmt.execute(CREATE_TABLE);

            /* Detect whether the pauseOffline column is already present. Old SQLite drivers
             * (e.g. the one bundled with legacy servers such as 1.8.x) support neither the
             * pragma_table_info() table-valued function (SQLite 3.16+) nor TRUE/FALSE literals
             * (3.23+). The classic "PRAGMA table_info(...)" statement works on every version,
             * so iterate it for SQLite; keep INFORMATION_SCHEMA for MySQL. */
            boolean columnUpToDate;
            if (Database.useMySQL) {
                PreparedStatement pstmt = conn.prepareStatement(CHECK_BEFORE_UPDATE_4_24_1_4);
                ResultSet rs = pstmt.executeQuery();
                columnUpToDate = rs.next();
                rs.close();
                pstmt.close();
            } else {
                columnUpToDate = false;
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + TABLE_COOLDOWNS + ");");
                while (rs.next()) {
                    if (COL_PAUSE_OFFLINE.equalsIgnoreCase(rs.getString("name"))) {
                        columnUpToDate = true;
                        break;
                    }
                }
                rs.close();
            }

            if(!columnUpToDate) {
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Table &6" + TABLE_COOLDOWNS_NAME + " &7exists, but it's not up to date, updating...");
                stmt.execute("ALTER TABLE "+TABLE_COOLDOWNS+" DROP COLUMN loaded;");
                stmt.execute("ALTER TABLE "+TABLE_COOLDOWNS+" ADD COLUMN "+COL_PAUSED+" BOOLEAN NOT NULL DEFAULT 0;");
            	stmt.execute("ALTER TABLE "+TABLE_COOLDOWNS+" ADD COLUMN "+COL_PAUSE_OFFLINE+" BOOLEAN NOT NULL DEFAULT 0;");
            	stmt.execute("ALTER TABLE "+TABLE_COOLDOWNS+" ADD COLUMN "+COL_PAUSE_PLACEHOLDERS_CONDITIONS+" TEXT NOT NULL DEFAULT '';");
            }

        } catch (SQLException e) {
            SCore.plugin.getLogger().severe("Error while creating table " + TABLE_COOLDOWNS_NAME + " in database "+e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void insertCooldowns(Connection conn, List<Cooldown> cooldowns) {
        if (Database.DEBUG) Utils.sendConsoleMsg("CooldownsQuery insertCooldowns");
        String sql = "INSERT INTO " + TABLE_COOLDOWNS + " (" + COL_ID + "," + COL_UUID + "," + COL_COOLDOWN + "," + COL_IS_IN_TICK + "," + COL_IS_GLOBAL + "," + COL_TIME + "," + COL_PAUSED+ "," + COL_PAUSE_OFFLINE + "," + COL_PAUSE_PLACEHOLDERS_CONDITIONS + ") VALUES(?,?,?,?,?,?,?,?,?)";

        PreparedStatement pstmt = null;
        int i = 0;

        try {
            pstmt = conn.prepareStatement(sql);
            for (Cooldown cd : cooldowns) {
                i++;
                if (cd != null) {
                    pstmt.setString(1, cd.getId());
                    pstmt.setString(2, cd.getEntityUUID() + "");
                    pstmt.setInt(3, cd.getCooldown());
                    pstmt.setBoolean(4, cd.isInTick());
                    pstmt.setBoolean(5, cd.isGlobal());
                    pstmt.setLong(6, cd.getTime());
                    pstmt.setBoolean(7, cd.isPaused());
                    pstmt.setBoolean(8, cd.isPauseWhenOffline());
                    pstmt.setString(9, cd.getPausePlaceholdersConditions().getConfigAsString());
                    pstmt.addBatch();
                }

                if (i % 1000 == 0 || i == cooldowns.size()) {
                    pstmt.executeBatch(); // Execute every 1000 items.
                }
            }
        } catch (SQLException e) {
            System.out.println(SCore.NAME_COLOR_WITH_BRACKETS + " " + e.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<Cooldown> getCooldownsOf(Connection conn, UUID uuid) {
        if (Database.DEBUG) Utils.sendConsoleMsg("CooldownsQuery getCooldownsOf");
        String sql = "SELECT " + COL_ID + "," + COL_UUID + "," + COL_COOLDOWN + "," + COL_IS_IN_TICK + "," + COL_IS_GLOBAL + "," + COL_TIME + "," + COL_PAUSED + "," + COL_PAUSE_OFFLINE + "," + COL_PAUSE_PLACEHOLDERS_CONDITIONS + " FROM " + TABLE_COOLDOWNS + " where " + COL_UUID + "=?";

        List<Cooldown> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uuid.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {

                String id = rs.getString(COL_ID);
                String uuidStr = rs.getString(COL_UUID);
                int cd = rs.getInt(COL_COOLDOWN);
                boolean isInTick = rs.getBoolean(COL_IS_IN_TICK);
                boolean isGlobal = rs.getBoolean(COL_IS_GLOBAL);
                long time = rs.getLong(COL_TIME);
                boolean paused = rs.getBoolean(COL_PAUSED);
                boolean pauseOffline = rs.getBoolean(COL_PAUSE_OFFLINE);
                String pausePlaceholdersConditions = rs.getString(COL_PAUSE_PLACEHOLDERS_CONDITIONS);
                PlaceholderConditionGroupFeature placeholderConditionGroupFeature = new PlaceholderConditionGroupFeature(null);
                Reader reader = new java.io.StringReader(pausePlaceholdersConditions);
                YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(reader);
                placeholderConditionGroupFeature.load(SCore.plugin, yamlConfiguration, true);

                Cooldown cooldown = new Cooldown(id, UUID.fromString(uuidStr), cd, isInTick, time, isGlobal);
                cooldown.setPauseFeatures(pauseOffline, placeholderConditionGroupFeature);
                cooldown.setPaused(paused);

                list.add(cooldown);
            }
        } catch (SQLException e) {
            System.out.println(SCore.NAME_COLOR_WITH_BRACKETS + " " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public static List<Cooldown> getGlobalCooldowns(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("CooldownsQuery getGlobalCooldowns");
        String sql = "SELECT " + COL_ID + "," + COL_UUID + "," + COL_COOLDOWN + "," + COL_IS_IN_TICK + "," + COL_IS_GLOBAL + "," + COL_TIME + " FROM " + TABLE_COOLDOWNS + " where " + COL_IS_GLOBAL + "=1";

        List<Cooldown> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {

                String id = rs.getString(COL_ID);
                int cd = rs.getInt(COL_COOLDOWN);
                boolean isInTick = rs.getBoolean(COL_IS_IN_TICK);
                boolean isGlobal = rs.getBoolean(COL_IS_GLOBAL);
                long time = rs.getLong(COL_TIME);

                Cooldown cooldown = new Cooldown(id, null, cd, isInTick, time, isGlobal);

                list.add(cooldown);
            }
        } catch (SQLException e) {
            System.out.println(SCore.NAME_COLOR_WITH_BRACKETS + " " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public static void deleteCooldownsOf(Connection conn, UUID uuid) {
        if (Database.DEBUG) Utils.sendConsoleMsg("CooldownsQuery deleteCooldownsOf");

        String sql = "DELETE FROM " + TABLE_COOLDOWNS + " where " + COL_UUID + "=?";

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(SCore.NAME_COLOR_WITH_BRACKETS + " " + e.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void deleteGlobalCooldowns(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("CooldownsQuery deleteGlobalCooldowns");

        String sql = "DELETE FROM " + TABLE_COOLDOWNS + " where " + COL_IS_GLOBAL + "=1";

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(SCore.NAME_COLOR_WITH_BRACKETS + " " + e.getMessage());
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
