package dev.raidcraft.smoke;

import dev.raidcraft.RaidCraftPlugin;
import dev.raidcraft.model.Generator;
import dev.raidcraft.model.OreKind;
import dev.raidcraft.service.GeneratorService;
import dev.raidcraft.storage.GeneratorRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Only installed by the CI workflow. Never deploy this test plugin to a production server.
 * Runs on Paper's main thread and exercises the actual RaidCraft listener and service.
 */
public final class RaidCraftSmokePlugin extends JavaPlugin {
    private static final String GENERATOR_ID = "ci_smoke";
    private GeneratorService service;
    private Block testBlock;
    private Material originalMaterial;
    private int initialDiamonds;

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, this::runInitialChecks, 40L);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private void runInitialChecks() {
        try {
            Plugin target = Bukkit.getPluginManager().getPlugin("RaidCraft");
            require(target instanceof RaidCraftPlugin && target.isEnabled(),
                    "RaidCraft plugin was not enabled");

            Field field = RaidCraftPlugin.class.getDeclaredField("service");
            field.setAccessible(true);
            service = (GeneratorService) field.get(target);
            require(service != null, "RaidCraft service is missing");

            require(!Bukkit.getWorlds().isEmpty(), "No Minecraft world was loaded");
            World world = Bukkit.getWorlds().get(0);
            world.getChunkAt(0, 0).load();
            int y = Math.min(world.getMaxHeight() - 10,
                    Math.max(world.getMinHeight() + 10, world.getHighestBlockYAt(0, 0) + 30));
            testBlock = world.getBlockAt(0, y, 0);
            originalMaterial = testBlock.getType();
            testBlock.setType(Material.STONE, false);

            String result = service.create(GENERATOR_ID, testBlock, OreKind.DIAMOND, 2, 2);
            require(result.contains("criado:"), "Generator creation failed: " + result);
            require(testBlock.getType() == Material.DIAMOND_ORE,
                    "Visible block did not become diamond ore");

            Generator generator = service.repository().get(GENERATOR_ID).orElseThrow();
            require(generator.amount() == 2 && generator.cooldownSeconds() == 2,
                    "Generator configuration mismatch");

            GeneratorRepository diskCopy = new GeneratorRepository((JavaPlugin) target);
            diskCopy.load();
            require(diskCopy.get(GENERATOR_ID).isPresent(), "Generator was not persisted");

            initialDiamonds = diamondItemsNearTestBlock();
            BlockBreakEvent breakEvent = breakWithFakePlayer();
            require(breakEvent.isCancelled(), "BlockBreakEvent was not cancelled by RaidCraft");
            require(testBlock.getType() == Material.BEDROCK,
                    "Generator did not switch to bedrock during cooldown");
            require(diamondItemsNearTestBlock() == initialDiamonds + 2,
                    "Generator must drop exactly two real diamonds");

            BlockBreakEvent duplicateBreak = breakWithFakePlayer();
            require(duplicateBreak.isCancelled(), "Second break event was not cancelled");
            require(diamondItemsNearTestBlock() == initialDiamonds + 2,
                    "Duplicate break produced additional items");

            getLogger().info("RAIDCRAFT_SMOKE_STAGE1_PASS");
            Bukkit.getScheduler().runTaskLater(this, this::runCooldownChecks, 50L);
        } catch (Throwable ex) {
            fail(ex);
        }
    }

    private BlockBreakEvent breakWithFakePlayer() {
        Player fakePlayer = (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[] {Player.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) return "RaidCraftCI";
                    if (method.getName().equals("getUniqueId")) return UUID.fromString(
                            "00000000-0000-0000-0000-000000000001");
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) return false;
                    if (type == int.class) return 0;
                    if (type == long.class) return 0L;
                    if (type == float.class) return 0f;
                    if (type == double.class) return 0d;
                    if (type == short.class) return (short) 0;
                    if (type == byte.class) return (byte) 0;
                    if (type == char.class) return (char) 0;
                    return null;
                });
        BlockBreakEvent event = new BlockBreakEvent(testBlock, fakePlayer);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    private int diamondItemsNearTestBlock() {
        int amount = 0;
        for (var entity : testBlock.getWorld().getNearbyEntities(
                testBlock.getLocation().add(0.5, 0.5, 0.5), 3, 3, 3)) {
            if (entity instanceof Item item && item.getItemStack().getType() == Material.DIAMOND) {
                amount += item.getItemStack().getAmount();
            }
        }
        return amount;
    }

    private void runCooldownChecks() {
        try {
            require(testBlock.getType() == Material.DIAMOND_ORE,
                    "Diamond ore did not regenerate after cooldown");
            service.reload();
            require(service.repository().get(GENERATOR_ID).isPresent(),
                    "Reload lost the generator persisted in YAML");
            require(testBlock.getType() == Material.DIAMOND_ORE,
                    "Reload did not restore the visible diamond ore");

            BlockBreakEvent secondHarvest = breakWithFakePlayer();
            require(secondHarvest.isCancelled(), "Second harvest was not intercepted");
            require(diamondItemsNearTestBlock() == initialDiamonds + 4,
                    "Second harvest should produce two more diamonds");

            String removed = service.delete(GENERATOR_ID);
            require(removed.contains("removido"), "Generator removal failed: " + removed);
            require(service.repository().get(GENERATOR_ID).isEmpty(),
                    "Generator still exists after remove");
            require(testBlock.getType() == Material.STONE,
                    "Removed generator was not replaced with stone");

            GeneratorRepository diskCopy = new GeneratorRepository(
                    (JavaPlugin) Bukkit.getPluginManager().getPlugin("RaidCraft"));
            diskCopy.load();
            require(diskCopy.get(GENERATOR_ID).isEmpty(), "Removed generator persisted on disk");
            restoreTestBlock();
            getLogger().info("RAIDCRAFT_SMOKE_PASS: create, listener, exact drops, anti-duplicate, cooldown, reload, remove");
            Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 20L);
        } catch (Throwable ex) {
            fail(ex);
        }
    }

    private void restoreTestBlock() {
        if (testBlock == null) return;
        for (var entity : testBlock.getWorld().getNearbyEntities(
                testBlock.getLocation().add(0.5, 0.5, 0.5), 3, 3, 3)) {
            if (entity instanceof Item item && item.getItemStack().getType() == Material.DIAMOND) {
                item.remove();
            }
        }
        if (originalMaterial != null) testBlock.setType(originalMaterial, false);
    }

    private void fail(Throwable ex) {
        getLogger().log(Level.SEVERE, "RAIDCRAFT_SMOKE_FAIL: " + ex.getMessage(), ex);
        try { restoreTestBlock(); } catch (Throwable ignored) { }
        Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 20L);
    }
}
