package com.kaiju;

import com.kaiju.entity.GhidorahEntity;
import com.kaiju.entity.GodzillaEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/** Empty Kaiju DNA syringe: right-click a kaiju to draw its cells. */
public class KaijuSyringeItem extends Item {
    public KaijuSyringeItem(Settings s) { super(s); }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Item filled = entity instanceof GodzillaEntity ? ModItems.GODZILLA_CELL
                : entity instanceof GhidorahEntity ? ModItems.GHIDORAH_CELL : null;
        if (filled == null) return ActionResult.PASS;
        if (user.getWorld() instanceof ServerWorld w) {
            entity.damage(user.getDamageSources().magic(), Math.max(0, Math.min(15, entity.getHealth() - 1)));
            w.spawnParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), 25, 1, 2, 1, 0.1);
            w.spawnParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), 10, 1, 2, 1, 0.05);
            if (!user.isCreative()) stack.decrement(1);
            user.getInventory().offerOrDrop(new ItemStack(filled));
        }
        return ActionResult.success(user.getWorld().isClient);
    }
}
