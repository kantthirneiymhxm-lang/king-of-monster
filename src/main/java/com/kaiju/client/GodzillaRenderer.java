package com.kaiju.client;

import com.kaiju.KaijuMod;
import com.kaiju.entity.GodzillaEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Model files: geo/entity/godzilla.geo.json, textures/entity/godzilla.png, animations/entity/godzilla.animation.json */
public class GodzillaRenderer extends GeoEntityRenderer<GodzillaEntity> {
    public GodzillaRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DefaultedEntityGeoModel<>(new Identifier(KaijuMod.ID, "godzilla")));
        this.shadowRadius = 3f;
    }

    @Override
    public void preRender(MatrixStack ps, GodzillaEntity e, BakedGeoModel model, VertexConsumerProvider buf, VertexConsumer vc,
                          boolean reRender, float pt, int light, int overlay, float r, float g, float b, float a) {
        float s = switch (e.getStage()) { case 0 -> 0.3f; case 1 -> 1.0f; default -> 1.5f; };
        ps.scale(s, s, s);
        super.preRender(ps, e, model, buf, vc, reRender, pt, light, overlay, r, g, b, a);
    }
}
