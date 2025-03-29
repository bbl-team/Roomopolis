package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.roomopolis.item.KeyItemPaletteCache;
import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.network.payload.GetStructurePalettePayload;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GetStructurePalettePacket {
    public static final GetStructurePalettePacket INSTANCE = new GetStructurePalettePacket();

    public static GetStructurePalettePacket get() {
        return INSTANCE;
    }

    public void handle(final GetStructurePalettePayload payload, IPayloadContext context) {
        Map<Block, Integer> blockCounts = payload.blockAmount();
        ResourceLocation templateID = ResourceLocation.parse(payload.templateID());

        KeyItemPaletteCache.setTemplatePalette(templateID, blockCounts);
        System.out.println("added palette to cache client: " + templateID + " ");


    }
}
