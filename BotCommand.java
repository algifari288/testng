package com.hiyura.bots.commands;

import com.hiyura.bots.HiyuraBotsPlugin;
import com.hiyura.bots.bot.Bot;
import com.hiyura.bots.bot.BotManager;
import com.hiyura.bots.bot.BotType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BotCommand implements CommandExecutor, TabCompleter {

    private static final String P = "§8[§bHiyuraBots§8] §7";

    private final HiyuraBotsPlugin plugin;
    private final BotManager bots;

    public BotCommand(HiyuraBotsPlugin plugin, BotManager bots) {
        this.plugin = plugin;
        this.bots = bots;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (a.length == 0) return help(sender);

        switch (a[0].toLowerCase()) {
            case "create" -> {
                if (!(sender instanceof Player p)) return msg(sender, "Hanya untuk pemain.");
                if (a.length < 2 || BotType.parse(a[1]) == null) return msg(sender, "Pakai: /bot create <miner|builder> [nama]");
                BotType type = BotType.parse(a[1]);
                String name = a.length >= 3 ? String.join(" ", Arrays.copyOfRange(a, 2, a.length))
                        : p.getName() + "'s " + type.display;
                if (name.length() > 24) return msg(sender, "Nama maksimal 24 karakter.");
                if (!plugin.claims().canWork(p.getUniqueId(), p.getLocation()))
                    return msg(sender, "Worker Station hanya boleh dibuat di claim milikmu.");
                Bot bot = bots.create(p.getUniqueId(), type, name, p.getLocation());
                if (bot == null) return msg(sender, "Slot bot penuh ("
                        + bots.slotLimit(p.getUniqueId()) + "). Beli slot tambahan untuk menambah bot.");
                msg(sender, "Bot #" + bot.id + " (" + type.display + ") dibuat.");
            }
            case "list" -> {
                if (!(sender instanceof Player p)) return msg(sender, "Hanya untuk pemain.");
                List<Bot> list = bots.botsOf(p.getUniqueId());
                if (list.isEmpty()) return msg(sender, "Kamu belum punya bot. Pakai /bot create.");
                for (Bot b : list) p.sendMessage(P + "#" + b.id + " §f" + b.name + " §7(" + b.type.display
                        + ", Lv." + b.level + ", " + b.state + ")");
            }
            case "rename" -> {
                if (!(sender instanceof Player p)) return msg(sender, "Hanya untuk pemain.");
                if (a.length < 3) return msg(sender, "Pakai: /bot rename <id> <nama>");
                Bot b = owned(p, a[1]);
                if (b == null) return msg(sender, "Bot tidak ditemukan / bukan milikmu.");
                String name = String.join(" ", Arrays.copyOfRange(a, 2, a.length));
                if (name.length() > 24) return msg(sender, "Nama maksimal 24 karakter.");
                bots.rename(b, name);
                msg(sender, "Nama bot diganti.");
            }
            case "remove" -> {
                if (!(sender instanceof Player p)) return msg(sender, "Hanya untuk pemain.");
                if (a.length < 2) return msg(sender, "Pakai: /bot remove <id>");
                Bot b = owned(p, a[1]);
                if (b == null) return msg(sender, "Bot tidak ditemukan / bukan milikmu.");
                msg(sender, bots.remove(b) ? "Bot dihapus." : "Gagal menghapus bot.");
            }
            case "reload" -> {
                if (!sender.hasPermission("hiyurabots.admin")) return msg(sender, "Tidak ada izin.");
                plugin.reload();
                msg(sender, "Config dimuat ulang.");
            }
            case "admin" -> {
                if (!sender.hasPermission("hiyurabots.admin")) return msg(sender, "Tidak ada izin.");
                // /bot admin addslot <player> [jumlah]
                if (a.length >= 3 && a[1].equalsIgnoreCase("addslot")) {
                    OfflinePlayer t = Bukkit.getOfflinePlayer(a[2]);
                    int n = 1;
                    if (a.length >= 4) {
                        try { n = Integer.parseInt(a[3]); } catch (NumberFormatException e) { return msg(sender, "Jumlah tidak valid."); }
                    }
                    bots.addExtraSlots(t.getUniqueId(), n);
                    msg(sender, "Slot bot " + a[2] + " +" + n);
                } else msg(sender, "Pakai: /bot admin addslot <player> [jumlah]");
            }
            default -> help(sender);
        }
        return true;
    }

    private Bot owned(Player p, String idStr) {
        try {
            Bot b = bots.get(Integer.parseInt(idStr));
            return b != null && b.owner.equals(p.getUniqueId()) ? b : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean help(CommandSender s) {
        s.sendMessage(P + "/bot create <miner|builder> [nama]");
        s.sendMessage(P + "/bot list | rename <id> <nama> | remove <id>");
        return true;
    }

    private boolean msg(CommandSender s, String m) {
        s.sendMessage(P + m);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        List<String> out = new ArrayList<>();
        if (a.length == 1) {
            out.addAll(List.of("create", "list", "rename", "remove"));
            if (s.hasPermission("hiyurabots.admin")) out.addAll(List.of("reload", "admin"));
        } else if (a.length == 2 && a[0].equalsIgnoreCase("create")) {
            out.addAll(List.of("miner", "builder"));
        } else if (a.length == 2 && a[0].equalsIgnoreCase("admin")) {
            out.add("addslot");
        }
        String last = a[a.length - 1].toLowerCase();
        out.removeIf(x -> !x.startsWith(last));
        return out;
    }
}
