package geko.alimentabestia.client.render;

import geko.alimentabestia.AlimentaBestia;
import geko.alimentabestia.entity.CeboEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CeboModel extends GeoModel<CeboEntity> {
    private static final ResourceLocation MODEL = AlimentaBestia.id("geo/cebo.geo.json");
    private static final ResourceLocation TEXTURE = AlimentaBestia.id("textures/entity/cebo.png");
    private static final ResourceLocation ANIMATION = AlimentaBestia.id("animations/cebo.animation.json");

    @Override
    public ResourceLocation getModelResource(CeboEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CeboEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CeboEntity animatable) {
        return ANIMATION;
    }
}
