package com.kaiju;

import com.kaiju.entity.GhidorahEntity;
import com.kaiju.entity.GodzillaEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public class ModEntities {
    public static final EntityType<GodzillaEntity> GODZILLA = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(KaijuMod.ID, "godzilla"),
            FabricEntityTypeBuilder.createMob().spawnGroup(SpawnGroup.CREATURE).entityFactory(GodzillaEntity::new)
                    .dimensions(EntityDimensions.fixed(5f, 10f)).fireImmune().trackRangeBlocks(256)
                    .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                            (t, w, r, p, rand) -> rand.nextInt(6) == 0 && w.getBlockState(p.down()).isOpaque())
                    .defaultAttributes(GodzillaEntity::createAttributes).build());

    public static final EntityType<GhidorahEntity> GHIDORAH = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(KaijuMod.ID, "king_ghidorah"),
            FabricEntityTypeBuilder.createMob().spawnGroup(SpawnGroup.MONSTER).entityFactory(GhidorahEntity::new)
                    .dimensions(EntityDimensions.fixed(3f, 3.5f)).fireImmune().trackRangeBlocks(256)
                    .defaultAttributes(GhidorahEntity::createAttributes).build());

    public static void init() {
        // Dinosaur Godzilla: very rare random spawn (weight 1, groups of 1)
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.CREATURE, GODZILLA, 1, 1, 1);
    }
}
