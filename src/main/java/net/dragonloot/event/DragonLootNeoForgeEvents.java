package net.dragonloot.event;

import java.util.List;
import net.dragonloot.init.ConfigInit;
import net.dragonloot.init.ItemInit;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class DragonLootNeoForgeEvents {

    private DragonLootNeoForgeEvents() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled() || event.getEntity().level().isClientSide() || !(event.getEntity() instanceof EnderDragon dragon)) {
            return;
        }

        AABB box = new AABB(dragon.blockPosition()).inflate(128.0D);
        List<Player> players = dragon.level().getEntitiesOfClass(Player.class, box, player -> player.isAlive() && !player.isSpectator());
        int scaleCount = ConfigInit.CONFIG.scale_minimum_drop_amount;

        for (Player ignored : players) {
            for (int i = 0; i < ConfigInit.CONFIG.additional_scales_per_player; i++) {
                if (dragon.level().random.nextFloat() < ConfigInit.CONFIG.additional_scale_drop_chance) {
                    scaleCount++;
                }
            }
        }

        ItemStack scaleStack = new ItemStack(ItemInit.DRAGON_SCALE_ITEM.get());
        int maxStackSize = scaleStack.getMaxStackSize();
        while (scaleCount > 0) {
            int stackSize = Math.min(scaleCount, maxStackSize);
            dragon.spawnAtLocation(scaleStack.copyWithCount(stackSize));
            scaleCount -= stackSize;
        }
    }
}
