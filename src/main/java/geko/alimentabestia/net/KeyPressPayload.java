package geko.alimentabestia.net;

import geko.alimentabestia.game.GameManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record KeyPressPayload(char letter) {
    public static KeyPressPayload read(FriendlyByteBuf buf) {
        return new KeyPressPayload((char) ('a' + buf.readByte()));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(letter - 'a');
    }

    public boolean isValid() {
        return letter >= 'a' && letter <= 'z';
    }

    public static void handle(KeyPressPayload msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !msg.isValid()) {
                return;
            }
            GameManager.get().onKeyPress(player, msg.letter());
        });
        ctx.setPacketHandled(true);
    }
}
