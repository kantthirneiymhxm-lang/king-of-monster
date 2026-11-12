package com.kaiju;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

/** Shared effects used by both the mobs and the player transformation moves. */
public class KaijuFx {
    public static final ParticleEffect BLUE = new DustParticleEffect(new Vector3f(0.3f, 0.7f, 1f), 2.6f);
    public static final ParticleEffect GOLD = new DustParticleEffect(new Vector3f(1f, 0.85f, 0.2f), 2.6f);
    public static final ParticleEffect RED = new DustParticleEffect(new Vector3f(1f, 0.15f, 0.1f), 2.2f);
    public static final ParticleEffect PINK = new DustParticleEffect(new Vector3f(1f, 0.4f, 0.8f), 1.6f);

    public static DamageSource src(LivingEntity e) {
        DamageSources d = e.getDamageSources();
        return e instanceof PlayerEntity p ? d.playerAttack(p) : d.mobAttack(e);
    }

    /** Draws a beam; damages every living thing along it when dmg > 0. Returns the end point (stops at blocks). */
    public static Vec3d beam(ServerWorld w, LivingEntity source, Vec3d from, Vec3d dir, double len,
                             ParticleEffect main, ParticleEffect aux, float dmg, boolean fire) {
        dir = dir.normalize();
        Vec3d end = from;
        for (double d = 0; d < len; d += 0.7) {
            Vec3d p = from.add(dir.multiply(d));
            BlockPos bp = BlockPos.ofFloored(p);
            if (!w.getBlockState(bp).getCollisionShape(w, bp).isEmpty()) { end = p; break; }
            end = p;
            w.spawnParticles(main, p.x, p.y, p.z, 2, 0.12, 0.12, 0.12, 0.0);
            if (((int) (d / 0.7)) % 3 == 0) w.spawnParticles(aux, p.x, p.y, p.z, 1, 0.45, 0.45, 0.45, 0.03);
        }
        if (dmg > 0) {
            double total = from.distanceTo(end);
            for (double d = 0; d <= total; d += 2.0) {
                Vec3d p = from.add(dir.multiply(d));
                for (Entity e : w.getOtherEntities(source, new Box(p, p).expand(1.6 + d * 0.02),
                        x -> x instanceof LivingEntity && x.isAlive())) {
                    ((LivingEntity) e).damage(src(source), dmg);
                    if (fire) e.setOnFireFor(6);
                }
            }
        }
        return end;
    }

    public static void ring(ServerWorld w, Vec3d c, double r, ParticleEffect p, int n) {
        for (int i = 0; i < n; i++) {
            double a = i * Math.PI * 2 / n;
            w.spawnParticles(p, c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r, 1, 0, 0.1, 0, 0.05);
        }
    }

    /** Big nuclear-style blast: explosion + expanding shock rings + radial damage + knockback. */
    public static void blast(ServerWorld w, LivingEntity source, Vec3d c, float power, double radius, float dmg) {
        w.createExplosion(source, c.x, c.y, c.z, power, true, World.ExplosionSourceType.MOB);
        w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 1, c.z, 4, power * 0.3, 0.5, power * 0.3, 0);
        for (double r = 2; r <= radius; r += 3) {
            ring(w, c.add(0, 0.4, 0), r, ParticleTypes.CLOUD, (int) (r * 4));
            ring(w, c.add(0, 0.6, 0), r, ParticleTypes.FLAME, (int) (r * 3));
        }
        w.spawnParticles(ParticleTypes.FLASH, c.x, c.y + 2, c.z, 1, 0, 0, 0, 0);
        for (Entity e : w.getOtherEntities(source, new Box(c, c).expand(radius), x -> x instanceof LivingEntity)) {
            double d = e.getPos().distanceTo(c);
            float f = (float) (1 - d / radius);
            if (f <= 0) continue;
            ((LivingEntity) e).damage(src(source), dmg * f + 2);
            Vec3d k = e.getPos().subtract(c).normalize().multiply(2.5 * f);
            e.addVelocity(k.x, 0.8 * f, k.z);
            e.velocityModified = true;
        }
    }

    /** Pulls entities toward a point (King Ghidorah gravity beam). */
    public static void gravityField(ServerWorld w, LivingEntity source, Vec3d center, double radius) {
        w.spawnParticles(ParticleTypes.REVERSE_PORTAL, center.x, center.y + 1, center.z, 25, radius / 3, 1, radius / 3, 0.3);
        for (Entity e : w.getOtherEntities(source, new Box(center, center).expand(radius), x -> x instanceof LivingEntity)) {
            Vec3d to = center.subtract(e.getPos()).normalize().multiply(0.18);
            e.addVelocity(to.x, 0.1, to.z);
            e.velocityModified = true;
        }
    }

    public static void lightning(ServerWorld w, Vec3d at, boolean cosmetic) {
        LightningEntity l = EntityType.LIGHTNING_BOLT.create(w);
        if (l == null) return;
        l.refreshPositionAfterTeleport(at.x, at.y, at.z);
        l.setCosmetic(cosmetic);
        w.spawnEntity(l);
    }
}
