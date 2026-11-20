package com.hiyura.bots;

import com.hiyura.bots.bot.BotManager;
import com.hiyura.bots.citizens.NpcService;
import com.hiyura.bots.claim.ClaimProvider;
import com.hiyura.bots.claim.NoClaimProvider;
import com.hiyura.bots.claim.RedProtectProvider;
import com.hiyura.bots.commands.BotCommand;
import com.hiyura.bots.database.Database;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class HiyuraBotsPlugin extends JavaPlugin {

    private Database database;
    private NpcService npcService;
    private BotManager botManager;
    private ClaimProvider claimProvider;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            getDataFolder().mkdirs();
            database = new Database(new File(getDataFolder(), "hiyurabots.db"), getLogger());
        } catch (SQLException e) {
            getLogger().severe("Gagal membuka database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        claimProvider = setupClaimProvider();
        npcService = new NpcService(this);
        botManager = new BotManager(this, database, npcService);
        botManager.loadAll();

        var cmd = new BotCommand(this, botManager);
        getCommand("bot").setExecutor(cmd);
        getCommand("bot").setTabCompleter(cmd);

        long period = getConfig().getLong("performance.autosave-seconds", 60) * 20L;
        getServer().getScheduler().runTaskTimer(this, botManager::saveAll, period, period);

        getLogger().info("HiyuraBots aktif. Claim provider: " + claimProvider.name());
    }

    @Override
    public void onDisable() {
        if (botManager != null) botManager.shutdown();
        if (database != null) database.close();
    }

    public void reload() {
        reloadConfig();
        claimProvider = setupClaimProvider();
    }

    private ClaimProvider setupClaimProvider() {
        String type = getConfig().getString("claim.provider", "redprotect");
        if (type.equalsIgnoreCase("redprotect")
                && getServer().getPluginManager().isPluginEnabled("RedProtect")) {
            try {
                return new RedProtectProvider(getConfig().getBoolean("claim.allow-wilderness", false));
            } catch (Throwable t) {
                getLogger().warning("Hook RedProtect gagal: " + t);
            }
        }
        getLogger().warning("Claim provider tidak aktif -> semua area dianggap TIDAK diizinkan kecuali wilderness diizinkan.");
        return new NoClaimProvider(getConfig().getBoolean("claim.allow-wilderness", false));
    }

    public ClaimProvider claims() { return claimProvider; }
    public Database database() { return database; }
    public BotManager bots() { return botManager; }
}
