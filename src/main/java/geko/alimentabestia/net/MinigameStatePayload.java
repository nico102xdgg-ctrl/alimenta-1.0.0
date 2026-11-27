package geko.alimentabestia.net;

import geko.alimentabestia.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record MinigameStatePayload(boolean movementLocked, boolean keysActive, List<KeyView> keys) {
    public static final MinigameStatePayload INACTIVE = new MinigameStatePayload(false, false, List.of());

    public static MinigameStatePayload read(FriendlyByteBuf buf) {
        boolean movementLocked = buf.readBoolean();
        boolean keysActive = buf.readBoolean();
        List<KeyView> keys = buf.readCollection(ArrayList::new, KeyView::read);
        return new MinigameStatePayload(movementLocked, keysActive, keys);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(movementLocked);
        buf.writeBoolean(keysActive);
        buf.writeCollection(keys, KeyView::write);
    }

    public static void handle(MinigameStatePayload msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.minigameState(msg)));
        ctx.setPacketHandled(true);
    }
}
