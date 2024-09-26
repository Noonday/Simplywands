package net.bennysmith.simplywands.init;

import org.lwjgl.glfw.GLFW;

import net.bennysmith.simplywands.item.custom.MagneticWand;
import net.bennysmith.simplywands.item.custom.FoodWand;
import net.bennysmith.simplywands.network.FoodWandToggleKeyMessage;
import net.bennysmith.simplywands.network.MagneticWandToggleKeyMessage;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class SimplywandsModKeyMappings {
    public static final KeyMapping FOOD_WAND_TOGGLE_KEY = new KeyMapping("key.simplywands.food_wand_toggle_key", GLFW.GLFW_KEY_UNKNOWN, "key.categories.simplywands");
    public static final KeyMapping MAGNETIC_WAND_TOGGLE_KEY = new KeyMapping("key.simplywands.magnetic_wand_toggle_key", GLFW.GLFW_KEY_UNKNOWN, "key.categories.simplywands");

    /**
     * Registers the key mappings for the mod.
     * @param event The event to register key mappings.
     */
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FOOD_WAND_TOGGLE_KEY);
        event.register(MAGNETIC_WAND_TOGGLE_KEY);
    }

    @EventBusSubscriber({Dist.CLIENT})
    public static class KeyEventListener {
        /**
         * Handles the client tick event to check for key presses.
         * @param event The client tick event.
         */
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            
            if (minecraft.screen == null && player != null) {
                if (FOOD_WAND_TOGGLE_KEY.consumeClick()) {
                    toggleWand(player, FoodWand.class, new FoodWandToggleKeyMessage(0, 0));
                }
                
                if (MAGNETIC_WAND_TOGGLE_KEY.consumeClick()) {
                    toggleWand(player, MagneticWand.class, new MagneticWandToggleKeyMessage(0, 0));
                }
            }
        }

        /**
         * Toggles the active state of the specified wand for the player.
         * @param player The player holding the wand.
         * @param wandClass The class of the wand to toggle.
         * @param message The message to send to the server.
         * @param <T> The type of the wand.
         */
        private static <T extends net.minecraft.world.item.Item> void toggleWand(Player player, Class<T> wandClass, net.minecraft.network.protocol.common.custom.CustomPacketPayload message) {
            for (ItemStack stack : player.getInventory().items) {
                if (wandClass.isInstance(stack.getItem())) {
                    if (wandClass == FoodWand.class) {
                        ((FoodWand) stack.getItem()).toggleActive(player.level(), player);
                    } else if (wandClass == MagneticWand.class) {
                        ((MagneticWand) stack.getItem()).toggleActive(player.level(), player);
                    }
                    PacketDistributor.sendToServer(message);
                    break;
                }
            }
        }
    }
}