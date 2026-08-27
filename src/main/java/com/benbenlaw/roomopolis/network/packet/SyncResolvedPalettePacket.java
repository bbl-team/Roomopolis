package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.loader.TemplateData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncResolvedPalettePacket(Identifier templateId, Map<Block, List<Block>> resolved) implements CustomPacketPayload {

    public static final Type<SyncResolvedPalettePacket> TYPE = new Type<>(Roomopolis.identifier("sync_resolved_palette"));

    public static final IPayloadHandler<SyncResolvedPalettePacket> HANDLER = (packet, context) -> {

        TemplateData.setResolvedPalette(packet.templateId(), packet.resolved());
    };

    private static final StreamCodec<RegistryFriendlyByteBuf, List<Block>> BLOCK_LIST_CODEC =
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.registry(Registries.BLOCK));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncResolvedPalettePacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, SyncResolvedPalettePacket::templateId,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.BLOCK), BLOCK_LIST_CODEC), SyncResolvedPalettePacket::resolved,
            SyncResolvedPalettePacket::new
    );

    @Override
    public Type<SyncResolvedPalettePacket> type() {
        return TYPE;
    }
}