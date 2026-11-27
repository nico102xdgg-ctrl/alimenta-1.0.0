package geko.alimentabestia.client.render;

import geko.alimentabestia.block.SillaBlockEntity;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class SillaBlockRenderer extends GeoBlockRenderer<SillaBlockEntity> {
    public SillaBlockRenderer() {
        super(new SillaModel<>());
    }
}
