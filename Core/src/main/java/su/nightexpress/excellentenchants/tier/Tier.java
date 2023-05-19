package su.nightexpress.excellentenchants.tier;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.Map;

public class Tier implements Placeholder {

    private final String id;
    private final int priority;
    private final String name; // Stored in MiniMessage string representation
    private final TextColor color;
    private final Map<ObtainType, Double> chance;
    private final PlaceholderMap placeholderMap;

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
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.TIER_ID, this::getId)
            .add(Placeholders.TIER_NAME, this::getName)
        ;
    }

    @Override
    public @NotNull PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
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
}
