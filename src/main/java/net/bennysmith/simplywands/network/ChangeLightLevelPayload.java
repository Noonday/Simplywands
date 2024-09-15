package net.bennysmith.simplywands.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChangeLightLevelPayload(int delta) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("simplywands", "change_light_level");
    public static final Type<ChangeLightLevelPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, ChangeLightLevelPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ChangeLightLevelPayload::delta,
            ChangeLightLevelPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
