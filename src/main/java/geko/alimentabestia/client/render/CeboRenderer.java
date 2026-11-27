package geko.alimentabestia.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import geko.alimentabestia.entity.CeboEntity;
import geko.alimentabestia.item.CanaItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CeboRenderer extends GeoEntityRenderer<CeboEntity> {
    private static final int SEGMENTS = 16;
    private static final float BAIT_ANCHOR_Y = 0.4f;
    private static final double FIRST_PERSON_RIGHT = 0.3d;
    private static final double FIRST_PERSON_UP = 0.05d;
    private static final double FIRST_PERSON_FORWARD = 1.1d;
    private static final double THIRD_PERSON_SIDEWAYS = 0.35d;
    private static final double THIRD_PERSON_FORWARDS = 0.8d;
    private static final double THIRD_PERSON_DOWN = 0.45d;

    public CeboRenderer(EntityRendererProvider.Context context) {
        super(context, new CeboModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(CeboEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        Player owner = entity.getPlayerOwner();
        if (owner != null) {
            renderLine(entity, owner, partialTick, poseStack, bufferSource);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderLine(CeboEntity entity, Player owner, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
        Vec3 hand = handPosition(owner, partialTick);
        Vec3 bait = entity.getPosition(partialTick).add(0.0d, 0.4000000059604645d, 0.0d);
        float dx = (float) (hand.x - bait.x);
        float dy = (float) (hand.y - bait.y);
        float dz = (float) (hand.z - bait.z);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lineStrip());
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i <= SEGMENTS; i++) {
            stringVertex(dx, dy, dz, consumer, pose, (float) i / SEGMENTS, (float) (i + 1) / SEGMENTS);
        }
    }

    private Vec3 handPosition(Player player, float partialTick) {
        int side = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        if (!(player.getMainHandItem().getItem() instanceof CanaItem)) {
            side = -side;
        }
        if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
            return firstPersonAnchor(side);
        }
        float bodyYaw = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot) * 0.017453292f;
        double sin = Mth.sin(bodyYaw);
        double cos = Mth.cos(bodyYaw);
        double scale = 1.0d;
        double sideways = side * THIRD_PERSON_SIDEWAYS * scale;
        double forwards = THIRD_PERSON_FORWARDS * scale;
        float crouch = player.isCrouching() ? -0.1875f : 0.0f;
        return player.getEyePosition(partialTick).add(
                -cos * sideways - sin * forwards,
                crouch - THIRD_PERSON_DOWN * scale,
                -sin * sideways + cos * forwards);
    }

    private Vec3 firstPersonAnchor(int side) {
        Camera camera = this.entityRenderDispatcher.camera;
        Vec3 tip = RodTip.worldAnchor(camera);
        if (tip != null) {
            return tip;
        }
        Vector3f look = camera.getLookVector();
        Vector3f up = camera.getUpVector();
        Vector3f left = camera.getLeftVector();
        return camera.getPosition()
                .add(look.x() * FIRST_PERSON_FORWARD, look.y() * FIRST_PERSON_FORWARD, look.z() * FIRST_PERSON_FORWARD)
                .add(up.x() * FIRST_PERSON_UP, up.y() * FIRST_PERSON_UP, up.z() * FIRST_PERSON_UP)
                .subtract(left.x() * side * FIRST_PERSON_RIGHT, left.y() * side * FIRST_PERSON_RIGHT, left.z() * side * FIRST_PERSON_RIGHT);
    }

    private static void stringVertex(float x, float y, float z, VertexConsumer consumer, PoseStack.Pose pose,
                                     float from, float to) {
        float x0 = x * from;
        float y0 = y * (from * from + from) * 0.5f + BAIT_ANCHOR_Y;
        float z0 = z * from;
        float nx = x * to - x0;
        float ny = y * (to * to + to) * 0.5f + BAIT_ANCHOR_Y - y0;
        float nz = z * to - z0;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        consumer.vertex(pose.pose(), x0, y0, z0)
                .color(0, 0, 0, 255)
                .normal(pose.normal(), nx / length, ny / length, nz / length)
                .endVertex();
    }
}
