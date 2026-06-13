package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.loader.TemplateData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SyncPaletteSelection(Identifier templateId, Block from, Block to) implements CustomPacketPayload {

    public static final Type<SyncPaletteSelection> TYPE =
            new Type<>(Roomopolis.identifier("sync_palette_selection"));

    public static final IPayloadHandler<SyncPaletteSelection> HANDLER = (packet, context) -> {

        TemplateData.setPaletteMapping(context.player().getUUID(), packet.templateId(), packet.from(), packet.to());
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPaletteSelection> STREAM_CODEC =
            StreamCodec.composite(

                    Identifier.STREAM_CODEC, SyncPaletteSelection::templateId,
                    ByteBufCodecs.registry(BuiltInRegistries.BLOCK.key()),
                    SyncPaletteSelection::from,

                    ByteBufCodecs.registry(BuiltInRegistries.BLOCK.key()),
                    SyncPaletteSelection::to,

                    SyncPaletteSelection::new
            );

    @Override
    public Type<SyncPaletteSelection> type() {
        return TYPE;
    }
}