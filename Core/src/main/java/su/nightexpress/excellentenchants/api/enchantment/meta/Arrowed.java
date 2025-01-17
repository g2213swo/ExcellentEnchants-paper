package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;

import java.util.Optional;

public interface Arrowed {

    @NotNull Arrowed getArrowImplementation();

    default @NotNull Optional<SimpleParticle> getTrailParticle() {
        return this.getArrowImplementation().getTrailParticle();
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
