package geko.alimentabestia.client.render;

import geko.alimentabestia.entity.FocaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FocaRenderer extends GeoEntityRenderer<FocaEntity> {
    public FocaRenderer(EntityRendererProvider.Context context) {
        super(context, new FocaModel());
        this.shadowRadius = 1.2f;
    }
}
