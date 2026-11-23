package com.ssomar.score.data;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.ActionInfoSerializer;
import com.ssomar.score.commands.runnable.block.BlockRunCommand;
import com.ssomar.score.utils.logging.Utils;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlockCommandsQuery {

    private final static String TABLE_COMMANDS_BLOCK = "commands_block";
    private final static String TABLE_COMMANDS_BLOCK_NAME = "Commands Block";

    private final static String COL_UUID_LAUNCHER = "uuid_launcher";
    private final static String COL_BLOCK_X = "block_x";
    private final static String COL_BLOCK_Y = "block_y";
    private final static String COL_BLOCK_Z = "block_z";
    private final static String COL_BLOCK_WORLD = "block_world";
    private final static String COL_BRUT_COMMAND = "brut_command";
    private final static String COL_RUN_TIME = "run_time";
    private final static String COL_ACTION_INFO = "action_info";

    public final static String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COMMANDS_BLOCK + " (" + COL_UUID_LAUNCHER + " TEXT NOT NULL, " + COL_BLOCK_X + " INTEGER NOT NULL, " + COL_BLOCK_Y + " INTEGER NOT NULL, " + COL_BLOCK_Z + " INTEGER NOT NULL, " + COL_BLOCK_WORLD + " TEXT NOT NULL, " + COL_BRUT_COMMAND + " TEXT NOT NULL," + COL_RUN_TIME + " LONG NOT NULL, " + COL_ACTION_INFO + " TEXT NOT NULL);";


    public static void createNewTable(Connection conn) {
        if(Database.DEBUG) Utils.sendConsoleMsg("BlockCommandsQuery createNewTable");
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Creating table &6" + TABLE_COMMANDS_BLOCK_NAME + " &7if not exists...");
            stmt.execute(CREATE_TABLE);
        } catch (SQLException e) {
            SCore.plugin.getLogger().severe("Error while creating table " + TABLE_COMMANDS_BLOCK_NAME + " in database "+e.getMessage());
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


    public static void insertCommand(Connection conn, List<BlockRunCommand> commands) {
        if (Database.DEBUG) Utils.sendConsoleMsg("BlockCommandsQuery insertCommand");
        String sql = "INSERT INTO " + TABLE_COMMANDS_BLOCK + " (" + COL_UUID_LAUNCHER + "," + COL_BLOCK_X + "," + COL_BLOCK_Y + "," + COL_BLOCK_Z + "," + COL_BLOCK_WORLD + "," + COL_BRUT_COMMAND + "," + COL_RUN_TIME + "," + COL_ACTION_INFO + ") VALUES(?,?,?,?,?,?,?,?)";

        PreparedStatement pstmt = null;

        int i = 0;

        try {
            pstmt = conn.prepareStatement(sql);
            for (BlockRunCommand command : commands) {
                try {
                    /* No launcher when the EB has no owner (placed by a command...), the column is not read back */
                    pstmt.setString(1, command.getLauncherUUID() == null ? "" : command.getLauncherUUID().toString());
                    pstmt.setInt(2, command.getBlockX());
                    pstmt.setInt(3, command.getBlockY());
                    pstmt.setInt(4, command.getBlockZ());
                    pstmt.setString(5, command.getBlockWorld().toString());
                    pstmt.setString(6, command.getBrutCommandToSave());
                    pstmt.setLong(7, command.getRunTime());
                    pstmt.setString(8, ActionInfoSerializer.toString(command.getaInfo()));
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


    public static void deleteCommands(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("BlockCommandsQuery deleteCommands");
        String sql = "DELETE FROM " + TABLE_COMMANDS_BLOCK;

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

    public static List<BlockRunCommand> selectAllCommands(Connection conn) {
        if (Database.DEBUG) Utils.sendConsoleMsg("BlockCommandsQuery selectAllCommands");
        String sql = "SELECT " + COL_BRUT_COMMAND + "," + COL_RUN_TIME + "," + COL_ACTION_INFO + " FROM " + TABLE_COMMANDS_BLOCK + " ORDER BY " + COL_RUN_TIME;

        List<BlockRunCommand> list = new ArrayList<>();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
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

                BlockRunCommand pCommand = new BlockRunCommand(brutCommand, runTime, aInfo);

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
