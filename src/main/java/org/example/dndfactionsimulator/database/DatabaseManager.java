package org.example.dndfactionsimulator.database;

import org.example.dndfactionsimulator.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:factions.db";
    private Connection connection;

    private static DatabaseManager instance;

    private DatabaseManager() {
        try {
            connection = DriverManager.getConnection(DATABASE_URL);
            createTables();
            System.out.println("✅ Database connected: factions.db");
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void createTables() {
        // Factions table
        String createFactionsTable =
                "CREATE TABLE IF NOT EXISTS factions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "type TEXT NOT NULL," +
                        "alignment TEXT NOT NULL," +
                        "gold INTEGER DEFAULT 100," +
                        "troops INTEGER DEFAULT 50," +
                        "magic INTEGER DEFAULT 10," +
                        "influence INTEGER DEFAULT 20," +
                        "is_active INTEGER DEFAULT 1" +
                        ")";

        // Relationships table
        String createRelationshipsTable =
                "CREATE TABLE IF NOT EXISTS relationships (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "faction1_id INTEGER NOT NULL," +
                        "faction2_id INTEGER NOT NULL," +
                        "type TEXT NOT NULL," +
                        "strength INTEGER DEFAULT 0," +
                        "FOREIGN KEY(faction1_id) REFERENCES factions(id)," +
                        "FOREIGN KEY(faction2_id) REFERENCES factions(id)" +
                        ")";

        // World events table
        String createEventsTable =
                "CREATE TABLE IF NOT EXISTS world_events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "turn_number INTEGER NOT NULL," +
                        "faction_id INTEGER NOT NULL," +
                        "action TEXT NOT NULL," +
                        "description TEXT NOT NULL," +
                        "timestamp TEXT NOT NULL," +
                        "target_faction_id INTEGER," +
                        "FOREIGN KEY(faction_id) REFERENCES factions(id)," +
                        "FOREIGN KEY(target_faction_id) REFERENCES factions(id)" +
                        ")";

        // Game state table (tracks current turn)
        String createGameStateTable =
                "CREATE TABLE IF NOT EXISTS game_state (" +
                        "id INTEGER PRIMARY KEY," +
                        "current_turn INTEGER DEFAULT 0" +
                        ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createFactionsTable);
            stmt.execute(createRelationshipsTable);
            stmt.execute(createEventsTable);
            stmt.execute(createGameStateTable);

            // Initialize game state if empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM game_state");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO game_state (id, current_turn) VALUES (1, 0)");
            }

            System.out.println("✅ Database tables ready");
        } catch (SQLException e) {
            System.err.println("❌ Error creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============== FACTION METHODS ==============

    public boolean addFaction(Faction faction) {
        String sql = "INSERT INTO factions (name, type, alignment, gold, troops, magic, influence, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, faction.getName());
            pstmt.setString(2, faction.getType().name());
            pstmt.setString(3, faction.getAlignment().name());
            pstmt.setInt(4, faction.getGold());
            pstmt.setInt(5, faction.getTroops());
            pstmt.setInt(6, faction.getMagic());
            pstmt.setInt(7, faction.getInfluence());
            pstmt.setInt(8, faction.isActive() ? 1 : 0);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Get the last inserted ID using SQLite's last_insert_rowid()
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    faction.setId(rs.getInt(1));
                }
                System.out.println("✅ Faction added: " + faction.getName());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding faction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Faction> getAllFactions() {
        List<Faction> factions = new ArrayList<>();
        String sql = "SELECT * FROM factions";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Faction faction = new Faction();
                faction.setId(rs.getInt("id"));
                faction.setName(rs.getString("name"));
                faction.setType(FactionType.valueOf(rs.getString("type")));
                faction.setAlignment(Alignment.valueOf(rs.getString("alignment")));
                faction.setGold(rs.getInt("gold"));
                faction.setTroops(rs.getInt("troops"));
                faction.setMagic(rs.getInt("magic"));
                faction.setInfluence(rs.getInt("influence"));
                faction.setActive(rs.getInt("is_active") == 1);

                factions.add(faction);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching factions: " + e.getMessage());
            e.printStackTrace();
        }

        return factions;
    }

    public List<Faction> getActiveFactions() {
        List<Faction> factions = new ArrayList<>();
        String sql = "SELECT * FROM factions WHERE is_active = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Faction faction = new Faction();
                faction.setId(rs.getInt("id"));
                faction.setName(rs.getString("name"));
                faction.setType(FactionType.valueOf(rs.getString("type")));
                faction.setAlignment(Alignment.valueOf(rs.getString("alignment")));
                faction.setGold(rs.getInt("gold"));
                faction.setTroops(rs.getInt("troops"));
                faction.setMagic(rs.getInt("magic"));
                faction.setInfluence(rs.getInt("influence"));
                faction.setActive(rs.getInt("is_active") == 1);

                factions.add(faction);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching active factions: " + e.getMessage());
            e.printStackTrace();
        }

        return factions;
    }

    public boolean updateFaction(Faction faction) {
        String sql = "UPDATE factions SET name = ?, type = ?, alignment = ?, gold = ?, troops = ?, " +
                "magic = ?, influence = ?, is_active = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, faction.getName());
            pstmt.setString(2, faction.getType().name());
            pstmt.setString(3, faction.getAlignment().name());
            pstmt.setInt(4, faction.getGold());
            pstmt.setInt(5, faction.getTroops());
            pstmt.setInt(6, faction.getMagic());
            pstmt.setInt(7, faction.getInfluence());
            pstmt.setInt(8, faction.isActive() ? 1 : 0);
            pstmt.setInt(9, faction.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error updating faction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFaction(int factionId) {
        String sql = "DELETE FROM factions WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, factionId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Faction permanently deleted: ID " + factionId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting faction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ============== RELATIONSHIP METHODS ==============

    public boolean addRelationship(Relationship relationship) {
        String sql = "INSERT INTO relationships (faction1_id, faction2_id, type, strength) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, relationship.getFaction1Id());
            pstmt.setInt(2, relationship.getFaction2Id());
            pstmt.setString(3, relationship.getType().name());
            pstmt.setInt(4, relationship.getStrength());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    relationship.setId(rs.getInt(1));
                }
                System.out.println("✅ Relationship added");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding relationship: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Relationship> getAllRelationships() {
        List<Relationship> relationships = new ArrayList<>();
        String sql = "SELECT * FROM relationships";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Relationship relationship = new Relationship();
                relationship.setId(rs.getInt("id"));
                relationship.setFaction1Id(rs.getInt("faction1_id"));
                relationship.setFaction2Id(rs.getInt("faction2_id"));
                relationship.setType(RelationshipType.valueOf(rs.getString("type")));
                relationship.setStrength(rs.getInt("strength"));
                relationships.add(relationship);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching relationships: " + e.getMessage());
            e.printStackTrace();
        }

        return relationships;
    }

    public boolean updateRelationship(Relationship relationship) {
        String sql = "UPDATE relationships SET type = ?, strength = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, relationship.getType().name());
            pstmt.setInt(2, relationship.getStrength());
            pstmt.setInt(3, relationship.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error updating relationship: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ============== WORLD EVENT METHODS ==============

    public boolean addWorldEvent(WorldEvent event) {
        String sql = "INSERT INTO world_events (turn_number, faction_id, action, description, timestamp, target_faction_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, event.getTurnNumber());
            pstmt.setInt(2, event.getFactionId());
            pstmt.setString(3, event.getAction().name());
            pstmt.setString(4, event.getDescription());
            pstmt.setString(5, event.getTimestamp().toString());

            if (event.getTargetFactionId() != null) {
                pstmt.setInt(6, event.getTargetFactionId());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    event.setId(rs.getInt(1));
                }
                System.out.println("✅ Event logged: " + event.getDescription());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding event: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<WorldEvent> getAllEvents() {
        List<WorldEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM world_events ORDER BY turn_number DESC, timestamp DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                WorldEvent event = new WorldEvent();
                event.setId(rs.getInt("id"));
                event.setTurnNumber(rs.getInt("turn_number"));
                event.setFactionId(rs.getInt("faction_id"));
                event.setAction(FactionAction.valueOf(rs.getString("action")));
                event.setDescription(rs.getString("description"));
                event.setTimestamp(java.time.LocalDateTime.parse(rs.getString("timestamp")));

                int targetId = rs.getInt("target_faction_id");
                if (!rs.wasNull()) {
                    event.setTargetFactionId(targetId);
                }

                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    public List<WorldEvent> getEventsByTurn(int turnNumber) {
        List<WorldEvent> events = new ArrayList<>();
        String sql = "SELECT * FROM world_events WHERE turn_number = ? ORDER BY timestamp";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, turnNumber);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                WorldEvent event = new WorldEvent();
                event.setId(rs.getInt("id"));
                event.setTurnNumber(rs.getInt("turn_number"));
                event.setFactionId(rs.getInt("faction_id"));
                event.setAction(FactionAction.valueOf(rs.getString("action")));
                event.setDescription(rs.getString("description"));
                event.setTimestamp(java.time.LocalDateTime.parse(rs.getString("timestamp")));

                int targetId = rs.getInt("target_faction_id");
                if (!rs.wasNull()) {
                    event.setTargetFactionId(targetId);
                }

                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching events by turn: " + e.getMessage());
            e.printStackTrace();
        }

        return events;
    }

    // ============== GAME STATE METHODS ==============

    public int getCurrentTurn() {
        String sql = "SELECT current_turn FROM game_state WHERE id = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("current_turn");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting current turn: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public boolean advanceTurn() {
        String sql = "UPDATE game_state SET current_turn = current_turn + 1 WHERE id = 1";

        try (Statement stmt = connection.createStatement()) {
            int affected = stmt.executeUpdate(sql);
            if (affected > 0) {
                int newTurn = getCurrentTurn();
                System.out.println("✅ Turn advanced to: " + newTurn);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error advancing turn: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // ============== CLOSE ==============

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error closing database: " + e.getMessage());
        }
    }
}