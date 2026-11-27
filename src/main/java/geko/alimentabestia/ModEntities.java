package geko.alimentabestia;

import geko.alimentabestia.entity.AsientoEntity;
import geko.alimentabestia.entity.CeboEntity;
import geko.alimentabestia.entity.FocaEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = AlimentaBestia.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AlimentaBestia.MOD_ID);

    public static final RegistryObject<EntityType<CeboEntity>> CEBO = ENTITIES.register("cebo",
            () -> EntityType.Builder.<CeboEntity>of(CeboEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .noSummon()
                    .clientTrackingRange(4)
                    .updateInterval(5)
                    .build("cebo"));

    public static final RegistryObject<EntityType<FocaEntity>> FOCA = ENTITIES.register("foca",
            () -> EntityType.Builder.<FocaEntity>of(FocaEntity::new, MobCategory.MISC)
                    .sized(2.0f, 1.3f)
                    .noSummon()
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("foca"));

    public static final RegistryObject<EntityType<AsientoEntity>> ASIENTO = ENTITIES.register("asiento",
            () -> EntityType.Builder.<AsientoEntity>of(AsientoEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .noSummon()
                    .noSave()
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build("asiento"));

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }

    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(FOCA.get(), FocaEntity.createAttributes().build());
    }
}
