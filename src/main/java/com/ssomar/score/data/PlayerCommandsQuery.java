package com.ssomar.score.data;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.ActionInfoSerializer;
import com.ssomar.score.commands.runnable.player.PlayerRunCommand;
import com.ssomar.score.utils.logging.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class PlayerCommandsQuery {

    private final static String TABLE_COMMANDS_PLAYER = "commands_player";
    private final static String TABLE_COMMANDS_PLAYER_NAME = "Commands Player";

    private final static String COL_UUID_LAUNCHER = "uuid_launcher";
    private final static String COL_UUID_RECEIVER = "uuid_receiver";
    private final static String COL_BRUT_COMMAND = "brut_command";
    private final static String COL_RUN_TIME = "run_time";
    private final static String COL_ACTION_INFO = "action_info";

    public final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COMMANDS_PLAYER + " (" + COL_UUID_LAUNCHER + " TEXT NOT NULL, " + COL_UUID_RECEIVER + " TEXT NOT NULL, " + COL_BRUT_COMMAND + " TEXT NOT NULL," + COL_RUN_TIME + " LONG NOT NULL, " + COL_ACTION_INFO + " TEXT NOT NULL);";


    public static void createNewTable(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("PlayerCommandsQuery createNewTable");
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Creating table &6" + TABLE_COMMANDS_PLAYER_NAME + " &7if not exists...");
            stmt.execute(CREATE_TABLE);
        } catch (SQLException e) {
            SCore.plugin.getLogger().severe("Error while creating table " + TABLE_COMMANDS_PLAYER_NAME + " in database "+e.getMessage());
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void insertCommand(Connection conn, List<PlayerRunCommand> commands, boolean async) {
        if (async) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    insertCommand(conn, commands);
                }
            };
            SCore.schedulerHook.runAsyncTask(runnable, 0L);

        } else insertCommand(conn, commands);
    }


    public static void insertCommand(Connection conn, List<PlayerRunCommand> commands) {
        if (Database.DEBUG) Utils.sendConsoleMsg("PlayerCommandsQuery insertCommand");

        String sql = "INSERT INTO " + TABLE_COMMANDS_PLAYER + " (" + COL_UUID_LAUNCHER + "," + COL_UUID_RECEIVER + "," + COL_BRUT_COMMAND + "," + COL_RUN_TIME + "," + COL_ACTION_INFO + ") VALUES(?,?,?,?,?)";

        PreparedStatement pstmt = null;

        int i = 0;

        try {
            pstmt = conn.prepareStatement(sql);
            for (PlayerRunCommand command : commands) {
                /* No receiver (server loop): nobody to wait for */
                if (command.getReceiverUUID() == null) continue;
                try {
                    /* No launcher when the activator has no owner (EB placed by a command...), the column is not read back */
                    pstmt.setString(1, command.getLauncherUUID() == null ? "" : command.getLauncherUUID().toString());
                    pstmt.setString(2, command.getReceiverUUID().toString());
                    pstmt.setString(3, command.getBrutCommandToSave());
                    pstmt.setLong(4, command.getRunTime());
                    pstmt.setString(5, ActionInfoSerializer.toString(command.getaInfo()));
                } catch (IOException | RuntimeException err) {
                    /* Only this command is lost, not the others */
                    SCore.plugin.getLogger().severe(SCore.NAME_COLOR_WITH_BRACKETS + " Couldn't save the delayed command: "+command.getBrutCommand()+" >>" + err);
                    continue;
                }
                pstmt.addBatch();
                i++;
                if (i % 1000 == 0) {
                    pstmt.executeBatch(); // Execute every 1000 items.
                }
            }
            if (i % 1000 != 0) pstmt.executeBatch();
        } catch (SQLException e) {
            Utils.sendConsoleMsg(SCore.NAME_COLOR_WITH_BRACKETS + " " + e.getMessage());
            e.printStackTrace();
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


    public static void deleteCommandsForPlayer(Connection conn, UUID uuid) {
        if (Database.DEBUG) Utils.sendConsoleMsg("PlayerCommandsQuery deleteCommandsForPlayer");

        String sql = "DELETE FROM " + TABLE_COMMANDS_PLAYER + " where " + COL_UUID_RECEIVER + "=?";

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

    public static void deleteCommands(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("PlayerCommandsQuery deleteCommands");

        String sql = "DELETE FROM " + TABLE_COMMANDS_PLAYER;

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

    /* public static List<PlayerRunCommand> selectCommandsForPlayer(Connection conn, UUID uuid) {
        String sql = "SELECT " + COL_BRUT_COMMAND + "," + COL_RUN_TIME + "," + COL_ACTION_INFO + " FROM " + TABLE_COMMANDS_PLAYER + " where " + COL_UUID_RECEIVER + "=? ORDER BY " + COL_RUN_TIME;

        List<PlayerRunCommand> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (!SCore.is1v11Less() && !conn.isValid(5)) {
                SCore.plugin.getLogger().severe("Connection to database is impossible, and can't load the saved commands of player " + uuid.toString());
                return list;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setQueryTimeout(5);
            pstmt.setString(1, uuid.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {

                String brutCommand = rs.getString(COL_BRUT_COMMAND);
                long runTime = rs.getLong(COL_RUN_TIME);
                ActionInfo aInfo = null;
                try {
                    aInfo = (ActionInfo) ActionInfoSerializer.fromString(rs.getString(COL_ACTION_INFO));
                } catch (Exception e) {
                    SCore.plugin.getLogger().severe("(NOT VERY SERIOUS) The delayed command " + brutCommand + " has been deleted because it was saved in an outdated version.");
                    continue;
                }

                PlayerRunCommand pCommand = new PlayerRunCommand(brutCommand, runTime, aInfo);

                list.add(pCommand);
            }
        } catch (SQLException e) {
            System.out.println(SCore.NAME_2 + " " + e.getMessage());
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
    }*/

    public static Map<UUID, List<PlayerRunCommand>> loadSavedCommands(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("PlayerCommandsQuery loadSavedCommands");
        String sql = "SELECT " + COL_BRUT_COMMAND + "," + COL_RUN_TIME + "," + COL_ACTION_INFO + "," + COL_UUID_RECEIVER + " FROM " + TABLE_COMMANDS_PLAYER + " ORDER BY " + COL_RUN_TIME;

        Map<UUID, List<PlayerRunCommand>> map = new HashMap<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            if (!SCore.is1v11Less() && !conn.isValid(5)) {
                SCore.plugin.getLogger().severe("Connection to database is impossible, and can't load the saved commands ");
                return map;
            }
            pstmt = conn.prepareStatement(sql);
            pstmt.setQueryTimeout(5);
            rs = pstmt.executeQuery();

            while (rs.next()) {

                String brutCommand = rs.getString(COL_BRUT_COMMAND);
                long runTime = rs.getLong(COL_RUN_TIME);
                UUID uuidReceiver = UUID.fromString(rs.getString(COL_UUID_RECEIVER));
                OfflinePlayer receiver = Bukkit.getOfflinePlayer(uuidReceiver);
                ActionInfo aInfo = null;
                try {
                    aInfo = (ActionInfo) ActionInfoSerializer.fromString(rs.getString(COL_ACTION_INFO));
                } catch (Exception e) {
                    e.printStackTrace();
                    SCore.plugin.getLogger().severe("(NOT VERY SERIOUS) The delayed command " + brutCommand + " for " + receiver.getName() + " has been deleted because it was saved in an outdated version.");
                    continue;
                }

                PlayerRunCommand pCommand = new PlayerRunCommand(brutCommand, runTime, aInfo);

                if (map.containsKey(uuidReceiver)) {
                    map.get(uuidReceiver).add(pCommand);
                } else map.put(uuidReceiver, new ArrayList<>(Collections.singletonList(pCommand)));
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
        return map;
    }

}
