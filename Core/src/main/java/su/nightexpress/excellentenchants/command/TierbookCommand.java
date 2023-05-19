package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.*;
import java.util.stream.Stream;

public class TierbookCommand extends AbstractCommand<ExcellentEnchants> {

    public TierbookCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"tierbook"}, Perms.COMMAND_TIERBOOK);
        this.setDescription(plugin.getMessage(Lang.COMMAND_TIER_BOOK_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_TIER_BOOK_USAGE));
    }

    @Override
    public @NotNull List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.playerNames(player);
        }
        //region Kinda smart completions of multiple tiers
        if (arg == 2) {
            String in = args[2];
            if (in.endsWith(":")) {
                return Stream.of("1", "5", "10", "20")
                    .map(str -> in + str)
                    .toList();
            } else if (in.endsWith(",")) {
                return this.plugin.getTierManager().getTierIds().stream()
                    .filter(str -> !in.contains(str))
                    .map(str -> in + str)
                    .toList();
            } else if (!in.isEmpty() && !Character.isDigit(in.charAt(in.length() - 1))) {
                return this.plugin.getTierManager().getTierIds().stream()
                    .filter(str -> !in.contains(str))
                    .map(str -> in + "," + str)
                    .toList();
            } else {
                return this.plugin.getTierManager().getTierIds();
            }
        }
        //endregion
        if (arg == 3) return Arrays.asList("-1", "1", "5", "10");
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 4) {
            this.printUsage(sender);
            return;
        }

        Player player = plugin.getServer().getPlayer(result.getArg(1));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        //region Construct a weight map of tiers
        Map<Tier, Double> weightMap = new HashMap<>();
        Arrays.stream(result.getArg(2).toLowerCase().split(","))
            .forEach(t -> {
                String[] kv = t.split(":");
                Tier tier = this.plugin.getTierManager().getTierById(kv[0]);
                double weight = (kv.length < 2) ? 1 : Double.parseDouble(kv[1]);
                weightMap.put(tier, weight);
            });
        if (weightMap.isEmpty()) {
            plugin.getMessage(Lang.COMMAND_TIER_BOOK_ERROR).send(sender);
            return;
        }
        //endregion

        Tier tier = Rnd.getByWeight(weightMap);
        Set<ExcellentEnchant> enchants = EnchantRegistry.getOfTier(tier);
        ExcellentEnchant enchant = enchants.isEmpty() ? null : Rnd.get(enchants);
        if (enchant == null) {
            plugin.getMessage(Lang.ERROR_NO_ENCHANT).send(sender);
            return;
        }

        int level = result.getInt(3, -1);
        if (level < 1) {
            level = Rnd.get(enchant.getStartLevel(), enchant.getMaxLevel());
        } else {
            level = EnchantUtils.clampLevel(level, enchant);
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantUtils.add(item, enchant, level, true);
        PlayerUtil.addItem(player, item);

        plugin.getMessage(Lang.COMMAND_TIER_BOOK_DONE)
            .replace(tier.replacePlaceholders())
            .replace(Placeholders.Player.replacer(player))
            .send(sender);
    }
}
