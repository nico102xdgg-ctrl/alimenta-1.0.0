package geko.alimentabestia;

import geko.alimentabestia.item.CanaItem;
import geko.alimentabestia.item.LinkToolItem;
import geko.alimentabestia.item.PescadoItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AlimentaBestia.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AlimentaBestia.MOD_ID);

    public static final RegistryObject<Item> PESCADO = ITEMS.register("pescado",
            () -> new PescadoItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<CanaItem> CANA = ITEMS.register("cana",
            () -> new CanaItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<LinkToolItem> LINK_TOOL = ITEMS.register("link_tool",
            () -> new LinkToolItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.geko_alimentabestia.main"))
                    .icon(() -> new ItemStack(CANA.get()))
                    .displayItems((params, output) -> {
                        output.accept(CANA.get());
                        output.accept(PESCADO.get());
                        output.accept(ModBlocks.SILLA_ITEM.get());
                        output.accept(LINK_TOOL.get());
                    })
                    .build());

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
    }
}
