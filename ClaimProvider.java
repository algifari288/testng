package com.hiyura.bots.claim;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/** Abstraksi plugin claim, supaya mudah diganti nanti. */
public interface ClaimProvider {

    String name();

    /** Apakah owner (online/offline) boleh bekerja (mengubah blok) di lokasi ini. */
    boolean canWork(UUID owner, Location location);

    /** Cek seluruh kotak area; true hanya jika semua titik sampel diizinkan. */
    default boolean canWorkInBox(UUID owner, World world,
                                 int x1, int y1, int z1, int x2, int y2, int z2) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        // Sampel tiap 8 blok + semua pojok (region RedProtect berbentuk kotak/kolom).
        for (int x = minX; x <= maxX + 7; x += 8) {
            for (int z = minZ; z <= maxZ + 7; z += 8) {
                int cx = Math.min(x, maxX), cz = Math.min(z, maxZ);
                if (!canWork(owner, new Location(world, cx, minY, cz))) return false;
                if (!canWork(owner, new Location(world, cx, maxY, cz))) return false;
            }
        }
        return true;
    }
}
