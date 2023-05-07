package su.nightexpress.excellentenchants.api.enchantment.util;

import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EnchantDropContainer {

    private final BlockDropItemEvent parent;
    private final List<ItemStack> drops;

    public EnchantDropContainer(@NotNull BlockDropItemEvent parent) {
        this.parent = parent;
        this.drops = new ArrayList<>();
    }

    public @NotNull BlockDropItemEvent getParent() {
        return this.parent;
    }

    public @NotNull List<ItemStack> getDrops() {
        return this.drops;
    }
}
