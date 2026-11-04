package dev.raidcraft.service;

import dev.raidcraft.model.Generator;
import dev.raidcraft.model.OreKind;
import dev.raidcraft.storage.GeneratorRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Toda manipulação de blocos e drops é executada na main thread do Paper. */
public final class GeneratorService {
    private final JavaPlugin plugin;
    private final GeneratorRepository repository;
    private final Map<String, BukkitTask> cooldowns = new HashMap<>();

    public GeneratorService(JavaPlugin plugin, GeneratorRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public GeneratorRepository repository() { return repository; }

    public void restore() {
        for (Generator generator : repository.all()) {
            Block block = blockOf(generator);
            if (block != null) block.setType(generator.ore().visibleBlock(), false);
        }
    }

    public void reload() {
        stop();
        repository.load();
        restore();
    }

    public void stop() {
        cooldowns.values().forEach(BukkitTask::cancel);
        cooldowns.clear();
    }

    public String create(String id, Block block, OreKind ore, int amount, int cooldownSeconds) {
        if (repository.get(id).isPresent()) return "Já existe um gerador com este ID.";
        if (repository.containsPosition(block)) return "Este bloco já pertence a um gerador.";
        if (!block.getType().isSolid() || block.getType() == Material.BEDROCK)
            return "Aponte para um bloco sólido comum.";
        Generator generator = new Generator(id, block.getWorld().getUID(), block.getX(), block.getY(),
                block.getZ(), ore, amount, cooldownSeconds);
        repository.put(generator);
        if (!repository.save()) {
            repository.remove(id);
            return "Falha ao salvar o gerador. Verifique os logs.";
        }
        block.setType(ore.visibleBlock(), false);
        return "Gerador '" + id + "' criado: " + ore.name() + " x" + amount
                + " / " + cooldownSeconds + "s.";
    }

    public String delete(String id) {
        Optional<Generator> found = repository.get(id);
        if (found.isEmpty()) return "Gerador não encontrado.";
        Generator generator = found.get();
        repository.remove(id);
        if (!repository.save()) {
            repository.put(generator);
            return "Falha ao salvar. O gerador foi mantido.";
        }
        BukkitTask task = cooldowns.remove(id);
        if (task != null) task.cancel();
        Block block = blockOf(generator);
        if (block != null && (block.getType() == Material.BEDROCK || block.getType() == generator.ore().visibleBlock())) {
            block.setType(Material.STONE, false);
        }
        return "Gerador '" + id + "' removido.";
    }

    /** O evento deve ter sido validado quanto a outros cancelamentos pelo listener. */
    public void harvest(Block block, Generator generator) {
        if (cooldowns.containsKey(generator.id())) return;
        if (block.getType() != generator.ore().visibleBlock()) return;
        block.setType(Material.BEDROCK, false);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5),
                new ItemStack(generator.ore().dropItem(), generator.amount()));
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(generator.id());
            if (repository.get(generator.id()).filter(generator::equals).isEmpty()) return;
            Block current = blockOf(generator);
            if (current != null && current.getType() == Material.BEDROCK) {
                current.setType(generator.ore().visibleBlock(), false);
            }
        }, generator.cooldownSeconds() * 20L);
        cooldowns.put(generator.id(), task);
    }

    private Block blockOf(Generator g) {
        World world = Bukkit.getWorld(g.worldId());
        if (world == null) {
            plugin.getLogger().warning("Mundo indisponível para gerador: " + g.id());
            return null;
        }
        return world.getBlockAt(g.x(), g.y(), g.z());
    }
}
