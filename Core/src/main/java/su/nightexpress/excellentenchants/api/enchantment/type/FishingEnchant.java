package su.nightexpress.excellentenchants.api.enchantment.type;

import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.api.event.MiniGameStartEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface FishingEnchant extends IEnchantment {

    boolean onFishingResult(@NotNull FishResultEvent event, @NotNull ItemStack item, @NotNull ItemStack result,  int level);

    boolean onFishingStart(@NotNull MiniGameStartEvent event, @NotNull ItemStack item, int level);

    boolean onFishing(PlayerFishEvent event, ItemStack item, int level);
}
