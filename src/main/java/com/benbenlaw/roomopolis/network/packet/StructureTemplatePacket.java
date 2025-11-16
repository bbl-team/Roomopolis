package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.roomopolis.network.payload.StructureTemplatePayload;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StructureTemplatePacket {
    public static final StructureTemplatePacket INSTANCE = new StructureTemplatePacket();

    public static StructureTemplatePacket get() {
        return INSTANCE;
    }

    public void handle(StructureTemplatePayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {

            // registry lookup required for load()
            HolderGetter<Block> blocks = Minecraft.getInstance()
                    .level
                    .registryAccess()
                    .lookupOrThrow(Registries.BLOCK);

            StructureTemplate template = new StructureTemplate();
            template.load(blocks, msg.structureTemplate());

            FakeStructureTemplateManager.INSTANCE.addTemplate(
                    ResourceLocation.parse(msg.templateID()),
                    template
            );

            System.out.println("Added structure template to client manager: " + msg.templateID());
        });
    }

}
