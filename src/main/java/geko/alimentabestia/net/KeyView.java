package geko.alimentabestia.net;

import net.minecraft.network.FriendlyByteBuf;

public record KeyView(char letter, float x, float y) {
    public static void write(FriendlyByteBuf buf, KeyView view) {
        buf.writeByte(view.letter - 'a');
        buf.writeFloat(view.x);
        buf.writeFloat(view.y);
    }

    public static KeyView read(FriendlyByteBuf buf) {
        return new KeyView((char) ('a' + buf.readByte()), buf.readFloat(), buf.readFloat());
    }
}
