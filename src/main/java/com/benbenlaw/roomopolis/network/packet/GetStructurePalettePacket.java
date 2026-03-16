package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.roomopolis.item.KeyItemPaletteCache;
import com.benbenlaw.roomopolis.network.payload.GetStructurePalettePayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;

public class GetStructurePalettePacket {
    public static final GetStructurePalettePacket INSTANCE = new GetStructurePalettePacket();

    public static GetStructurePalettePacket get() {
        return INSTANCE;
    }

    public void handle(final GetStructurePalettePayload payload, IPayloadContext context) {
        Map<Block, Integer> blockCounts = payload.blockAmount();
        Identifier templateID = Identifier.parse(payload.templateID());

        KeyItemPaletteCache.setTemplatePalette(templateID, blockCounts);
    }
}
