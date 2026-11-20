package com.hiyura.bots.claim;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Integrasi RedProtect.
 *
 * CATATAN: nama package/method di bawah mengikuti RedProtect 8.x dan HARUS dicocokkan
 * dengan jar RedProtect yang terpasang (taruh di libs/). Jika compile error, cukup
 * sesuaikan import dan 4 pemanggilan di method canWork().
 */
public final class RedProtectProvider implements ClaimProvider {

    private final boolean allowWilderness;

    public RedProtectProvider(boolean allowWilderness) {
        this.allowWilderness = allowWilderness;
    }

    @Override public String name() { return "RedProtect"; }

    @Override
    public boolean canWork(UUID owner, Location loc) {
        Region region = RedProtect.get().getAPI().getRegion(loc);
        if (region == null) return allowWilderness;
        String id = owner.toString();
        return region.isLeader(id) || region.isAdmin(id) || region.isMember(id);
    }
}
