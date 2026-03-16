package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GetStructureSizePacket {
    public static final GetStructureSizePacket INSTANCE = new GetStructureSizePacket();

    public static GetStructureSizePacket get() {
        return INSTANCE;
    }

    public void handle(final GetStructureSizePayload payload, IPayloadContext context) {
        Vec3i size = payload.size();
        Identifier templateID = Identifier.parse(payload.templateID());
        KeyItemSizeCache.setTemplateSize(templateID, size);
        System.out.println("added template size to cache client: " + templateID + " " + size);
    }
}
