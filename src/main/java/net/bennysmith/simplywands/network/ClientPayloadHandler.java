package net.bennysmith.simplywands.network;

import net.bennysmith.simplywands.blockentity.PedestalBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.bennysmith.simplywands.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.bennysmith.simplywands.client.ClientOreHighlightHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleHighlightOres(HighlightOresPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientOreHighlightHandler.highlightOres(payload.orePositions());
        });
    }
}
