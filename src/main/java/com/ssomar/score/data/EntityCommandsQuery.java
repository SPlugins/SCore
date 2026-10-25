package com.ssomar.score.data;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.ActionInfoSerializer;
import com.ssomar.score.commands.runnable.entity.EntityRunCommand;
import com.ssomar.score.utils.logging.Utils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntityCommandsQuery {

    private final static String TABLE_COMMANDS_ENTITY = "commands_entity";
    private final static String TABLE_COMMANDS_ENTITY_NAME = "Commands Entity";

    private final static String COL_UUID_LAUNCHER = "uuid_launcher";
    private final static String COL_UUID_ENTITY = "uuid_entity";
    private final static String COL_BRUT_COMMAND = "brut_command";
    private final static String COL_RUN_TIME = "run_time";
    private final static String COL_ACTION_INFO = "action_info";

    public final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COMMANDS_ENTITY + " (" + COL_UUID_LAUNCHER + " TEXT NOT NULL, " + COL_UUID_ENTITY + " TEXT NOT NULL, " + COL_BRUT_COMMAND + " TEXT NOT NULL," + COL_RUN_TIME + " LONG NOT NULL, " + COL_ACTION_INFO + " TEXT NOT NULL);";


    public static void createNewTable(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("EntityCommandsQuery createNewTable");
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Creating table &6" + TABLE_COMMANDS_ENTITY_NAME + " &7if not exists...");
            stmt.execute(CREATE_TABLE);
        } catch (SQLException e) {
            SCore.plugin.getLogger().severe("Error while creating table " + TABLE_COMMANDS_ENTITY_NAME + " in database "+e.getMessage());
        }finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void insertCommand(Connection conn, List<EntityRunCommand> commands) {
        if (Database.DEBUG) Utils.sendConsoleMsg("EntityCommandsQuery insertCommand");
        String sql = "INSERT INTO " + TABLE_COMMANDS_ENTITY + " (" + COL_UUID_LAUNCHER + "," + COL_UUID_ENTITY + "," + COL_BRUT_COMMAND + "," + COL_RUN_TIME + "," + COL_ACTION_INFO + ") VALUES(?,?,?,?,?)";

        PreparedStatement pstmt = null;

        int i = 0;

        try {
            pstmt = conn.prepareStatement(sql);
            for (EntityRunCommand command : commands) {
                try {
                    /* No launcher when nobody triggered the activator, the column is not read back */
                    pstmt.setString(1, command.getLauncherUUID() == null ? "" : command.getLauncherUUID().toString());
                    pstmt.setString(2, command.getEntityUUID().toString());
                    pstmt.setString(3, command.getBrutCommandToSave());
                    pstmt.setLong(4, command.getRunTime());
                    pstmt.setString(5, ActionInfoSerializer.toString(command.getaInfo()));
                } catch (IOException | RuntimeException err) {
                    /* Only this command is lost, not the others */
                    SCore.plugin.getLogger().severe(SCore.NAME_COLOR_WITH_BRACKETS + " Couldn't save the delayed command: " + command.getBrutCommand() + " >>" + err);
                    continue;
                }
                pstmt.addBatch();
                i++;
            }
            if (i > 0) pstmt.executeBatch();
        } catch (SQLException e) {
            System.out.println(SCore.NAME_COLOR_WITH_BRACKETS + " " + e.getMessage());
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


    public static void deleteEntityCommands(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("EntityCommandsQuery deleteEntityCommands");

        String sql = "DELETE FROM " + TABLE_COMMANDS_ENTITY;

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

    public static List<EntityRunCommand> selectEntityCommands(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("EntityCommandsQuery selectEntityCommands");
        String sql = "SELECT " + COL_BRUT_COMMAND + "," + COL_RUN_TIME + "," + COL_ACTION_INFO + " FROM " + TABLE_COMMANDS_ENTITY + " ORDER BY " + COL_RUN_TIME;

        List<EntityRunCommand> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {

                String brutCommand = rs.getString(COL_BRUT_COMMAND);
                long runTime = rs.getLong(COL_RUN_TIME);
                ActionInfo aInfo;
                try {
                    aInfo = (ActionInfo) ActionInfoSerializer.fromString(rs.getString(COL_ACTION_INFO));
                } catch (Exception e) {
                    SCore.plugin.getLogger().severe("(NOT VERY SERIOUS) The delayed command " + brutCommand + " has been deleted because it was saved in an outdated version.");
                    continue;
                }

                EntityRunCommand pCommand = new EntityRunCommand(brutCommand, runTime, aInfo);

                list.add(pCommand);
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

}
