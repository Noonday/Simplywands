package net.bennysmith.simplywands.client;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.bennysmith.simplywands.item.custom.LightWand;
import net.bennysmith.simplywands.network.ChangeLightLevelPayload;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class WandScrollHandler {

    // Light Wand Scroll wheel handler
    @SubscribeEvent
    public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack heldItem = player.getMainHandItem();

            if (player.isShiftKeyDown() && heldItem.getItem() instanceof LightWand) {
                int delta = event.getScrollDeltaY() > 0 ? 1 : -1;
                
                // Send delta to server instead of new level
                ChangeLightLevelPayload payload = new ChangeLightLevelPayload(delta);
                PacketDistributor.sendToServer(payload);
                
                event.setCanceled(true);
            }
        }
    }
}
