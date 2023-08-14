package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.api.event.MiniGameStartEvent;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class AutoReelEnchant extends ExcellentEnchant implements FishingEnchant {

    public static final String ID = "auto_reel";

    public AutoReelEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc>");
        // "enchantment.g2213swo.auto_reel.desc": "Automatically reels in a hook on bite."
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.6);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @Override
    public boolean onFishingResult(@NotNull FishResultEvent event, @NotNull ItemStack item, @NotNull ItemStack result, int level) {
        return false;
    }

    @Override
    public boolean onFishingStart(@NotNull MiniGameStartEvent event, @NotNull ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.BITE) return false;
        if (!this.isAvailableToUse(event.getPlayer())) return false;

        this.plugin.runTask(task -> {
            if (event.isCancelled()) return;

            plugin.getEnchantNMS().sendAttackPacket(event.getPlayer(), 0);
            plugin.getEnchantNMS().retrieveHook(event.getHook(), item);
        });
        return true;
    }
}
