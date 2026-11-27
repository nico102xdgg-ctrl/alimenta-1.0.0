package geko.alimentabestia;

import com.mojang.logging.LogUtils;
import geko.alimentabestia.command.GabCommand;
import geko.alimentabestia.game.GameManager;
import geko.alimentabestia.net.ModNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AlimentaBestia.MOD_ID)
public class AlimentaBestia {
    public static final String MOD_ID = "geko_alimentabestia";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AlimentaBestia() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModSounds.register(modBus);

        ModNetwork.register();
        MinecraftForge.EVENT_BUS.register(new GabCommand());
        GameManager.registerEvents();

        LOGGER.info("Geko - Alimenta Bestia listo");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
