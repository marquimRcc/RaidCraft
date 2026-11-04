package dev.raidcraft.storage;

import dev.raidcraft.model.Generator;
import dev.raidcraft.model.OreKind;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/** Persistência YAML, sem banco ou dependências adicionais no MVP. */
public final class GeneratorRepository {
    private record Position(UUID worldId, int x, int y, int z) { }

    private final JavaPlugin plugin;
    private final File file;
    private final Map<String, Generator> byId = new LinkedHashMap<>();
    private final Map<Position, Generator> byPosition = new HashMap<>();

    public GeneratorRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "generators.yml");
    }

    public Optional<Generator> get(String id) {
        return Optional.ofNullable(byId.get(id.toLowerCase(java.util.Locale.ROOT)));
    }

    public Optional<Generator> at(Block block) {
        return Optional.ofNullable(byPosition.get(new Position(block.getWorld().getUID(),
                block.getX(), block.getY(), block.getZ())));
    }

    public Collection<Generator> all() {
        return java.util.List.copyOf(byId.values());
    }

    public boolean containsPosition(Block block) {
        return at(block).isPresent();
    }

    public void put(Generator generator) {
        Generator old = byId.put(generator.id(), generator);
        if (old != null) byPosition.remove(position(old));
        byPosition.put(position(generator), generator);
    }

    public void remove(String id) {
        Generator old = byId.remove(id.toLowerCase(java.util.Locale.ROOT));
        if (old != null) byPosition.remove(position(old));
    }

    private static Position position(Generator g) {
        return new Position(g.worldId(), g.x(), g.y(), g.z());
    }

    public void load() {
        byId.clear();
        byPosition.clear();
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("generators");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            try {
                ConfigurationSection node = section.getConfigurationSection(id);
                if (node == null) continue;
                UUID worldId = UUID.fromString(node.getString("world"));
                OreKind ore = OreKind.parse(node.getString("ore")).orElseThrow();
                int amount = node.getInt("amount", 1);
                int cooldown = node.getInt("cooldownSeconds", 30);
                if (amount < 1 || amount > 64 || cooldown < 1 || cooldown > 3600)
                    throw new IllegalArgumentException("Configuração fora dos limites");
                Generator generator = new Generator(id.toLowerCase(java.util.Locale.ROOT), worldId,
                        node.getInt("x"), node.getInt("y"), node.getInt("z"), ore, amount, cooldown);
                if (byPosition.containsKey(position(generator)))
                    throw new IllegalArgumentException("Local duplicado");
                put(generator);
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("Gerador inválido ignorado: " + id + " (" + ex.getMessage() + ")");
            }
        }
    }

    public boolean save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Generator g : byId.values()) {
            String base = "generators." + g.id();
            yaml.set(base + ".world", g.worldId().toString());
            yaml.set(base + ".x", g.x());
            yaml.set(base + ".y", g.y());
            yaml.set(base + ".z", g.z());
            yaml.set(base + ".ore", g.ore().name());
            yaml.set(base + ".amount", g.amount());
            yaml.set(base + ".cooldownSeconds", g.cooldownSeconds());
        }
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new IOException("Impossível criar pasta " + plugin.getDataFolder());
            }
            yaml.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Falha ao salvar geradores", e);
            return false;
        }
    }
}
