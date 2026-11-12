package com.kaiju;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class KaijuForm {
    public static final int FORM_TICKS = 6000; // 5 minutes
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static String kindOf(PlayerEntity p) {
        return p.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof KaijuArmorItem k ? k.kind : null;
    }

    public static void transform(ServerPlayerEntity p, String kind) {
        ServerWorld w = p.getServerWorld();
        long expire = w.getTime() + FORM_TICKS;
        Item[] set = kind.equals("godzilla") ? ModItems.GODZILLA_ARMOR : ModItems.GHIDORAH_ARMOR;
        for (int i = 0; i < 4; i++) {
            ItemStack old = p.getEquippedStack(SLOTS[i]);
            if (!old.isEmpty() && !(old.getItem() instanceof KaijuArmorItem)) p.getInventory().offerOrDrop(old.copy());
            ItemStack n = new ItemStack(set[i]);
            n.getOrCreateNbt().putLong("KaijuExpire", expire);
            n.getOrCreateNbt().putBoolean("Unbreakable", true);
            n.addEnchantment(Enchantments.BINDING_CURSE, 1);
            n.addEnchantment(Enchantments.VANISHING_CURSE, 1);
            p.equipStack(SLOTS[i], n);
        }
        applyEffects(p, kind);
        Vec3d c = p.getPos();
        KaijuFx.lightning(w, c, true);
        w.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 1, c.z, 2, 1, 1, 1, 0);
        for (double r = 1.5; r < 10; r += 2) KaijuFx.ring(w, c.add(0, 0.3, 0), r, kind.equals("godzilla") ? KaijuFx.BLUE : KaijuFx.GOLD, (int) (r * 6));
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 4f, 0.5f);
        if (kind.equals("ghidorah")) { p.getAbilities().allowFlying = true; p.sendAbilitiesUpdate(); }
    }

    private static void applyEffects(ServerPlayerEntity p, String kind) {
        boolean g = kind.equals("godzilla");
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100, g ? 3 : 2, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, g ? 3 : 2, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 100, 0, false, false));
        if (g) p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1, false, false));
        else p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 0, false, false));
    }

    /** Called once per second. */
    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            String kind = kindOf(p);
            if (kind == null) continue;
            long exp = p.getEquippedStack(EquipmentSlot.HEAD).getOrCreateNbt().getLong("KaijuExpire");
            if (p.getServerWorld().getTime() >= exp) { revert(p, kind); continue; }
            applyEffects(p, kind);
            if (kind.equals("ghidorah") && !p.getAbilities().allowFlying) { p.getAbilities().allowFlying = true; p.sendAbilitiesUpdate(); }
            ServerWorld w = p.getServerWorld();
            w.spawnParticles(kind.equals("godzilla") ? KaijuFx.BLUE : KaijuFx.GOLD, p.getX(), p.getY() + 1, p.getZ(), 6, 0.6, 1, 0.6, 0.02);
        }
    }

    public static void revert(ServerPlayerEntity p, String kind) {
        for (EquipmentSlot s : SLOTS) if (p.getEquippedStack(s).getItem() instanceof KaijuArmorItem) p.equipStack(s, ItemStack.EMPTY);
        if (kind.equals("ghidorah") && !p.isCreative() && !p.isSpectator()) {
            p.getAbilities().allowFlying = false; p.getAbilities().flying = false; p.sendAbilitiesUpdate();
        }
        p.getServerWorld().spawnParticles(ParticleTypes.POOF, p.getX(), p.getY() + 1, p.getZ(), 40, 1, 1, 1, 0.1);
    }
}
