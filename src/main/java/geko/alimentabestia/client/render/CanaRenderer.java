package geko.alimentabestia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import geko.alimentabestia.item.CanaItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CanaRenderer extends GeoItemRenderer<CanaItem> {
    private static final String ARM_BONE = "RightArm";
    private static final String ROD_BONE = "cana";
    private static final float ROD_TIP_X = 0.03125f;
    private static final float ROD_TIP_Y = 0.34375f;
    private static final float ROD_TIP_Z = -0.875f;

    private static final double ITEM_OFFSET_Z = 0.55d;
    private static final float ITEM_ROTATION_X = -30.0f;
    private static final float ITEM_ROTATION_Y = -1.0f;
    private static final float ITEM_PITCH = -30.0f;
    private static final double PITCH_PIVOT_X = -0.03d;
    private static final double PITCH_PIVOT_Y = -1.68d;
    private static final double PITCH_PIVOT_Z = 1.2d;
    private static final double ARM_OFFSET_X = 0.35d;
    private static final double ARM_OFFSET_Y = 0.3d;
    private static final float ARM_SCALE = 1.5f;
    private static final long THIRD_PERSON_SALT = -7046029254386353131L;
    private static final int FULL_BRIGHT = 15728880;

    public CanaRenderer() {
        super(new CanaModel());
        ((CanaModel) getGeoModel()).setRenderer(this);
    }

    public boolean isFirstPerson() {
        return this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    
    @Override
    public long getInstanceId(CanaItem animatable) {
        long id = super.getInstanceId(animatable);
        return isFirstPerson() ? id : id ^ THIRD_PERSON_SALT;
    }

    @Override
    public void preRender(PoseStack poseStack, CanaItem animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);
        if (!isReRender && isFirstPerson()) {
            applyFirstPersonItemTransform(poseStack);
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, CanaItem animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                  float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        boolean firstPerson = isFirstPerson();
        int light = firstPerson ? FULL_BRIGHT : packedLight;
        if (ARM_BONE.equals(bone.getName())) {
            if (firstPerson) {
                renderArm(poseStack, animatable, bone, bufferSource, isReRender, partialTick, light, packedOverlay,
                        red, green, blue, alpha);
            }
            return;
        }
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                partialTick, light, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void renderCubesOfBone(PoseStack poseStack, GeoBone bone, VertexConsumer buffer, int packedLight,
                                  int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        if (isFirstPerson() && ROD_BONE.equals(bone.getName())) {
            RodTip.capture(poseStack.last().pose(), ROD_TIP_X, ROD_TIP_Y, ROD_TIP_Z);
        }
    }

    private void renderArm(PoseStack poseStack, CanaItem animatable, GeoBone bone, MultiBufferSource bufferSource,
                           boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                           float red, float green, float blue, float alpha) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        RenderType skin = RenderType.entityCutoutNoCull(player.getSkinTextureLocation());
        poseStack.pushPose();
        applyFirstPersonArmTransform(poseStack);
        float sx = bone.getScaleX();
        float sy = bone.getScaleY();
        float sz = bone.getScaleZ();
        bone.setScaleX(ARM_SCALE);
        bone.setScaleY(ARM_SCALE);
        bone.setScaleZ(ARM_SCALE);
        super.renderRecursively(poseStack, animatable, bone, skin, bufferSource, bufferSource.getBuffer(skin),
                isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        bone.setScaleX(sx);
        bone.setScaleY(sy);
        bone.setScaleZ(sz);
        poseStack.popPose();
    }

    private void applyFirstPersonItemTransform(PoseStack poseStack) {
        int side = firstPersonSide();
        poseStack.translate(0.0d, 0.0d, ITEM_OFFSET_Z);
        rotate(poseStack, ITEM_ROTATION_X, ITEM_ROTATION_Y * side, 0.0f);
        pitchAroundGrip(poseStack, side);
        poseStack.scale(1.0f, 1.0f, 1.0f);
    }

    private void pitchAroundGrip(PoseStack poseStack, int side) {
        poseStack.translate(PITCH_PIVOT_X * side, PITCH_PIVOT_Y, PITCH_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotationDegrees(ITEM_PITCH));
        poseStack.translate(-PITCH_PIVOT_X * side, -PITCH_PIVOT_Y, -PITCH_PIVOT_Z);
    }

    private void applyFirstPersonArmTransform(PoseStack poseStack) {
        int side = firstPersonSide();
        poseStack.translate(ARM_OFFSET_X * side, ARM_OFFSET_Y, 0.0d);
    }

    private int firstPersonSide() {
        return this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -1 : 1;
    }

    private static void rotate(PoseStack poseStack, float x, float y, float z) {
        if (z != 0.0f) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(z));
        }
        if (y != 0.0f) {
            poseStack.mulPose(Axis.YP.rotationDegrees(y));
        }
        if (x != 0.0f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(x));
        }
    }
}
