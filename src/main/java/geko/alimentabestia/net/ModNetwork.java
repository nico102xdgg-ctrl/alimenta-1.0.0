package geko.alimentabestia.net;

import geko.alimentabestia.AlimentaBestia;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            AlimentaBestia.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, MinigameStatePayload.class, MinigameStatePayload::write,
                MinigameStatePayload::read, MinigameStatePayload::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, SealFedPayload.class, SealFedPayload::write,
                SealFedPayload::read, SealFedPayload::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, KeyPressPayload.class, KeyPressPayload::write,
                KeyPressPayload::read, KeyPressPayload::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendTo(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
