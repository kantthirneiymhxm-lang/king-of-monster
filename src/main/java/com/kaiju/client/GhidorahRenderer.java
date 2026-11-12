package com.kaiju.client;

import com.kaiju.KaijuMod;
import com.kaiju.entity.GhidorahEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Hides the two side heads while King Ghidorah still has 1 head. */
public class GhidorahRenderer extends GeoEntityRenderer<GhidorahEntity> {
    public GhidorahRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DefaultedEntityGeoModel<>(new Identifier(KaijuMod.ID, "king_ghidorah")));
        this.shadowRadius = 3f;
    }

    @Override
    public void preRender(MatrixStack ps, GhidorahEntity e, BakedGeoModel model, VertexConsumerProvider buf, VertexConsumer vc,
                          boolean reRender, float pt, int light, int overlay, float r, float g, float b, float a) {
        boolean one = e.getHeads() == 1;
        model.getBone("head_left").ifPresent(bone -> bone.setHidden(one));
        model.getBone("head_right").ifPresent(bone -> bone.setHidden(one));
        model.getBone("neck_left").ifPresent(bone -> bone.setHidden(one));
        model.getBone("neck_right").ifPresent(bone -> bone.setHidden(one));
        float s = one ? 0.5f : 1.0f;
        ps.scale(s, s, s);
        super.preRender(ps, e, model, buf, vc, reRender, pt, light, overlay, r, g, b, a);
    }
}
