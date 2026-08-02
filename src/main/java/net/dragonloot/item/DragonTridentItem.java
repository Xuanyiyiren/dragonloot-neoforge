package net.dragonloot.item;

import net.dragonloot.entity.DragonTridentEntity;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DragonTridentItem extends TridentItem {

    public DragonTridentItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (living instanceof Player player) {
            int elapsed = this.getUseDuration(stack, living) - timeLeft;
            if (elapsed >= 10) {
                float spinStrength = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
                if ((spinStrength <= 0.0F || player.isInWaterOrRain() || player.isInLava())
                        && stack.getDamageValue() < stack.getMaxDamage() - 1) {
                    Holder<SoundEvent> sound = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND)
                            .orElse(SoundEvents.TRIDENT_THROW);
                    if (spinStrength <= 0.0F) {
                        if (level.isClientSide) {
                            return;
                        }

                        int previousDamage = stack.getDamageValue();
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(living.getUsedItemHand()));
                        DragonTridentEntity tridentEntity = new DragonTridentEntity(level, player, stack);
                        tridentEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, TridentItem.SHOOT_POWER, 1.0F);
                        if (player.hasInfiniteMaterials()) {
                            tridentEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        if (!level.addFreshEntity(tridentEntity)) {
                            stack.setDamageValue(previousDamage);
                            return;
                        }

                        level.playSound(null, tridentEntity, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        if (!player.hasInfiniteMaterials()) {
                            player.getInventory().removeItem(stack);
                        }
                    } else {
                        if (!level.isClientSide) {
                            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(living.getUsedItemHand()));
                        }

                        float yaw = player.getYRot();
                        float pitch = player.getXRot();
                        float x = -Mth.sin(yaw * (float) (Math.PI / 180.0F)) * Mth.cos(pitch * (float) (Math.PI / 180.0F));
                        float y = -Mth.sin(pitch * (float) (Math.PI / 180.0F));
                        float z = Mth.cos(yaw * (float) (Math.PI / 180.0F)) * Mth.cos(pitch * (float) (Math.PI / 180.0F));
                        float magnitude = Mth.sqrt(x * x + y * y + z * z);
                        x *= spinStrength / magnitude;
                        y *= spinStrength / magnitude;
                        z *= spinStrength / magnitude;
                        player.push(x, y, z);
                        player.startAutoSpinAttack(20, TridentItem.BASE_DAMAGE, stack);
                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999F, 0.0D));
                        }

                        level.playSound(null, player, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        } else {
            float spinStrength = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
            if (spinStrength > 0.0F && !(player.isInWaterOrRain() || player.isInLava())) {
                return InteractionResultHolder.fail(stack);
            }
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
    }
}
