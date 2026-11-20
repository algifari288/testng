package com.hiyura.bots.bot;

import com.hiyura.bots.HiyuraBotsPlugin;
import com.hiyura.bots.citizens.NpcService;
import com.hiyura.bots.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public final class BotManager {

    private final HiyuraBotsPlugin plugin;
    private final Database db;
    private final NpcService npcs;
    private final Map<Integer, Bot> bots = new LinkedHashMap<>();

    public BotManager(HiyuraBotsPlugin plugin, Database db, NpcService npcs) {
        this.plugin = plugin;
        this.db = db;
        this.npcs = npcs;
    }

    // ---------- load / save ----------

    public void loadAll() {
        try (Statement st = db.connection().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM bots")) {
            while (rs.next()) {
                World w = Bukkit.getWorld(rs.getString("world"));
                if (w == null) {
                    plugin.getLogger().warning("Bot #" + rs.getInt("id") + ": world tidak ditemukan, dilewati.");
                    continue;
                }
                BotType type = BotType.parse(rs.getString("type"));
                if (type == null) continue;
                Location loc = new Location(w, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), 0f);
                Bot bot = new Bot(rs.getInt("id"), UUID.fromString(rs.getString("owner")), type,
                        rs.getString("name"), rs.getInt("level"), loc, rs.getString("state"));
                bots.put(bot.id, bot);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Gagal memuat bot: " + e.getMessage());
        }
        // Spawn NPC setelah semua world pasti siap.
        Bukkit.getScheduler().runTask(plugin, () -> bots.values().forEach(npcs::spawn));
        plugin.getLogger().info(bots.size() + " bot dimuat.");
    }

    public void saveAll() {
        String sql = "UPDATE bots SET name=?, level=?, state=? WHERE id=?";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            for (Bot b : bots.values()) {
                ps.setString(1, b.name);
                ps.setInt(2, b.level);
                ps.setString(3, b.state);
                ps.setInt(4, b.id);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().severe("Gagal menyimpan bot: " + e.getMessage());
        }
    }

    public void shutdown() {
        saveAll();
        npcs.shutdown();
    }

    // ---------- kepemilikan ----------

    public List<Bot> botsOf(UUID owner) {
        List<Bot> out = new ArrayList<>();
        for (Bot b : bots.values()) if (b.owner.equals(owner)) out.add(b);
        return out;
    }

    public Bot get(int id) { return bots.get(id); }

    public int extraSlots(UUID owner) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT extra_slots FROM players WHERE uuid=?")) {
            ps.setString(1, owner.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    public void addExtraSlots(UUID owner, int amount) {
        try (PreparedStatement ps = db.connection().prepareStatement("""
                INSERT INTO players(uuid, extra_slots) VALUES(?, ?)
                ON CONFLICT(uuid) DO UPDATE SET extra_slots = extra_slots + excluded.extra_slots""")) {
            ps.setString(1, owner.toString());
            ps.setInt(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("addExtraSlots gagal: " + e.getMessage());
        }
    }

    public int slotLimit(UUID owner) {
        int free = plugin.getConfig().getInt("ownership.free-bots", 1);
        int max = plugin.getConfig().getInt("ownership.max-bots", 5);
        return Math.min(max, free + extraSlots(owner));
    }

    /** @return bot baru, atau null jika slot penuh / DB gagal. */
    public Bot create(UUID owner, BotType type, String name, Location station) {
        if (botsOf(owner).size() >= slotLimit(owner)) return null;
        String sql = "INSERT INTO bots(owner,type,name,level,world,x,y,z,yaw,state,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = db.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, owner.toString());
            ps.setString(2, type.name());
            ps.setString(3, name);
            ps.setInt(4, 1);
            ps.setString(5, station.getWorld().getName());
            ps.setDouble(6, station.getX());
            ps.setDouble(7, station.getY());
            ps.setDouble(8, station.getZ());
            ps.setFloat(9, station.getYaw());
            ps.setString(10, "IDLE");
            ps.setLong(11, System.currentTimeMillis());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) return null;
                Bot bot = new Bot(keys.getInt(1), owner, type, name, 1, station.clone(), "IDLE");
                bots.put(bot.id, bot);
                npcs.spawn(bot);
                return bot;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Gagal membuat bot: " + e.getMessage());
            return null;
        }
    }

    public boolean remove(Bot bot) {
        try (PreparedStatement ps = db.connection().prepareStatement("DELETE FROM bots WHERE id=?")) {
            ps.setInt(1, bot.id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Gagal menghapus bot: " + e.getMessage());
            return false;
        }
        npcs.despawn(bot);
        bots.remove(bot.id);
        return true;
    }

    public void rename(Bot bot, String name) {
        bot.name = name;
        npcs.rename(bot);
    }
}
