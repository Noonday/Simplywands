package net.bennysmith.simplywands.network;

import net.bennysmith.simplywands.item.custom.LightWand;
import net.bennysmith.simplywands.blockentity.PedestalBlockEntity;
import net.bennysmith.simplywands.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleTeleportToPedestal(TeleportToPedestalPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)
                    || !(player.level() instanceof ServerLevel level)
                    || (!player.getMainHandItem().is(ModItems.TELEPORT_WAND.get())
                    && !player.getOffhandItem().is(ModItems.TELEPORT_WAND.get()))) return;
            if (player.blockPosition().distSqr(payload.pedestalPos()) > 100 * 100) return;
            if (level.getBlockEntity(payload.pedestalPos()) instanceof PedestalBlockEntity pedestal
                    && pedestal.hasWand(ModItems.TELEPORT_WAND.get())) pedestal.teleportPlayerToStation(player);
        });
    }

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
