package geko.alimentabestia;

import geko.alimentabestia.block.SillaBlock;
import geko.alimentabestia.block.SillaBlockEntity;
import geko.alimentabestia.item.SillaItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AlimentaBestia.MOD_ID);
    static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AlimentaBestia.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AlimentaBestia.MOD_ID);

    public static final RegistryObject<SillaBlock> SILLA = BLOCKS.register("silla",
            () -> new SillaBlock(BlockBehaviour.Properties.of().strength(1.0f).sound(SoundType.WOOD).noOcclusion()));

    public static final RegistryObject<SillaItem> SILLA_ITEM = BLOCK_ITEMS.register("silla",
            () -> new SillaItem(SILLA.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<SillaBlockEntity>> SILLA_BLOCK_ENTITY = BLOCK_ENTITIES.register("silla",
            () -> BlockEntityType.Builder.of(SillaBlockEntity::new, SILLA.get()).build(null));

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
