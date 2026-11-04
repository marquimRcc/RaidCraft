package dev.raidcraft.command;

import dev.raidcraft.model.Generator;
import dev.raidcraft.model.OreKind;
import dev.raidcraft.service.GeneratorService;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class RaidCraftCommand implements CommandExecutor, TabCompleter {
    private final GeneratorService service;

    public RaidCraftCommand(GeneratorService service) { this.service = service; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("raidcraft.admin")) {
            tell(sender, "Você não tem permissão.");
            return true;
        }
        if (args.length == 0) return help(sender);
        String operation = args[0].toLowerCase(Locale.ROOT);
        switch (operation) {
            case "create" -> create(sender, args);
            case "remove" -> {
                if (args.length != 2) tell(sender, "Uso: /rc remove <id>");
                else tell(sender, service.delete(args[1].toLowerCase(Locale.ROOT)));
            }
            case "info" -> {
                if (args.length != 2) tell(sender, "Uso: /rc info <id>");
                else service.repository().get(args[1]).ifPresentOrElse(
                        g -> tell(sender, describe(g)),
                        () -> tell(sender, "Gerador não encontrado."));
            }
            case "list" -> {
                var all = service.repository().all();
                tell(sender, "Geradores cadastrados: " + all.size());
                all.forEach(g -> tell(sender, "- " + describe(g)));
            }
            case "reload" -> {
                service.reload();
                tell(sender, "Geradores recarregados de generators.yml.");
            }
            case "help" -> help(sender);
            default -> help(sender);
        }
        return true;
    }

    private void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            tell(sender, "Esse comando deve ser executado dentro do jogo.");
            return;
        }
        if (args.length < 3 || args.length > 5) {
            tell(sender, "Uso: /rc create <id> <ore> [quantidade=1] [cooldown=30]");
            return;
        }
        String id = args[1].toLowerCase(Locale.ROOT);
        if (!id.matches("[a-z0-9_-]{1,32}")) {
            tell(sender, "ID: até 32 caracteres (letras, números, - ou _).");
            return;
        }
        Optional<OreKind> ore = OreKind.parse(args[2]);
        if (ore.isEmpty()) {
            tell(sender, "Minérios permitidos: " + OreKind.allowedNames());
            return;
        }
        final int amount;
        final int cooldown;
        try {
            amount = args.length >= 4 ? Integer.parseInt(args[3]) : 1;
            cooldown = args.length >= 5 ? Integer.parseInt(args[4]) : 30;
        } catch (NumberFormatException ex) {
            tell(sender, "Quantidade e cooldown precisam ser números inteiros.");
            return;
        }
        if (amount < 1 || amount > 64 || cooldown < 1 || cooldown > 3600) {
            tell(sender, "Quantidade: 1–64; cooldown: 1–3600 segundos.");
            return;
        }
        Block target = player.getTargetBlockExact(6);
        if (target == null) {
            tell(sender, "Olhe para um bloco a até 6 blocos de distância.");
            return;
        }
        tell(sender, service.create(id, target, ore.get(), amount, cooldown));
    }

    private static String describe(Generator g) {
        return g.id() + " | " + g.ore().name() + " x" + g.amount()
                + " | " + g.cooldownSeconds() + "s | "
                + g.x() + "," + g.y() + "," + g.z();
    }

    private static boolean help(CommandSender sender) {
        tell(sender, "/rc create <id> <ore> [quantidade] [cooldown] — olhe para um bloco");
        tell(sender, "/rc list | /rc info <id> | /rc remove <id> | /rc reload");
        tell(sender, "Minérios: " + OreKind.allowedNames());
        return true;
    }

    private static void tell(CommandSender sender, String value) {
        sender.sendMessage(ChatColor.AQUA + "[RaidCraft] " + ChatColor.WHITE + value);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("raidcraft.admin")) return List.of();
        if (args.length == 1) return filter(List.of("create", "remove", "info", "list", "reload", "help"), args[0]);
        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("info")))
            return filter(service.repository().all().stream().map(Generator::id).toList(), args[1]);
        if (args.length == 3 && args[0].equalsIgnoreCase("create"))
            return filter(Arrays.stream(OreKind.values()).map(Enum::name).toList(), args[2]);
        return List.of();
    }

    private static List<String> filter(List<String> values, String prefix) {
        return values.stream().filter(v -> v.toLowerCase(Locale.ROOT)
                .startsWith(prefix.toLowerCase(Locale.ROOT))).toList();
    }
}
