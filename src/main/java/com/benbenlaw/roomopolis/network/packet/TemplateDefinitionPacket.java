package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.loader.TemplateData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.HashMap;
import java.util.Map;

public record TemplateDefinitionPacket(Map<Identifier, TemplateDefinition> data) implements CustomPacketPayload {

    public static final Type<TemplateDefinitionPacket> TYPE = new Type<>(Roomopolis.identifier("sync_template_data"));

    public static final IPayloadHandler<TemplateDefinitionPacket> HANDLER = (packet, context) -> {

            for (var entry : packet.data.entrySet()) {
                TemplateDefinition definition = entry.getValue();
                TemplateData.addTemplateDefinition(entry.getKey(), definition);
            }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplateDefinitionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, TemplateDefinition.STREAM_CODEC),
            TemplateDefinitionPacket::data,
            TemplateDefinitionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}