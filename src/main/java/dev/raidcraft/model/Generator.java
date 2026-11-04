package dev.raidcraft.model;

import org.bukkit.Location;
import org.bukkit.block.Block;
import java.util.UUID;

/** Configuração persistida de um gerador em um ponto fixo do mundo. */
public record Generator(String id, UUID worldId, int x, int y, int z,
                        OreKind ore, int amount, int cooldownSeconds) {
    public boolean isAt(Block block) {
        Location location = block.getLocation();
        return block.getWorld().getUID().equals(worldId)
                && location.getBlockX() == x && location.getBlockY() == y
                && location.getBlockZ() == z;
    }
}
