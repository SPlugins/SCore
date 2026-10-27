package com.ssomar.score.data;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.mixed_player_entity.commands.addtempattribute.AddTemporaryAttributeObject;
import com.ssomar.score.utils.logging.Utils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.*;
import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TemporaryAttributeQuery {

    private static final String TABLE_ID = "temp_attributes";
    // column names
    /**
     * The primary key for the temp_attributes table. Also used to give a unique key to an attribute
     */
    private static final String COL_ATTRIBUTE_KEY = "attribute_key";
    /**
     * The ADD_TEMPORARY_ATTRIBUTE works for both players and entities so this column will be named "entity_uuid"
     */
    private static final String COL_ENTITY_UUID = "entity_uuid";
    /**
     * Attributes come in various attribute types
     */
    private static final String COL_ATTRIBUTE_TYPE = "attribute_type";
    /**
     * Stored in unix time/long/bigint
     */
    private static final String COL_EXPIRY_TIME = "expiry_time";
    /**
     * Attributes only accept values in double
     */
    private static final String COL_AMOUNT = "amount";
    // create table query
    private static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ID + " (" +
                    COL_ATTRIBUTE_KEY + " VARCHAR(36) NOT NULL, " +
                    COL_ATTRIBUTE_TYPE + " TEXT NOT NULL, " +
                    COL_AMOUNT + " DOUBLE NOT NULL, " +
                    COL_ENTITY_UUID + " VARCHAR(36) NOT NULL, " +
                    COL_EXPIRY_TIME + " BIGINT NOT NULL, " +
                    "PRIMARY KEY (" + COL_ATTRIBUTE_KEY + ")" +
                    ");";

    public static void createNewTable(Connection conn) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7Creating table &6ATemporaryAttributesQuery &7if not exists...");
            statement.execute(CREATE_TABLE);
        } catch (SQLException e) {
            SCore.plugin.getLogger().severe("ERROR WHILE CREATING TABLE "+TABLE_ID+" IN DATABASE: "+e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Used to insert properties of a temporary attribute to the MySQL database
     * @param conn
     * @param attribute_key
     * @param attribute_type
     * @param amount
     * @param entity_uuid
     * @param expiry_time
     */
    public static void insertToRecords(Connection conn,
                                       String attribute_key,
                                       String attribute_type,
                                       double amount,
                                       String entity_uuid,
                                       long expiry_time) {
        final String insertQuery = "INSERT INTO "+TABLE_ID+" ("
                +COL_ATTRIBUTE_KEY+","
                +COL_ATTRIBUTE_TYPE+","
                +COL_AMOUNT+","
                +COL_ENTITY_UUID+","
                +COL_EXPIRY_TIME
                +") VALUES (?, ?, ?, ?, ?);";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(insertQuery);
            stmt.setString(1, attribute_key);
            stmt.setString(2, attribute_type);
            stmt.setDouble(3, amount);
            stmt.setString(4, entity_uuid);
            stmt.setLong(5, expiry_time);
            stmt.execute();
        } catch (Exception e) {
            SCore.plugin.getLogger().warning("There was complication with the insert query for TemporaryAttributeQuery.java: "+e.getMessage());
        } finally {
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
        }

    }

    /**
     * Fetches expired temporary attributes without deleting them
     * @param conn Database.getInstance().connect()
     * @param entityUUID
     * @return ArrayList consisting of expired temporary attributes.
     */
    public static ArrayList<AddTemporaryAttributeObject> fetchExpiredTemporaryAttributes(Connection conn, String entityUUID) {
        ArrayList<AddTemporaryAttributeObject> returnArray = new ArrayList<>();

        final long currentTime = System.currentTimeMillis();
        final String selectQuery = "SELECT * FROM "+TABLE_ID+" WHERE "+COL_ENTITY_UUID+"=? AND "+COL_EXPIRY_TIME+"<?;";

        PreparedStatement stmt = null;
        ResultSet rset = null;
        try {
            stmt = conn.prepareStatement(selectQuery);
            stmt.setString(1, entityUUID);
            stmt.setLong(2, currentTime);
            rset = stmt.executeQuery();

            while (rset.next()) {
                returnArray.add(
                        new AddTemporaryAttributeObject(
                                rset.getString(1),
                                rset.getString(2),
                                rset.getDouble(3),
                                rset.getString(4),
                                rset.getLong(5)
                        )
                );
            }
        } catch (Exception e) {
            SCore.plugin.getLogger().warning("There was complication with the select query for TemporaryAttributeQuery.java: "+e.getMessage());
        } finally {
            if (rset != null) try { rset.close(); } catch (Exception ignored) {}
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
        }
        return returnArray;
    }

    /**
     * Deletes expired temporary attributes for an entity
     * @param conn Database.getInstance().connect()
     * @param entityUUID
     */
    public static void deleteExpiredTemporaryAttributes(Connection conn, String entityUUID) {
        final long currentTime = System.currentTimeMillis();
        final String deleteQuery = "DELETE FROM "+TABLE_ID+" WHERE "+COL_ENTITY_UUID+"=? AND "+COL_EXPIRY_TIME+"<?;";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(deleteQuery);
            stmt.setString(1, entityUUID);
            stmt.setLong(2, currentTime);
            stmt.executeUpdate();
        } catch (Exception e) {
            SCore.plugin.getLogger().warning("There was complication with the delete query for TemporaryAttributeQuery.java: "+e.getMessage());
        } finally {
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Used to simply perform a DELETE query to delete an attribute key from the database via the attribute's key value
     * @param conn
     * @param key_attr
     */
    public static void removeFromRecords(Connection conn, String key_attr) {
        final String deleteQuery = "DELETE FROM "+TABLE_ID+" WHERE "+COL_ATTRIBUTE_KEY+"=?;";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(deleteQuery);
            stmt.setString(1, key_attr);
            stmt.executeUpdate();
        } catch (Exception e) {
            SCore.plugin.getLogger().warning("There was complication with the delete operation for TemporaryAttributeQuery.java: "+e.getMessage());
        } finally {
            if (stmt != null) try { stmt.close(); } catch (Exception ignored) {}
        }
    }

}
