package com.kaiju;

import com.kaiju.entity.GodzillaEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Light with flint & steel (or redstone). Any dinosaur within 30 blocks evolves into Godzilla. */
public class NukeBlock extends Block {
    public NukeBlock(Settings s) { super(s); }

    @Override
    public ActionResult onUse(BlockState st, World w, BlockPos pos, PlayerEntity pl, Hand hand, BlockHitResult hit) {
        ItemStack s = pl.getStackInHand(hand);
        if (!s.isOf(Items.FLINT_AND_STEEL) && !s.isOf(Items.FIRE_CHARGE)) return ActionResult.PASS;
        if (!w.isClient) {
            detonate((ServerWorld) w, pos);
            if (!pl.isCreative()) { if (s.isOf(Items.FIRE_CHARGE)) s.decrement(1); else s.damage(1, pl, p -> p.sendToolBreakStatus(hand)); }
        }
        return ActionResult.success(w.isClient);
    }

    @Override
    public void neighborUpdate(BlockState state, World w, BlockPos pos, Block sb, BlockPos sp, boolean notify) {
        if (!w.isClient && w.isReceivingRedstonePower(pos)) detonate((ServerWorld) w, pos);
    }

    public static void detonate(ServerWorld w, BlockPos pos) {
        w.removeBlock(pos, false);
        Vec3d c = Vec3d.ofCenter(pos);
        for (GodzillaEntity g : w.getEntitiesByClass(GodzillaEntity.class, new Box(pos).expand(30), e -> e.getStage() == 0)) g.evolveTo(1);
        w.createExplosion(null, c.x, c.y, c.z, 14f, true, World.ExplosionSourceType.TNT);
        w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 3, c.z, 6, 5, 3, 5, 0);
        w.spawnParticles(ParticleTypes.FLASH, c.x, c.y + 3, c.z, 1, 0, 0, 0, 0);
        for (double r = 3; r < 40; r += 4) KaijuFx.ring(w, c.add(0, 0.5, 0), r, ParticleTypes.CLOUD, (int) (r * 5));
    }
}
