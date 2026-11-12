package com.kaiju.client;

import com.kaiju.KaijuMod;
import com.kaiju.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import org.lwjgl.glfw.GLFW;

public class KaijuClient implements ClientModInitializer {
    private static final KeyBinding[] KEYS = new KeyBinding[4];

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.GODZILLA, GodzillaRenderer::new);
        EntityRendererRegistry.register(ModEntities.GHIDORAH, GhidorahRenderer::new);
        int[] codes = {GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_V};
        for (int i = 0; i < 4; i++)
            KEYS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.kaiju.move" + (i + 1), InputUtil.Type.KEYSYM, codes[i], "key.categories.kaiju"));
        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            for (int i = 0; i < 4; i++)
                while (KEYS[i].wasPressed()) {
                    PacketByteBuf b = PacketByteBufs.create();
                    b.writeInt(i);
                    ClientPlayNetworking.send(KaijuMod.MOVE_PACKET, b);
                }
        });
    }
}
