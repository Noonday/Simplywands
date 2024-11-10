package net.bennysmith.simplywands.network;

import net.bennysmith.simplywands.simplywands;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record FoodWandToggleKeyMessage(int eventType, int pressedms) implements CustomPacketPayload {
	public static final Type<FoodWandToggleKeyMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(simplywands.MOD_ID, "key_food_wand_toggle_key"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FoodWandToggleKeyMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, FoodWandToggleKeyMessage message) -> {
		buffer.writeInt(message.eventType);
		buffer.writeInt(message.pressedms);
	}, (RegistryFriendlyByteBuf buffer) -> new FoodWandToggleKeyMessage(buffer.readInt(), buffer.readInt()));

	/**
	 * Returns the type of the message.
	 * @return The type of the message.
	 */
	@Override
	public Type<FoodWandToggleKeyMessage> type() {
		return TYPE;
	}

	/**
	 * Handles the data received in the message.
	 * @param message The message received.
	 * @param context The context of the payload.
	 */
	public static void handleData(final FoodWandToggleKeyMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> {
			}).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	/**
	 * Registers the message type and codec.
	 * @param event The setup event.
	 */
	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		simplywands.addNetworkMessage(FoodWandToggleKeyMessage.TYPE, FoodWandToggleKeyMessage.STREAM_CODEC, FoodWandToggleKeyMessage::handleData);
	}
}