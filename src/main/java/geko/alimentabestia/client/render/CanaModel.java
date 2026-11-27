package geko.alimentabestia.client.render;

import geko.alimentabestia.AlimentaBestia;
import geko.alimentabestia.item.CanaItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.model.GeoModel;

public class CanaModel extends GeoModel<CanaItem> {
    private static final ResourceLocation MODEL = AlimentaBestia.id("geo/cana.geo.json");
    private static final ResourceLocation TEXTURE = AlimentaBestia.id("textures/item/cana.png");
    private static final ResourceLocation ANIMATION = AlimentaBestia.id("animations/cana.animation.json");

    private CanaRenderer renderer;

    void setRenderer(CanaRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handleAnimations(CanaItem animatable, long instanceId, AnimationState<CanaItem> animationState) {
        if (renderer == null || renderer.isFirstPerson()) {
            super.handleAnimations(animatable, instanceId, animationState);
            return;
        }
        for (CoreGeoBone bone : getAnimationProcessor().getRegisteredBones()) {
            resetBone(bone);
        }
    }

    private static void resetBone(CoreGeoBone bone) {
        var initial = bone.getInitialSnapshot();
        if (initial == null) {
            return;
        }
        bone.setPosX(initial.getOffsetX());
        bone.setPosY(initial.getOffsetY());
        bone.setPosZ(initial.getOffsetZ());
        bone.setRotX(initial.getRotX());
        bone.setRotY(initial.getRotY());
        bone.setRotZ(initial.getRotZ());
        bone.setScaleX(initial.getScaleX());
        bone.setScaleY(initial.getScaleY());
        bone.setScaleZ(initial.getScaleZ());
    }

    @Override
    public ResourceLocation getModelResource(CanaItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(CanaItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(CanaItem animatable) {
        return ANIMATION;
    }
}
