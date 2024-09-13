package net.bennysmith.simplywands.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


import java.util.List;

public record HighlightOresPayload(List<BlockPos> orePositions) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("simplywands", "highlight_ores");
    public static final Type<HighlightOresPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, HighlightOresPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(2048)), // Limit to 2048 positions
            HighlightOresPayload::orePositions,
            HighlightOresPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
