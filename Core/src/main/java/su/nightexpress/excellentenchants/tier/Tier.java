package su.nightexpress.excellentenchants.tier;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.api.placeholder.PlaceholderConstants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tier implements IPlaceholder {

    public static final Tier DEFAULT = new Tier(PlaceholderConstants.DEFAULT, 100, "Default", NamedTextColor.WHITE, new HashMap<>());

    static {
        Stream.of(ObtainType.values()).forEach(type -> DEFAULT.getChance().put(type, 100D));
    }

    private final String id;
    private final int priority;
    private final String name; // Stored in MiniMessage string representation
    private final TextColor color;
    private final Map<ObtainType, Double> chance;

    private final Set<ExcellentEnchant> enchants;

    public Tier(
        @NotNull String id,
        int priority,
        @NotNull String name,
        @NotNull TextColor color,
        @NotNull Map<ObtainType, Double> chance
    ) {
        this.id = id.toLowerCase();
        this.priority = priority;
        this.name = name;
        this.color = color;
        this.chance = chance;
        this.enchants = new HashSet<>();
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(Placeholders.TIER_ID, this.getId())
            .replace(Placeholders.TIER_NAME, this.getName())
            ;
    }

    public @NotNull String getId() {
        return this.id;
    }

    public int getPriority() {
        return this.priority;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public @NotNull TextColor getColor() {
        return this.color;
    }

    public @NotNull Map<ObtainType, Double> getChance() {
        return this.chance;
    }

    public double getChance(@NotNull ObtainType obtainType) {
        return this.getChance().getOrDefault(obtainType, 0D);
    }

    public @NotNull Set<ExcellentEnchant> getEnchants() {
        return this.enchants;
    }

    public @NotNull Set<ExcellentEnchant> getEnchants(@NotNull ObtainType obtainType) {
        return this.getEnchants(obtainType, null);
    }

    public @NotNull Set<ExcellentEnchant> getEnchants(@NotNull ObtainType obtainType, @Nullable ItemStack item) {
        Set<ExcellentEnchant> set = this.getEnchants().stream()
            .filter(enchant -> enchant.getObtainChance(obtainType) > 0)
            .filter(enchant -> item == null || enchant.canEnchantItem(item))
            .collect(Collectors.toCollection(HashSet::new));
        set.removeIf(enchant -> obtainType == ObtainType.ENCHANTING && (enchant.isTreasure() || enchant.isCursed()));
        return set;
    }
}
