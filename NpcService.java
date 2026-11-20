package com.hiyura.bots.citizens;

import com.hiyura.bots.HiyuraBotsPlugin;
import com.hiyura.bots.bot.Bot;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * Membungkus Citizens. Registry bersifat memori (anonim): NPC dibuat ulang dari database
 * setiap server start, jadi tidak ada NPC yatim di saves.yml Citizens.
 */
public final class NpcService {

    private final NPCRegistry registry;
    private final Map<Integer, NPC> npcs = new HashMap<>();

    public NpcService(HiyuraBotsPlugin plugin) {
        this.registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
    }

    public void spawn(Bot bot) {
        despawn(bot);
        NPC npc = registry.createNPC(EntityType.PLAYER, "§b" + bot.name);
        npc.setProtected(true);
        npc.spawn(bot.station);
        bot.npcId = npc.getUniqueId();
        npcs.put(bot.id, npc);
    }

    public void despawn(Bot bot) {
        NPC npc = npcs.remove(bot.id);
        if (npc != null) npc.destroy();
        bot.npcId = null;
    }

    public void rename(Bot bot) {
        NPC npc = npcs.get(bot.id);
        if (npc != null) npc.setName("§b" + bot.name);
    }

    /** Mengembalikan id bot jika entity ini NPC milik HiyuraBots, atau -1. */
    public int botIdOf(NPC npc) {
        for (var e : npcs.entrySet()) if (e.getValue().equals(npc)) return e.getKey();
        return -1;
    }

    public void shutdown() {
        npcs.values().forEach(NPC::destroy);
        npcs.clear();
        registry.deregisterAll();
    }
}
