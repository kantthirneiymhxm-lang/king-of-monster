package com.kaiju;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block NUKE = Registry.register(Registries.BLOCK, new Identifier(KaijuMod.ID, "nuke_block"),
            new NukeBlock(AbstractBlock.Settings.create().mapColor(MapColor.RED).strength(2f)));
    public static void init() { }
}
