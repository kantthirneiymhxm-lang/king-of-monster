package com.kaiju;

import com.kaiju.entity.GhidorahEntity;
import com.kaiju.entity.GodzillaEntity;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {
    static Item reg(String name, Item item) { return Registry.register(Registries.ITEM, new Identifier(KaijuMod.ID, name), item); }
    static FoodComponent cellFood() { return new FoodComponent.Builder().hunger(2).saturationModifier(0.3f).alwaysEdible().build(); }

    public static final Item NUKE_BLOCK = reg("nuke_block", new BlockItem(ModBlocks.NUKE, new Item.Settings()));
    public static final Item GODZILLA_COMPASS = reg("godzilla_compass", new KaijuCompassItem(new Item.Settings().maxCount(1), GodzillaEntity.class, false));
    public static final Item GHIDORAH_COMPASS = reg("ghidorah_compass", new KaijuCompassItem(new Item.Settings().maxCount(1), GhidorahEntity.class, true));
    public static final Item KAIJU_SYRINGE = reg("kaiju_syringe", new KaijuSyringeItem(new Item.Settings().maxCount(16)));
    public static final Item GODZILLA_CELL = reg("godzilla_cell", new KaijuCellItem(new Item.Settings().maxCount(16).food(cellFood()), "godzilla"));
    public static final Item GHIDORAH_CELL = reg("ghidorah_cell", new KaijuCellItem(new Item.Settings().maxCount(16).food(cellFood()), "ghidorah"));
    public static final Item NUCLEAR_SHIP_DEVICE = reg("nuclear_ship_device", new NuclearShipItem(new Item.Settings().maxCount(1)));
    public static final Item GODZILLA_SPAWN_EGG = reg("godzilla_spawn_egg", new SpawnEggItem(ModEntities.GODZILLA, 0x2f3b2f, 0x66ccff, new Item.Settings()));
    public static final Item GHIDORAH_SPAWN_EGG = reg("ghidorah_spawn_egg", new SpawnEggItem(ModEntities.GHIDORAH, 0xd4a017, 0xffe066, new Item.Settings()));

    static Item[] armor(KaijuArmorMaterial m) {
        ArmorItem.Type[] types = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};
        String[] names = {"helmet", "chestplate", "leggings", "boots"};
        Item[] out = new Item[4];
        for (int i = 0; i < 4; i++) out[i] = reg(m.kind + "_" + names[i], new KaijuArmorItem(m, types[i], new Item.Settings().maxCount(1).fireproof()));
        return out;
    }
    public static final Item[] GODZILLA_ARMOR = armor(KaijuArmorMaterial.GODZILLA);   // head, chest, legs, feet
    public static final Item[] GHIDORAH_ARMOR = armor(KaijuArmorMaterial.GHIDORAH);

    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier(KaijuMod.ID, "main"),
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.kaiju")).icon(() -> new ItemStack(GODZILLA_CELL))
                    .entries((ctx, e) -> {
                        e.add(NUKE_BLOCK); e.add(NUCLEAR_SHIP_DEVICE); e.add(GODZILLA_COMPASS); e.add(GHIDORAH_COMPASS);
                        e.add(KAIJU_SYRINGE); e.add(GODZILLA_CELL); e.add(GHIDORAH_CELL);
                        e.add(GODZILLA_SPAWN_EGG); e.add(GHIDORAH_SPAWN_EGG);
                        for (Item i : GODZILLA_ARMOR) e.add(i);
                        for (Item i : GHIDORAH_ARMOR) e.add(i);
                    }).build());

    public static void init() { }
}
