package com.vexelcore.prison.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vexelcore.prison.core.VexelPrisonPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private static final Type MAP_INT = new TypeToken<Map<String, Integer>>() {}.getType();
    private static final Type MAP_LONG = new TypeToken<Map<String, Long>>() {}.getType();
    private static final Type MAP_BOOL = new TypeToken<Map<String, Boolean>>() {}.getType();

    private final VexelPrisonPlugin plugin;
    private final Gson gson = new Gson();
    private HikariDataSource source;
    private DatabaseType databaseType = DatabaseType.SQLITE;

    public DatabaseManager(VexelPrisonPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        DatabaseType type = DatabaseType.valueOf(plugin.getConfig().getString("database.type", "SQLITE").toUpperCase());
        this.databaseType = type;
        HikariConfig cfg = new HikariConfig();
        if (type == DatabaseType.SQLITE) {
            File dbFile = new File(plugin.getDataFolder(), "data.db");
            cfg.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            cfg.setMaximumPoolSize(1);
        } else {
            String host = plugin.getConfig().getString("database.mysql.host", "127.0.0.1");
            int port = plugin.getConfig().getInt("database.mysql.port", 3306);
            String db = plugin.getConfig().getString("database.mysql.database", "vexelprison");
            cfg.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false");
            cfg.setUsername(plugin.getConfig().getString("database.mysql.user", "root"));
            cfg.setPassword(plugin.getConfig().getString("database.mysql.password", ""));
            cfg.setMaximumPoolSize(10);
        }
        cfg.setPoolName("VexelPrisonPool");
        source = new HikariDataSource(cfg);
        createTables();
        try (Connection c = source.getConnection()) {
            new MigrationService().migrate(c);
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[VexelPrison] Migration check failed: " + e.getMessage());
        }
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS prison_players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "pickaxe_xp INT NOT NULL," +
                "pickaxe_level INT NOT NULL," +
                "rebirth INT NOT NULL," +
                "prestige INT NOT NULL," +
                "playtime BIGINT NOT NULL," +
                "upgrades TEXT NOT NULL," +
                "keys_json TEXT NOT NULL," +
                "boosters_json TEXT NOT NULL," +
                "blocks_json TEXT NOT NULL," +
                "claims_json TEXT NOT NULL" +
                ")";
        try (Connection c = source.getConnection(); Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[VexelPrison] DB table creation failed: " + e.getMessage());
        }
    }

    public CompletableFuture<PlayerData> load(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = source.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM prison_players WHERE uuid=?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return new PlayerData(uuid);
                    PlayerData data = new PlayerData(uuid);
                    data.setPickaxeXp(rs.getInt("pickaxe_xp"));
                    data.setPickaxeLevel(rs.getInt("pickaxe_level"));
                    data.setRebirth(rs.getInt("rebirth"));
                    data.setPrestige(rs.getInt("prestige"));
                    data.setPlaytimeSeconds(rs.getLong("playtime"));
                    data.getUpgrades().putAll(readMapInt(rs.getString("upgrades")));
                    data.getVirtualKeys().putAll(readMapInt(rs.getString("keys_json")));
                    data.getBoosters().putAll(readMapLong(rs.getString("boosters_json")));
                    data.getBlockStats().putAll(readMapInt(rs.getString("blocks_json")));
                    data.getClaims().putAll(readMapBool(rs.getString("claims_json")));
                    return data;
                }
            } catch (SQLException e) {
                Bukkit.getLogger().severe("[VexelPrison] Failed to load player data: " + e.getMessage());
                return new PlayerData(uuid);
            }
        });
    }

    public CompletableFuture<Void> save(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String sql = databaseType == DatabaseType.MYSQL
                    ? "INSERT INTO prison_players (uuid,pickaxe_xp,pickaxe_level,rebirth,prestige,playtime,upgrades,keys_json,boosters_json,blocks_json,claims_json) VALUES (?,?,?,?,?,?,?,?,?,?,?) " +
                      "ON DUPLICATE KEY UPDATE pickaxe_xp=VALUES(pickaxe_xp),pickaxe_level=VALUES(pickaxe_level),rebirth=VALUES(rebirth),prestige=VALUES(prestige),playtime=VALUES(playtime),upgrades=VALUES(upgrades),keys_json=VALUES(keys_json),boosters_json=VALUES(boosters_json),blocks_json=VALUES(blocks_json),claims_json=VALUES(claims_json)"
                    : "INSERT INTO prison_players (uuid,pickaxe_xp,pickaxe_level,rebirth,prestige,playtime,upgrades,keys_json,boosters_json,blocks_json,claims_json) VALUES (?,?,?,?,?,?,?,?,?,?,?) " +
                      "ON CONFLICT(uuid) DO UPDATE SET pickaxe_xp=excluded.pickaxe_xp,pickaxe_level=excluded.pickaxe_level,rebirth=excluded.rebirth,prestige=excluded.prestige,playtime=excluded.playtime,upgrades=excluded.upgrades,keys_json=excluded.keys_json,boosters_json=excluded.boosters_json,blocks_json=excluded.blocks_json,claims_json=excluded.claims_json";
            try (Connection c = source.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, data.getUuid().toString());
                ps.setInt(2, data.getPickaxeXp());
                ps.setInt(3, data.getPickaxeLevel());
                ps.setInt(4, data.getRebirth());
                ps.setInt(5, data.getPrestige());
                ps.setLong(6, data.getPlaytimeSeconds());
                ps.setString(7, gson.toJson(data.getUpgrades()));
                ps.setString(8, gson.toJson(data.getVirtualKeys()));
                ps.setString(9, gson.toJson(data.getBoosters()));
                ps.setString(10, gson.toJson(data.getBlockStats()));
                ps.setString(11, gson.toJson(data.getClaims()));
                ps.executeUpdate();
            } catch (SQLException e) {
                Bukkit.getLogger().severe("[VexelPrison] Failed to save player data: " + e.getMessage());
            }
        });
    }

    public void close() { if (source != null) source.close(); }

    public boolean isConnected() {
        try (Connection ignored = source.getConnection()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Integer> readMapInt(String raw) {
        return raw == null || raw.isBlank() ? new HashMap<>() : gson.fromJson(raw, MAP_INT);
    }
    private Map<String, Long> readMapLong(String raw) {
        return raw == null || raw.isBlank() ? new HashMap<>() : gson.fromJson(raw, MAP_LONG);
    }
    private Map<String, Boolean> readMapBool(String raw) {
        return raw == null || raw.isBlank() ? new HashMap<>() : gson.fromJson(raw, MAP_BOOL);
    }
}
