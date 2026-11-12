package com.kaiju;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/** Filled syringe, edible: eating it transforms the player into the kaiju armor form. */
public class KaijuCellItem extends Item {
    private final String kind;
    public KaijuCellItem(Settings s, String kind) { super(s); this.kind = kind; }

    @Override
    public ItemStack finishUsing(ItemStack stack, World w, LivingEntity user) {
        ItemStack r = super.finishUsing(stack, w, user);
        if (user instanceof ServerPlayerEntity p) KaijuForm.transform(p, kind);
        return r;
    }
}
