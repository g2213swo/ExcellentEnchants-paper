package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.Particle;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Arrowed {

    @NotNull Arrowed getArrowImplementation();

    default @NotNull Optional<Particle> getTrailParticle() {
        return this.getArrowImplementation().getTrailParticle();
    }

    default @NotNull Optional<String> getTrailData() {
        return this.getArrowImplementation().getTrailData();
    }

    default void addTrail(@NotNull Projectile projectile) {
        this.getArrowImplementation().addTrail(projectile);
    }

    default void addData(@NotNull Projectile projectile) {
        this.getArrowImplementation().addData(projectile);
    }

    default boolean isOurProjectile(@NotNull Projectile projectile) {
        return this.getArrowImplementation().isOurProjectile(projectile);
    }
}
