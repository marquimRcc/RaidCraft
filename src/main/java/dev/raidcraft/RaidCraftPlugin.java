package dev.raidcraft;

import dev.raidcraft.command.RaidCraftCommand;
import dev.raidcraft.listener.GeneratorListener;
import dev.raidcraft.service.GeneratorService;
import dev.raidcraft.storage.GeneratorRepository;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class RaidCraftPlugin extends JavaPlugin {
    private GeneratorService service;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        GeneratorRepository repository = new GeneratorRepository(this);
        repository.load();
        service = new GeneratorService(this, repository);
        service.restore();
        getServer().getPluginManager().registerEvents(new GeneratorListener(service), this);
        RaidCraftCommand handler = new RaidCraftCommand(service);
        Objects.requireNonNull(getCommand("raidcraft"), "Comando ausente no plugin.yml")
                .setExecutor(handler);
        Objects.requireNonNull(getCommand("raidcraft")).setTabCompleter(handler);
        getLogger().info("RaidCraft MVP inicializado. Geradores: " + repository.all().size());
    }

    @Override
    public void onDisable() {
        if (service != null) service.stop();
    }
}
