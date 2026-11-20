package com.hiyura.bots.claim;

import org.bukkit.Location;

import java.util.UUID;

public final class NoClaimProvider implements ClaimProvider {

    private final boolean allowAll;

    public NoClaimProvider(boolean allowAll) { this.allowAll = allowAll; }

    @Override public String name() { return "none"; }

    @Override public boolean canWork(UUID owner, Location location) { return allowAll; }
}
