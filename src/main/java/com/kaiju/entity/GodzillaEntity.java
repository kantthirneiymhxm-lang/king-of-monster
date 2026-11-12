package com.kaiju.entity;

import com.kaiju.KaijuFx;
import com.kaiju.ModBiomes;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Stage 0 = dinosaur, 1 = Godzilla (after a nuke), 2 = evolved Godzilla (after the Nuclear Ship Device). */
public class GodzillaEntity extends HostileEntity implements GeoEntity {
    public static final TrackedData<Integer> STAGE = DataTracker.registerData(GodzillaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final int MAX_CHARGE = 1200, ATOMIC_COST = 300, EVOLVE_CHARGE = 600;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int beamCd, atomicCd, beamTicks, atomicWindup;
    private int charge = 300;

    public GodzillaEntity(EntityType<? extends GodzillaEntity> type, World world) {
        super(type, world);
        this.setStepHeight(3.0f);
        this.experiencePoints = 500;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 120)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64);
    }

    @Override protected void initDataTracker() { super.initDataTracker(); dataTracker.startTracking(STAGE, 0); }
    public int getStage() { return dataTracker.get(STAGE); }
    public int getCharge() { return charge; }
    public void setCharge(int c) { charge = c; }

    @Override
    protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 24f));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, GhidorahEntity.class, 5, false, false, e -> getStage() >= 1));
    }

    /** Applies size/health/damage for the stage. */
    public void applyStats() {
        int s = getStage();
        double hp = s == 0 ? 120 : s == 1 ? 500 : 900;
        double dmg = s == 0 ? 8 : s == 1 ? 25 : 45;
        getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(hp);
        getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(dmg);
        calculateDimensions();
    }

    public void evolveTo(int stage) {
        dataTracker.set(STAGE, stage);
        applyStats();
        setHealth(getMaxHealth());
        if (getWorld() instanceof ServerWorld w) {
            Vec3d c = getPos();
            w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 3, c.z, 3, 2, 2, 2, 0);
            w.spawnParticles(ParticleTypes.FLASH, c.x, c.y + 4, c.z, 1, 0, 0, 0, 0);
            for (double r = 3; r < 20; r += 4) KaijuFx.ring(w, c.add(0, 0.5, 0), r, ParticleTypes.SOUL_FIRE_FLAME, (int) (r * 5));
            KaijuFx.lightning(w, c, true);
            w.playSound(null, getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 8f, 0.4f);
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return switch (getStage()) {
            case 0 -> EntityDimensions.changing(2.0f, 2.6f);
            case 1 -> EntityDimensions.changing(5.0f, 10.0f);
            default -> EntityDimensions.changing(7.0f, 15.0f);
        };
    }

    @Override public void onTrackedDataSet(TrackedData<?> data) { if (STAGE.equals(data)) calculateDimensions(); super.onTrackedDataSet(data); }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) return false; // nuclear-fueled: immune to explosions
        return super.damage(source, amount);
    }

    @Override public boolean isDisallowedInPeaceful() { return false; }
    @Override public boolean canImmediatelyDespawn(double d) { return false; }
    @Override public boolean handleFallDamage(float f, float m, DamageSource s) { return false; }

    @Override
    public void tick() {
        super.tick();
        if (!(getWorld() instanceof ServerWorld w)) return;
        int stage = getStage();
        if (beamCd > 0) beamCd--;
        if (atomicCd > 0) atomicCd--;

        if (stage >= 1) {
            if (ModBiomes.isPinkCave(w, getBlockPos())) {           // charging in the pink underwater cave
                charge = Math.min(MAX_CHARGE, charge + 3);
                if (age % 20 == 0) heal(3f);
                if (age % 4 == 0) w.spawnParticles(KaijuFx.PINK, getX(), getY() + getHeight() * 0.6, getZ(), 6, 1.5, 3, 1.5, 0.02);
            } else if (age % 20 == 0) charge = Math.min(600, charge + 1);
        }

        LivingEntity t = getTarget();
        if (stage >= 1 && t != null && t.isAlive()) {
            getLookControl().lookAt(t, 30, 30);
            double d = squaredDistanceTo(t);
            if (beamTicks == 0 && atomicWindup == 0) {
                if (atomicCd == 0 && charge >= ATOMIC_COST && d < 28 * 28) {
                    atomicWindup = 50; atomicCd = 600; charge -= ATOMIC_COST;
                    triggerAnim("attack", "atomic");
                    playSound(SoundEvents.ENTITY_WARDEN_SONIC_CHARGE, 6f, 0.5f);
                } else if (beamCd == 0 && d < 50 * 50) {
                    beamTicks = 50; beamCd = 300;
                    triggerAnim("attack", "beam");
                }
            }
        }

        if (beamTicks > 0 && t != null) {
            beamTicks--;
            Vec3d eye = getEyePos();
            Vec3d dir = t.getEyePos().subtract(eye).normalize();
            Vec3d from = eye.add(dir.multiply(2.5));
            float dmg = beamTicks % 5 == 0 ? (stage >= 2 ? 40f : 22f) : 0f;
            Vec3d end = KaijuFx.beam(w, this, from, dir, 55, KaijuFx.BLUE, ParticleTypes.ELECTRIC_SPARK, dmg, true);
            if (beamTicks % 8 == 0) w.createExplosion(this, end.x, end.y, end.z, 2.5f, true, World.ExplosionSourceType.MOB);
            if (beamTicks == 49) playSound(SoundEvents.ENTITY_WARDEN_SONIC_BOOM, 8f, 0.5f);
        }

        if (atomicWindup > 0) {
            atomicWindup--;
            Vec3d c = getPos();
            double h = getHeight();
            w.spawnParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y + h, c.z, 12, 2.5, 1, 2.5, 0.3);
            w.spawnParticles(KaijuFx.BLUE, c.x, c.y + h * 0.7, c.z, 8, 2, 2, 2, 0.02);
            if (atomicWindup == 0) {
                boolean evolved = stage >= 2;
                KaijuFx.blast(w, this, c, evolved ? 12f : 8f, evolved ? 30 : 20, evolved ? 90f : 50f);
            }
        }
    }

    @Override public void writeCustomDataToNbt(NbtCompound n) { super.writeCustomDataToNbt(n); n.putInt("Stage", getStage()); n.putInt("Charge", charge); }
    @Override public void readCustomDataFromNbt(NbtCompound n) {
        super.readCustomDataFromNbt(n);
        dataTracker.set(STAGE, n.getInt("Stage")); charge = n.getInt("Charge"); applyStats();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar c) {
        c.add(new AnimationController<>(this, "move", 5, s ->
                s.setAndContinue(s.isMoving() ? RawAnimation.begin().thenLoop("walk") : RawAnimation.begin().thenLoop("idle"))));
        c.add(new AnimationController<GodzillaEntity>(this, "attack", 0, s -> PlayState.STOP)
                .triggerableAnim("beam", RawAnimation.begin().then("beam", Animation.LoopType.PLAY_ONCE))
                .triggerableAnim("atomic", RawAnimation.begin().then("atomic", Animation.LoopType.PLAY_ONCE)));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
