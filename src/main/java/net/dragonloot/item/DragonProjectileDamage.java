package net.dragonloot.item;

import net.minecraft.world.entity.projectile.AbstractArrow;

final class DragonProjectileDamage {

    private DragonProjectileDamage() {
    }

    static AbstractArrow apply(AbstractArrow arrow) {
        arrow.setBaseDamage(arrow.getBaseDamage() * 1.25D + 1.0D);
        return arrow;
    }
}
