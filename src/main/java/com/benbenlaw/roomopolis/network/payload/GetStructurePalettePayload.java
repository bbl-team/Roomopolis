package com.benbenlaw.roomopolis.network.payload;

import com.benbenlaw.Roomopolis;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public record GetStructurePalettePayload(String templateID, Map<Block, Integer> blockAmount) implements CustomPacketPayload {

    public static final Type<GetStructurePalettePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Roomopolis.MOD_ID, "structure_palette"));

    @Override
    public Type<GetStructurePalettePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, GetStructurePalettePayload> STREAM_CODEC = StreamCodec.composite(

            ByteBufCodecs.STRING_UTF8, GetStructurePalettePayload::templateID,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(Registries.BLOCK), ByteBufCodecs.INT), GetStructurePalettePayload::blockAmount,
            GetStructurePalettePayload::new
    );


}