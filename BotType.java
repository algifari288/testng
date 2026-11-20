package com.hiyura.bots.bot;

public enum BotType {
    MINER("Miner"),
    BUILDER("Builder");

    public final String display;

    BotType(String display) { this.display = display; }

    public static BotType parse(String s) {
        for (BotType t : values()) if (t.name().equalsIgnoreCase(s)) return t;
        return null;
    }
}
