package com.kaiju;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Right-click: points to the nearest loaded kaiju. Ghidorah compass + sneak: calls it down from space. */
public class KaijuCompassItem extends Item {
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
    private final Class<? extends LivingEntity> target;
    private final boolean summon;

    public KaijuCompassItem(Settings s, Class<? extends LivingEntity> target, boolean summon) { super(s); this.target = target; this.summon = summon; }

    @Override
    public TypedActionResult<ItemStack> use(World w, PlayerEntity p, Hand hand) {
        ItemStack stack = p.getStackInHand(hand);
        if (!(w instanceof ServerWorld sw)) return TypedActionResult.success(stack);
        if (summon && p.isSneaking()) {
            if (p.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
            double a = w.random.nextDouble() * Math.PI * 2;
            KaijuEvents.dropGhidorah(sw, p.getPos().add(Math.cos(a) * 70, 0, Math.sin(a) * 70));
            p.getItemCooldownManager().set(this, 1200);
            if (!p.isCreative()) stack.decrement(1);
            return TypedActionResult.consume(stack);
        }
        Entity best = null; double bd = Double.MAX_VALUE;
        for (Entity e : sw.iterateEntities()) {
            if (!target.isInstance(e) || !e.isAlive()) continue;
            double d = e.squaredDistanceTo(p);
            if (d < bd) { bd = d; best = e; }
        }
        if (best == null) { p.sendMessage(Text.translatable("msg.kaiju.compass.none"), true); return TypedActionResult.fail(stack); }
        Vec3d diff = best.getPos().subtract(p.getPos());
        float targetYaw = (float) (MathHelper.atan2(-diff.x, diff.z) * 57.29578);
        float rel = MathHelper.wrapDegrees(targetYaw - p.getYaw());
        String arrow = ARROWS[(Math.round(rel / 45f) + 8) % 8];
        p.sendMessage(Text.translatable("msg.kaiju.compass.found", arrow, (int) Math.sqrt(bd)), true);
        return TypedActionResult.success(stack);
    }
}
