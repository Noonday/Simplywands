package net.bennysmith.simplywands.client;

import net.bennysmith.simplywands.Config;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientOreHighlightHandler {
    private static final List<BlockPos> highlightedOres = new CopyOnWriteArrayList<>();
    private static long highlightEndTime;

    public static void highlightOres(List<BlockPos> orePositions) {
        highlightedOres.clear();
        highlightedOres.addAll(orePositions);
        highlightEndTime = System.currentTimeMillis() + Config.highlightDurationMs;
    }

    public static boolean shouldRender() {
        return System.currentTimeMillis() < highlightEndTime;
    }

    public static List<BlockPos> getHighlightedOres() {
        return highlightedOres;
    }
}
