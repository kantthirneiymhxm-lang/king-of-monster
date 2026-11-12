package com.kaiju;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ModBiomes {
    public static final RegistryKey<Biome> PINK_CAVE = RegistryKey.of(RegistryKeys.BIOME, new Identifier(KaijuMod.ID, "pink_cave"));
    public static final RegistryKey<Biome> LAVA_MOUNTAIN = RegistryKey.of(RegistryKeys.BIOME, new Identifier(KaijuMod.ID, "lava_mountain"));

    public static boolean isPinkCave(World w, BlockPos p) { return w.getBiome(p).matchesKey(PINK_CAVE); }
    public static boolean isLavaMountain(World w, BlockPos p) { return w.getBiome(p).matchesKey(LAVA_MOUNTAIN); }
}
