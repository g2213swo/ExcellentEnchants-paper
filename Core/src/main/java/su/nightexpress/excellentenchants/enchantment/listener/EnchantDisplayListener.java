package su.nightexpress.excellentenchants.enchantment.listener;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

public class EnchantDisplayListener extends AbstractListener<ExcellentEnchants> {

    public EnchantDisplayListener(final @NotNull EnchantManager enchantManager) {
        super(enchantManager.plugin());
    }

    // @EventHandler
    // public void onNetworkItemSerialize(NetworkItemSerializeEvent event) {
    //     ItemStack item = event.getItemStack();
    // }

}
