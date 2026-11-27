package geko.alimentabestia.client.render;

import geko.alimentabestia.item.SillaItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SillaItemRenderer extends GeoItemRenderer<SillaItem> {
    public SillaItemRenderer() {
        super(new SillaModel<>());
    }
}
