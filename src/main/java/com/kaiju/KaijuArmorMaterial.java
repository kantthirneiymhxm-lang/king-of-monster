package com.kaiju;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum KaijuArmorMaterial implements ArmorMaterial {
    GODZILLA("godzilla"), GHIDORAH("ghidorah");

    public final String kind;
    KaijuArmorMaterial(String kind) { this.kind = kind; }

    @Override public int getDurability(ArmorItem.Type t) { return 5000; }
    @Override public int getProtection(ArmorItem.Type t) {
        return switch (t) { case HELMET -> 6; case CHESTPLATE -> 12; case LEGGINGS -> 9; case BOOTS -> 6; };
    }
    @Override public int getEnchantability() { return 20; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(Items.NETHERITE_INGOT); }
    @Override public String getName() { return KaijuMod.ID + ":" + kind; }
    @Override public float getToughness() { return 5f; }
    @Override public float getKnockbackResistance() { return 0.8f; }
}
