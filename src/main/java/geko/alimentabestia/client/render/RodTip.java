package geko.alimentabestia.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

final class RodTip {
    private static final long MAX_AGE_MS = 500L;
    private static boolean captured;
    private static long capturedAt;
    private static float screenX;
    private static float screenY;
    private static float depth;

    private RodTip() {
    }

    static void capture(Matrix4f modelMatrix, float x, float y, float z) {
        Vector3f view = modelMatrix.transformPosition(x, y, z, new Vector3f());
        RenderSystem.getModelViewMatrix().transformPosition(view);
        Vector4f clip = RenderSystem.getProjectionMatrix().transform(new Vector4f(view, 1.0f));
        if (clip.w <= 0.0f) {
            return;
        }
        screenX = clip.x / clip.w;
        screenY = clip.y / clip.w;
        depth = -view.z;
        capturedAt = Util.getMillis();
        captured = true;
    }

    static Vec3 worldAnchor(Camera camera) {
        if (!captured || Util.getMillis() - capturedAt > MAX_AGE_MS) {
            return null;
        }
        Matrix4f inverse = new Matrix4f(RenderSystem.getProjectionMatrix()).invert();
        Vector3f near = inverse.transformProject(screenX, screenY, -1.0f, new Vector3f());
        Vector3f far = inverse.transformProject(screenX, screenY, 0.0f, new Vector3f());
        if (far.z == near.z) {
            return null;
        }
        Vector3f point = near.lerp(far, (-depth - near.z) / (far.z - near.z), new Vector3f());
        camera.rotation().transform(point);
        return camera.getPosition().add(point.x, point.y, point.z);
    }
}
