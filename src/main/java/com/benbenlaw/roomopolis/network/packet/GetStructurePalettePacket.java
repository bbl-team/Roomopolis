package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.TemplatePaletteCache;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.HashMap;
import java.util.Map;

public record GetStructurePalettePacket(Identifier templateID, Map<Block, Integer> blockAmount) implements CustomPacketPayload{

    public static final CustomPacketPayload.Type<GetStructurePalettePacket> TYPE = new CustomPacketPayload.Type<>(Roomopolis.identifier("structure_palette"));

    public static final IPayloadHandler<GetStructurePalettePacket> HANDLER = (packet, context) -> {
        Map<Block, Integer> blockCounts = packet.blockAmount();
        Identifier templateID = packet.templateID();

        TemplatePaletteCache.setTemplatePalette(templateID, blockCounts);
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, GetStructurePalettePacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, GetStructurePalettePacket::templateID,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.BLOCK), ByteBufCodecs.INT), GetStructurePalettePacket::blockAmount,
            GetStructurePalettePacket::new
    );

    public CustomPacketPayload.Type<GetStructurePalettePacket> type() {
        return TYPE;
    }



}
