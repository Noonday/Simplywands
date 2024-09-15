package net.bennysmith.simplywands.network;

import net.bennysmith.simplywands.item.custom.LightWand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleChangeLightLevel(ChangeLightLevelPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ItemStack heldItem = player.getMainHandItem();

            if (heldItem.getItem() instanceof LightWand) {
                LightWand wand = (LightWand) heldItem.getItem();
                wand.handleScrollServer(player, payload.delta());
            }
        });
    }
}
