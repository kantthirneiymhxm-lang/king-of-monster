package com.kaiju.entity;

import com.kaiju.KaijuFx;
import com.kaiju.ModBiomes;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Falls from space with 1 head, evolves to 3 heads after staying in the lava-mountain biome. */
public class GhidorahEntity extends HostileEntity implements GeoEntity {
    public static final TrackedData<Integer> HEADS = DataTracker.registerData(GhidorahEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean falling;
    private int lavaTicks, gravCd, stormCd, gravTicks, stormTicks;
    private Vec3d stormCenter = Vec3d.ZERO;
    private double stormAngle;
    private BlockPos seek;

    public GhidorahEntity(EntityType<? extends GhidorahEntity> type, World world) {
        super(type, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.experiencePoints = 500;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 200)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.9)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 12)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 80);
    }

    @Override protected void initDataTracker() { super.initDataTracker(); dataTracker.startTracking(HEADS, 1); }
    public int getHeads() { return dataTracker.get(HEADS); }
    public boolean isFalling() { return falling; }
    public void startFalling() { falling = true; setNoGravity(false); }

    @Override protected EntityNavigation createNavigation(World w) {
        BirdNavigation n = new BirdNavigation(this, w);
        n.setCanPathThroughDoors(false); n.setCanSwim(true); n.setCanEnterOpenDoors(true);
        return n;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(5, new FlyGoal(this, 1.0));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 40f));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, GodzillaEntity.class, 5, false, false, e -> getHeads() == 3));
        targetSelector.add(3, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false, e -> getHeads() == 3));
    }

    public void evolve() {
        dataTracker.set(HEADS, 3);
        getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(900);
        getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(40);
        setHealth(getMaxHealth());
        calculateDimensions();
        if (getWorld() instanceof ServerWorld w) {
            Vec3d c = getPos();
            for (int i = 0; i < 6; i++) KaijuFx.lightning(w, c.add(w.random.nextInt(14) - 7, 0, w.random.nextInt(14) - 7), true);
            w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 3, c.z, 3, 3, 3, 3, 0);
            for (double r = 3; r < 24; r += 4) KaijuFx.ring(w, c.add(0, 0.5, 0), r, KaijuFx.GOLD, (int) (r * 5));
            w.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 10f, 0.3f);
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return getHeads() == 3 ? EntityDimensions.changing(9f, 9f) : EntityDimensions.changing(3f, 3.5f);
    }
    @Override public void onTrackedDataSet(TrackedData<?> d) { if (HEADS.equals(d)) calculateDimensions(); super.onTrackedDataSet(d); }

    @Override
    public boolean damage(DamageSource s, float a) {
        if (s.isOf(DamageTypes.LIGHTNING_BOLT) || s.isOf(DamageTypes.FALL)) return false;
        return super.damage(s, a);
    }
    @Override public void onStruckByLightning(ServerWorld w, LightningEntity l) { }
    @Override public boolean handleFallDamage(float f, float m, DamageSource s) { return false; }
    @Override public boolean isDisallowedInPeaceful() { return false; }
    @Override public boolean canImmediatelyDespawn(double d) { return false; }

    @Override
    public void tick() {
        super.tick();
        if (!(getWorld() instanceof ServerWorld w)) return;

        if (falling) {                                   // meteor entry
            setVelocity(0, -1.7, 0); velocityModified = true;
            w.spawnParticles(ParticleTypes.FLAME, getX(), getY() + 2, getZ(), 25, 1.5, 3, 1.5, 0.1);
            w.spawnParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 4, getZ(), 15, 1, 4, 1, 0.05);
            if (isOnGround() || verticalCollision) {
                falling = false; setNoGravity(true);
                KaijuFx.blast(w, this, getPos(), 7f, 25, 30f);
            }
            return;
        }
        setNoGravity(true);
        if (gravCd > 0) gravCd--;
        if (stormCd > 0) stormCd--;
        int heads = getHeads();

        if (heads == 1) {
            boolean lava = ModBiomes.isLavaMountain(w, getBlockPos());
            if (lava) {
                lavaTicks++;
                w.spawnParticles(ParticleTypes.LAVA, getX(), getY() + 1, getZ(), 3, 1, 1, 1, 0);
                w.spawnParticles(KaijuFx.GOLD, getX(), getY() + 1, getZ(), 4, 1.5, 1.5, 1.5, 0.02);
                if (lavaTicks >= 400) evolve();
            } else {
                lavaTicks = 0;
                if (age % 600 == 1) {                    // wander toward the lava mountains on its own
                    Pair<BlockPos, ?> r = w.locateBiome(b -> b.matchesKey(ModBiomes.LAVA_MOUNTAIN), getBlockPos(), 2500, 64, 64);
                    seek = r == null ? null : r.getFirst();
                }
                if (seek != null && getTarget() == null)
                    getMoveControl().moveTo(seek.getX(), Math.max(seek.getY() + 25, getY()), seek.getZ(), 0.9);
            }
            return;
        }

        LivingEntity t = getTarget();
        if (stormTicks > 0) {                            // circle + lightning storm
            stormTicks--; stormAngle += 0.12;
            getMoveControl().moveTo(stormCenter.x + Math.cos(stormAngle) * 14, stormCenter.y + 12,
                    stormCenter.z + Math.sin(stormAngle) * 14, 1.8);
            if (stormTicks % 3 == 0) {
                double a = random.nextDouble() * Math.PI * 2, d = random.nextDouble() * 26;
                double x = stormCenter.x + Math.cos(a) * d, z = stormCenter.z + Math.sin(a) * d;
                if (t != null && random.nextBoolean()) { x = t.getX() + random.nextInt(9) - 4; z = t.getZ() + random.nextInt(9) - 4; }
                KaijuFx.lightning(w, new Vec3d(x, w.getTopY(Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z), z), false);
            }
            w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 6, getZ(), 15, 5, 3, 5, 0.3);
            return;
        }

        if (t != null && t.isAlive()) {
            getLookControl().lookAt(t, 40, 40);
            double d = squaredDistanceTo(t);
            if (gravTicks == 0) {
                if (d > 20 * 20) getMoveControl().moveTo(t.getX(), t.getY() + 9, t.getZ(), 1.4);
                else getMoveControl().moveTo(getX(), t.getY() + 10, getZ(), 0.6);
                if (stormCd == 0 && d < 40 * 40 && random.nextInt(60) == 0) {
                    stormTicks = 160; stormCd = 800; stormCenter = getPos(); stormAngle = 0;
                    triggerAnim("attack", "storm");
                } else if (gravCd == 0 && d < 45 * 45) {
                    gravTicks = 80; gravCd = 260;
                    triggerAnim("attack", "gravity");
                    playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, 8f, 0.4f);
                }
            }
            if (gravTicks > 0) {                         // gravity beams from each of the 3 heads
                gravTicks--;
                Vec3d look = getRotationVector();
                Vec3d right = look.crossProduct(new Vec3d(0, 1, 0)).normalize();
                Vec3d eye = getEyePos();
                Vec3d end = t.getPos();
                for (int h = -1; h <= 1; h++) {
                    Vec3d from = eye.add(right.multiply(h * 3.0)).add(0, 1.5, 0).add(look.multiply(3));
                    Vec3d dir = t.getEyePos().subtract(from);
                    end = KaijuFx.beam(w, this, from, dir, 55, KaijuFx.GOLD, ParticleTypes.END_ROD,
                            gravTicks % 5 == 0 ? 14f : 0f, true);
                }
                KaijuFx.gravityField(w, this, end, 10);
            }
        }
    }

    @Override public void writeCustomDataToNbt(NbtCompound n) { super.writeCustomDataToNbt(n); n.putInt("Heads", getHeads()); n.putBoolean("Falling", falling); }
    @Override public void readCustomDataFromNbt(NbtCompound n) {
        super.readCustomDataFromNbt(n);
        int h = Math.max(1, n.getInt("Heads"));
        dataTracker.set(HEADS, h); falling = n.getBoolean("Falling");
        if (h == 3) { getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(900); getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(40); }
        calculateDimensions();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar c) {
        c.add(new AnimationController<>(this, "move", 5, s -> s.setAndContinue(RawAnimation.begin().thenLoop("fly"))));
        c.add(new AnimationController<GhidorahEntity>(this, "attack", 0, s -> PlayState.STOP)
                .triggerableAnim("gravity", RawAnimation.begin().then("gravity", Animation.LoopType.PLAY_ONCE))
                .triggerableAnim("storm", RawAnimation.begin().then("storm", Animation.LoopType.PLAY_ONCE)));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
