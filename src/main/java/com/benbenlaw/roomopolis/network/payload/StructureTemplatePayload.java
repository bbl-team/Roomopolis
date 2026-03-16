package com.benbenlaw.roomopolis.network.payload;

import com.benbenlaw.Roomopolis;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.Map;

public record StructureTemplatePayload(String templateID, CompoundTag structureTemplate) implements CustomPacketPayload {

    public static final Type<StructureTemplatePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Roomopolis.MOD_ID, "template"));

    @Override
    public Type<StructureTemplatePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, StructureTemplatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, StructureTemplatePayload::templateID,
                    ByteBufCodecs.COMPOUND_TAG, StructureTemplatePayload::structureTemplate,
                    StructureTemplatePayload::new
            );


}