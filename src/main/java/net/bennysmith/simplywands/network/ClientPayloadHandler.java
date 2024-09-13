package net.bennysmith.simplywands.network;

import net.bennysmith.simplywands.client.ClientOreHighlightHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleHighlightOres(HighlightOresPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientOreHighlightHandler.highlightOres(payload.orePositions());
        });
    }
}
