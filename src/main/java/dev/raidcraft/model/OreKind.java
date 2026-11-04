package dev.raidcraft.model;

import org.bukkit.Material;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** Cada bloco exibido tem uma recompensa material correspondente, sem tabelas aleatórias. */
public enum OreKind {
    DIAMOND(Material.DIAMOND_ORE, Material.DIAMOND),
    EMERALD(Material.EMERALD_ORE, Material.EMERALD),
    IRON(Material.IRON_ORE, Material.RAW_IRON),
    GOLD(Material.GOLD_ORE, Material.RAW_GOLD),
    COPPER(Material.COPPER_ORE, Material.RAW_COPPER),
    COAL(Material.COAL_ORE, Material.COAL),
    REDSTONE(Material.REDSTONE_ORE, Material.REDSTONE),
    LAPIS(Material.LAPIS_ORE, Material.LAPIS_LAZULI),
    QUARTZ(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);

    private final Material visibleBlock;
    private final Material dropItem;

    OreKind(Material visibleBlock, Material dropItem) {
        this.visibleBlock = visibleBlock;
        this.dropItem = dropItem;
    }

    public Material visibleBlock() { return visibleBlock; }
    public Material dropItem() { return dropItem; }

    public static Optional<OreKind> parse(String value) {
        if (value == null) return Optional.empty();
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(v -> v.name().equals(normalized)).findFirst();
    }

    public static String allowedNames() {
        return String.join(", ", Arrays.stream(values()).map(Enum::name).toList());
    }
}
