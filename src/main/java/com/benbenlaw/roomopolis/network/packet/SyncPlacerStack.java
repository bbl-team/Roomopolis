package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.Roomopolis;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SyncPlacerStack(ItemStack placer) implements CustomPacketPayload {

    public static final Type<SyncPlacerStack> TYPE = new Type<>(Roomopolis.identifier("update_placer_stack"));

    public static final IPayloadHandler<SyncPlacerStack> HANDLER = (packet, context) -> {

        context.player().setItemInHand(InteractionHand.MAIN_HAND, packet.placer());
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlacerStack> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, SyncPlacerStack::placer,
            SyncPlacerStack::new
    );

    @Override
    public Type<SyncPlacerStack> type() {
        return TYPE;
    }
}