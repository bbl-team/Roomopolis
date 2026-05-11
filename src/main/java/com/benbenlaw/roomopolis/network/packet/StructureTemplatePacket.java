package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record StructureTemplatePacket(Identifier templateID, CompoundTag structureTemplate) implements CustomPacketPayload {

    public static final Type<StructureTemplatePacket> TYPE = new Type<>(Roomopolis.identifier("template"));


    public static final IPayloadHandler<StructureTemplatePacket> HANDLER = (packet, context) -> {

        HolderGetter<Block> blocks = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.BLOCK);

        StructureTemplate template = new StructureTemplate();
        template.load(blocks, packet.structureTemplate());

        FakeStructureTemplateManager.INSTANCE.addTemplate(packet.templateID, template);

        System.out.println("Added structure template to client manager: " + packet.templateID());
    };


    public static final StreamCodec<RegistryFriendlyByteBuf, StructureTemplatePacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, StructureTemplatePacket::templateID,
            ByteBufCodecs.COMPOUND_TAG, StructureTemplatePacket::structureTemplate,
            StructureTemplatePacket::new
    );

    @Override
    public Type<StructureTemplatePacket> type() {
        return TYPE;
    }
}
