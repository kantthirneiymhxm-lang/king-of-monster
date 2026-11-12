package com.kaiju;

import com.kaiju.entity.GhidorahEntity;
import com.kaiju.entity.GodzillaEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/** Test commands (op only): /kaiju dino | godzilla | evolved | ghidorah | ghidorah3 | meteor */
public class KaijuCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((d, reg, env) -> d.register(CommandManager.literal("kaiju")
                .requires(s -> s.hasPermissionLevel(2))
                .then(CommandManager.literal("dino").executes(c -> godzilla(c.getSource(), 0)))
                .then(CommandManager.literal("godzilla").executes(c -> godzilla(c.getSource(), 1)))
                .then(CommandManager.literal("evolved").executes(c -> godzilla(c.getSource(), 2)))
                .then(CommandManager.literal("ghidorah").executes(c -> ghidorah(c.getSource(), 1)))
                .then(CommandManager.literal("ghidorah3").executes(c -> ghidorah(c.getSource(), 3)))
                .then(CommandManager.literal("meteor").executes(c -> {
                    ServerCommandSource s = c.getSource();
                    KaijuEvents.dropGhidorah(s.getWorld(), s.getPosition().add(40, 0, 0));
                    return 1;
                }))));
    }

    private static int godzilla(ServerCommandSource s, int stage) {
        ServerWorld w = s.getWorld(); Vec3d p = s.getPosition();
        GodzillaEntity g = ModEntities.GODZILLA.create(w);
        g.refreshPositionAndAngles(p.x, p.y, p.z, 0, 0);
        w.spawnEntity(g);
        if (stage > 0) g.evolveTo(stage);
        return 1;
    }

    private static int ghidorah(ServerCommandSource s, int heads) {
        ServerWorld w = s.getWorld(); Vec3d p = s.getPosition();
        GhidorahEntity g = ModEntities.GHIDORAH.create(w);
        g.refreshPositionAndAngles(p.x, p.y + 5, p.z, 0, 0);
        w.spawnEntity(g);
        if (heads == 3) g.evolve();
        return 1;
    }
}
