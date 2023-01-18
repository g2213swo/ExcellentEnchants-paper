package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.utils.ComponentUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.*;
import java.util.function.UnaryOperator;

public class EnchantTelekinesis extends IEnchantChanceTemplate implements CustomDropEnchant {

    private LangMessage messageDropReceived;
    private String messageItemName; // Stored as MiniMessage string representation
    private String messageItemSeparator;

    public static final String ID = "telekinesis";

    public EnchantTelekinesis(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.messageDropReceived = new LangMessage(plugin, cfg.getString("Settings.Message.Drop_Received", ""));
        this.messageItemName = cfg.getString("Settings.Message.Item_Name", "<gray>x%item_amount% <white>%item_name%");
        this.messageItemSeparator = cfg.getString("Settings.Message.Item_Separator", "<gray>, ");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Radius");
        cfg.remove("Settings.Power");

        cfg.addMissing("Settings.Message.Drop_Received", "{message: ~type: ACTION_BAR; ~prefix: false;}%items%");
        cfg.addMissing("Settings.Message.Item_Name", "<gray>x%item_amount% <white>%item_name%");
        cfg.addMissing("Settings.Message.Item_Separator", "<gray>, ");
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return super.replacePlaceholders(level);
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.TOOL};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public void handleDrop(
        @NotNull EnchantDropContainer container,
        @NotNull Player player,
        @NotNull ItemStack item,
        int level
    ) {
        BlockDropItemEvent dropItemEvent = container.getParent();

        if (!this.isEnchantmentAvailable(player))
            return;
        // if (block.getState() instanceof Container)
        //     return;
        if (!this.checkTriggerChance(level))
            return;

        List<ItemStack> drops = new ArrayList<>();
        drops.addAll(dropItemEvent.getItems().stream().map(Item::getItemStack).toList());
        drops.addAll(container.getDrops());
        drops.removeIf(Objects::isNull);

        StringBuilder sb = new StringBuilder();
        drops.forEach(drop -> {
            PlayerUtil.addItem(player, drop);
            if (!sb.isEmpty()) sb.append(this.messageItemSeparator);
            sb.append(this.messageItemName
                .replace("%item_name%", ComponentUtil.asMiniMessage(ItemUtil.getName(drop)))
                .replace("%item_amount%", String.valueOf(drop.getAmount()))
            );
        });
        this.messageDropReceived
            .replace("%items%", sb.toString())
            .send(player);

        container.getDrops().clear();
        dropItemEvent.getItems().clear();
    }
}
