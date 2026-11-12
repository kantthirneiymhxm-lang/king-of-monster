package com.kaiju;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class KaijuMod implements ModInitializer {
    public static final String ID = "kaiju";
    public static final Identifier MOVE_PACKET = new Identifier(ID, "move");

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModEntities.init();
        ModItems.init();
        KaijuEvents.init();
        KaijuCommands.init();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            KaijuTasks.tick();
            if (server.getTicks() % 20 == 0) KaijuForm.tick(server);
        });
        ServerPlayNetworking.registerGlobalReceiver(MOVE_PACKET, (server, player, handler, buf, sender) -> {
            int idx = buf.readInt();
            server.execute(() -> KaijuMoves.perform(player, idx));
        });
    }
}
