package com.hiyura.bots.bot;

import org.bukkit.Location;

import java.util.UUID;

/** Data satu bot. NPC Citizens hanya tampilan; sumber kebenaran ada di sini + database. */
public final class Bot {

    public final int id;
    public final UUID owner;
    public final BotType type;
    public String name;
    public int level;
    public Location station;
    public String state; // IDLE, WORKING, PAUSED, ...

    /** UUID NPC Citizens saat ini (transient, tidak disimpan). */
    public UUID npcId;

    public Bot(int id, UUID owner, BotType type, String name, int level, Location station, String state) {
        this.id = id;
        this.owner = owner;
        this.type = type;
        this.name = name;
        this.level = level;
        this.station = station;
        this.state = state;
    }
}
