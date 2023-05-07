package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TierbookCommand extends AbstractCommand<ExcellentEnchants> {

    public TierbookCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"tierbook"}, Perms.COMMAND_TIERBOOK);
    }

    @Override
    public @NotNull String getDescription() {
        return this.plugin.getMessage(Lang.COMMAND_TIER_BOOK_DESC).getLocalized();
    }

    @Override
    public @NotNull String getUsage() {
        return this.plugin.getMessage(Lang.COMMAND_TIER_BOOK_USAGE).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public @NotNull List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) return CollectionsUtil.playerNames(player);
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
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length != 4) {
            this.printUsage(sender);
            return;
        }

        Player player = this.plugin.getServer().getPlayer(args[1]);
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        //region Construct a weight map of tiers
        Map<Tier, Double> weightMap = new HashMap<>();
        Arrays.stream(args[2].toLowerCase().split(","))
            .forEach(t -> {
                String[] kv = t.split(":");
                Tier tier = this.plugin.getTierManager().getTierById(kv[0]);
                double weight = (kv.length < 2) ? 1 : Double.parseDouble(kv[1]);
                weightMap.put(tier, weight);
            });
        if (weightMap.isEmpty()) {
            this.plugin.getMessage(Lang.COMMAND_TIER_BOOK_ERROR).send(sender);
            return;
        }
        //endregion

        Tier tier = Rnd.getByWeight(weightMap);
        ExcellentEnchant enchant = Rnd.get(tier.getEnchants());
        if (enchant == null) {
            this.plugin.getMessage(Lang.ERROR_NO_ENCHANT).send(sender);
            return;
        }

        int level = StringUtil.getInteger(args[3], -1, true);
        if (level < 1) {
            level = Rnd.get(enchant.getStartLevel(), enchant.getMaxLevel());
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantManager.addEnchantment(item, enchant, level, true);
        PlayerUtil.addItem(player, item);

        this.plugin.getMessage(Lang.COMMAND_TIER_BOOK_DONE)
            .replace(tier.replacePlaceholders())
            .replace(Placeholders.Player.replacer(player))
            .send(sender);
    }
}
