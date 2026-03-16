package com.benbenlaw.roomopolis.network.payload;

import com.benbenlaw.Roomopolis;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GetStructureSizePayload(String templateID, Vec3i size) implements CustomPacketPayload {


    public static final Type<GetStructureSizePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Roomopolis.MOD_ID, "structure_size"));

    @Override
    public Type<GetStructureSizePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, GetStructureSizePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GetStructureSizePayload::templateID,
            ByteBufCodecs.INT, getStructureSizePayload -> getStructureSizePayload.size().getX(),
            ByteBufCodecs.INT, getStructureSizePayload -> getStructureSizePayload.size().getY(),
            ByteBufCodecs.INT, getStructureSizePayload -> getStructureSizePayload.size().getZ(),
            (templateID, x, y, z) -> new GetStructureSizePayload(templateID, new Vec3i(x, y, z))
    );
}