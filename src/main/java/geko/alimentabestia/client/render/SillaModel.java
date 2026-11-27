package geko.alimentabestia.client.render;

import geko.alimentabestia.AlimentaBestia;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class SillaModel<T extends GeoAnimatable> extends GeoModel<T> {
    private static final ResourceLocation MODEL = AlimentaBestia.id("geo/silla.geo.json");
    private static final ResourceLocation TEXTURE = AlimentaBestia.id("textures/block/silla.png");
    private static final ResourceLocation ANIMATION = AlimentaBestia.id("animations/silla.animation.json");

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return ANIMATION;
    }
}
