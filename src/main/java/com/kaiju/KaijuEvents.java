package com.kaiju;

import com.kaiju.entity.GhidorahEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class KaijuEvents {
    public static void init() {
        // Every 5 minutes, 2% chance that King Ghidorah falls from space near a random player.
        ServerTickEvents.END_WORLD_TICK.register(w -> {
            if (w.getRegistryKey() != World.OVERWORLD || w.getTime() % 6000 != 0 || w.getPlayers().isEmpty()) return;
            if (w.random.nextFloat() < 0.02f) {
                PlayerEntity p = w.getPlayers().get(w.random.nextInt(w.getPlayers().size()));
                double a = w.random.nextDouble() * Math.PI * 2, d = 50 + w.random.nextInt(50);
                dropGhidorah(w, p.getPos().add(Math.cos(a) * d, 0, Math.sin(a) * d));
            }
        });
    }

    public static GhidorahEntity dropGhidorah(ServerWorld w, Vec3d at) {
        GhidorahEntity g = ModEntities.GHIDORAH.create(w);
        if (g == null) return null;
        g.refreshPositionAndAngles(at.x, w.getTopY() - 20, at.z, 0, 0);
        g.startFalling();
        w.spawnEntity(g);
        for (ServerPlayerEntity p : w.getPlayers()) {
            p.sendMessage(Text.translatable("msg.kaiju.meteor"), true);
            w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 10f, 0.5f);
        }
        return g;
    }
}
