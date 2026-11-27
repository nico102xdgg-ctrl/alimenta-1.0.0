package geko.alimentabestia.net;

import geko.alimentabestia.client.ClientPacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public record SealFedPayload() {
    public static final SealFedPayload INSTANCE = new SealFedPayload();

    public static SealFedPayload read(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public void write(FriendlyByteBuf buf) {
    }

    public static void handle(SealFedPayload msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandler::sealFed));
        ctx.setPacketHandled(true);
    }
}
