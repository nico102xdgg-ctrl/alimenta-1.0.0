package geko.alimentabestia.client.render;

import geko.alimentabestia.AlimentaBestia;
import geko.alimentabestia.entity.FocaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FocaModel extends GeoModel<FocaEntity> {
    private static final ResourceLocation MODEL = AlimentaBestia.id("geo/foca.geo.json");
    private static final ResourceLocation TEXTURE = AlimentaBestia.id("textures/entity/foca.png");
    private static final ResourceLocation ANIMATION = AlimentaBestia.id("animations/foca.animation.json");

    @Override
    public ResourceLocation getModelResource(FocaEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FocaEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FocaEntity animatable) {
        return ANIMATION;
    }
}
