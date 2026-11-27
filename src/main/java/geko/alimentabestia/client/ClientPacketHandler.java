package geko.alimentabestia.client;

import geko.alimentabestia.client.hud.CelebrationEffect;
import geko.alimentabestia.net.MinigameStatePayload;

public final class ClientPacketHandler {
    private ClientPacketHandler() {
    }

    public static void minigameState(MinigameStatePayload payload) {
        ClientMinigameState.accept(payload);
    }

    public static void sealFed() {
        CelebrationEffect.trigger();
    }
}
