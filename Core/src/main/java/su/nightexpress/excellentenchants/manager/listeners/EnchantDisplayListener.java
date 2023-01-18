package su.nightexpress.excellentenchants.manager.listeners;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.manager.EnchantManager;

public class EnchantDisplayListener extends AbstractListener<ExcellentEnchants> {

    public EnchantDisplayListener(@NotNull final EnchantManager enchantManager) {
        super(enchantManager.plugin());
    }

    // @EventHandler
    // public void onNetworkItemSerialize(NetworkItemSerializeEvent event) {
    //     ItemStack item = event.getItemStack();
    // }

}
