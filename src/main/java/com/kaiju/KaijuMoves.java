package com.kaiju;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** The 4 moves of each armor form. Keys: Z X C V. */
public class KaijuMoves {
    private static final Map<UUID, long[]> CD = new HashMap<>();
    private static final int[] COOLDOWN = {60, 160, 300, 700};

    public static void perform(ServerPlayerEntity p, int idx) {
        if (idx < 0 || idx > 3) return;
        String kind = KaijuForm.kindOf(p);
        if (kind == null) return;
        ServerWorld w = p.getServerWorld();
        long[] cd = CD.computeIfAbsent(p.getUuid(), k -> new long[4]);
        if (w.getTime() < cd[idx]) { p.sendMessage(Text.translatable("msg.kaiju.cooldown", (cd[idx] - w.getTime()) / 20 + 1), true); return; }
        cd[idx] = w.getTime() + COOLDOWN[idx];
        if (kind.equals("godzilla")) godzilla(p, w, idx); else ghidorah(p, w, idx);
    }

    private static void godzilla(ServerPlayerEntity p, ServerWorld w, int idx) {
        switch (idx) {
            case 0 -> { // Atomic Breath
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 4f, 0.6f);
                KaijuTasks.repeat(30, i -> {
                    if (!p.isAlive()) return;
                    Vec3d dir = p.getRotationVector();
                    Vec3d from = p.getEyePos().add(dir.multiply(1.2)).add(0, -0.3, 0);
                    Vec3d end = KaijuFx.beam(w, p, from, dir, 55, KaijuFx.BLUE, ParticleTypes.ELECTRIC_SPARK, i % 5 == 0 ? 16f : 0f, true);
                    if (i % 6 == 0) w.createExplosion(p, end.x, end.y, end.z, 2.5f, true, World.ExplosionSourceType.MOB);
                });
            }
            case 1 -> { // Tail Sweep
                Vec3d c = p.getPos();
                for (int k = 0; k < 48; k++) {
                    double a = k * Math.PI / 24;
                    w.spawnParticles(ParticleTypes.SWEEP_ATTACK, c.x + Math.cos(a) * 6, c.y + 1, c.z + Math.sin(a) * 6, 1, 0, 0, 0, 0);
                    w.spawnParticles(ParticleTypes.CLOUD, c.x + Math.cos(a) * 8, c.y + 0.5, c.z + Math.sin(a) * 8, 2, 0.2, 0.2, 0.2, 0.05);
                }
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 4f, 0.4f);
                for (Entity e : w.getOtherEntities(p, new Box(c, c).expand(9), x -> x instanceof LivingEntity)) {
                    ((LivingEntity) e).damage(KaijuFx.src(p), 14f);
                    Vec3d k = e.getPos().subtract(c).normalize().multiply(2.2);
                    e.addVelocity(k.x, 0.6, k.z); e.velocityModified = true;
                }
            }
            case 2 -> // Atomic Pulse
                KaijuTasks.repeat(45, i -> {
                    Vec3d c = p.getPos();
                    KaijuFx.ring(w, c.add(0, 0.5, 0), 8 * (1 - i / 45.0), ParticleTypes.ELECTRIC_SPARK, 24);
                    w.spawnParticles(KaijuFx.BLUE, c.x, c.y + 2.5, c.z, 6, 0.4, 1.5, 0.4, 0.02);
                    if (i == 44) KaijuFx.blast(w, p, c, 9f, 22, 30f);
                });
            default -> // Thermonuclear Meltdown
                KaijuTasks.repeat(90, i -> {
                    Vec3d c = p.getPos();
                    w.spawnParticles(KaijuFx.RED, c.x, c.y + 1 + i * 0.05, c.z, 12, 1, 1.5, 1, 0.02);
                    w.spawnParticles(ParticleTypes.LAVA, c.x, c.y + 1, c.z, 3, 1, 1, 1, 0);
                    if (i % 15 == 0) KaijuFx.ring(w, c.add(0, 0.3, 0), 2 + i / 6.0, ParticleTypes.FLAME, 30);
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 20, 4, false, false));
                    if (i == 89) KaijuFx.blast(w, p, c, 16f, 35, 60f);
                });
        }
    }

    private static void ghidorah(ServerPlayerEntity p, ServerWorld w, int idx) {
        switch (idx) {
            case 0 -> // Gravity Beam
                KaijuTasks.repeat(40, i -> {
                    if (!p.isAlive()) return;
                    Vec3d dir = p.getRotationVector();
                    Vec3d from = p.getEyePos().add(dir.multiply(1.2)).add(0, -0.3, 0);
                    Vec3d end = KaijuFx.beam(w, p, from, dir, 55, KaijuFx.GOLD, ParticleTypes.END_ROD, i % 5 == 0 ? 12f : 0f, true);
                    KaijuFx.gravityField(w, p, end, 9);
                });
            case 1 -> // Lightning Storm
                KaijuTasks.repeat(100, i -> {
                    if (i % 3 != 0) return;
                    double a = w.random.nextDouble() * Math.PI * 2, d = 5 + w.random.nextDouble() * 22;
                    double x = p.getX() + Math.cos(a) * d, z = p.getZ() + Math.sin(a) * d;
                    KaijuFx.lightning(w, new Vec3d(x, w.getTopY(Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z), z), false);
                });
            case 2 -> { // Wing Gust
                Vec3d c = p.getPos(), look = p.getRotationVector().multiply(1, 0, 1).normalize();
                for (int k = 1; k < 12; k++) w.spawnParticles(ParticleTypes.CLOUD, c.x + look.x * k, c.y + 1.5, c.z + look.z * k, 12, k * 0.25, 0.8, k * 0.25, 0.3);
                w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 6f, 0.5f);
                for (Entity e : w.getOtherEntities(p, new Box(c, c).expand(14), x -> x instanceof LivingEntity)) {
                    Vec3d to = e.getPos().subtract(c).normalize();
                    if (to.dotProduct(look) < 0.3) continue;
                    ((LivingEntity) e).damage(KaijuFx.src(p), 6f);
                    e.addVelocity(look.x * 2.5, 0.9, look.z * 2.5); e.velocityModified = true;
                }
            }
            default -> { // Dive Strike
                Vec3d look = p.getRotationVector();
                p.setVelocity(look.x * 2.8, look.y * 2.8 + 0.2, look.z * 2.8); p.velocityModified = true;
                KaijuTasks.repeat(14, i -> {
                    p.fallDistance = 0;
                    Vec3d c = p.getPos();
                    w.spawnParticles(KaijuFx.GOLD, c.x, c.y + 1, c.z, 15, 0.8, 0.8, 0.8, 0.05);
                    if (i % 4 == 0) KaijuFx.lightning(w, c, false);
                    if (i == 13) KaijuFx.blast(w, p, c, 5f, 10, 20f);
                });
            }
        }
    }
}
