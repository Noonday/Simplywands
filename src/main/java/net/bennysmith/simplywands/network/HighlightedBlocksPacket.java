package net.bennysmith.simplywands.network;

import net.bennysmith.simplywands.simplywands;
import net.bennysmith.simplywands.item.custom.OreLocatorWand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class HighlightedBlocksPacket {
    private Map<BlockPos, OreLocatorWand.BlockHighlight> highlightedBlocks;

    public HighlightedBlocksPacket(Map<BlockPos, OreLocatorWand.BlockHighlight> highlightedBlocks) {
        this.highlightedBlocks = highlightedBlocks;
    }

    public static void encode(HighlightedBlocksPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.highlightedBlocks.size());
        for (Map.Entry<BlockPos, OreLocatorWand.BlockHighlight> entry : msg.highlightedBlocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeVarInt(Block.getId(entry.getValue().block.defaultBlockState()));
            buf.writeVarLong(entry.getValue().expirationTime);
        }
    }

    public static HighlightedBlocksPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<BlockPos, OreLocatorWand.BlockHighlight> highlightedBlocks = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            Block block = Block.stateById(buf.readVarInt()).getBlock();
            long expirationTime = buf.readVarLong();
            highlightedBlocks.put(pos, new OreLocatorWand.BlockHighlight(block, expirationTime));
        }
        return new HighlightedBlocksPacket(highlightedBlocks);
    }

    public static void handle(HighlightedBlocksPacket msg) {
        // Handle the packet on the server side
        // You can process the highlighted blocks here
    }

    //public static void sendToServer(Map<BlockPos, OreLocatorWand.BlockHighlight> highlightedBlocks) {
    //    simplywands.NETWORK.sendToServer(new HighlightedBlocksPacket(highlightedBlocks));
    //}
}