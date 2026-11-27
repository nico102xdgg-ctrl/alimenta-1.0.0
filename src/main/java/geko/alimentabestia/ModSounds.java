package geko.alimentabestia;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AlimentaBestia.MOD_ID);

    public static final RegistryObject<SoundEvent> TECLA = create("tecla");
    public static final RegistryObject<SoundEvent> PLAYER_PASS = create("player_pass");
    public static final RegistryObject<SoundEvent> ALERT = create("alert");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> create(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(AlimentaBestia.id(name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
