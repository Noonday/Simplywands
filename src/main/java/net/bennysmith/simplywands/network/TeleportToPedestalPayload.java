package net.bennysmith.simplywands.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TeleportToPedestalPayload(BlockPos pedestalPos) implements CustomPacketPayload {
    public static final Type<TeleportToPedestalPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("simplywands", "teleport_to_pedestal"));
    public static final StreamCodec<ByteBuf, TeleportToPedestalPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TeleportToPedestalPayload::pedestalPos, TeleportToPedestalPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
