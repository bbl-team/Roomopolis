package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.TemplateSizeCache;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record GetStructureSizePacket(Identifier templateID, Vec3i size) implements CustomPacketPayload {

    public static final Type<GetStructureSizePacket> TYPE = new Type<>(Roomopolis.identifier("structure_size"));

    public static final IPayloadHandler<GetStructureSizePacket> HANDLER = (packet, context) -> {

        Vec3i size = packet.size();
        Identifier templateID = packet.templateID;
        TemplateSizeCache.setTemplateSize(templateID, size);
        System.out.println("added template size to cache client: " + templateID + " " + size);
    };

    public static final StreamCodec<FriendlyByteBuf, GetStructureSizePacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, GetStructureSizePacket::templateID,
            Vec3i.STREAM_CODEC, GetStructureSizePacket::size,
            GetStructureSizePacket::new
    );

    @Override
    public Type<GetStructureSizePacket> type() {
        return TYPE;
    }
}
