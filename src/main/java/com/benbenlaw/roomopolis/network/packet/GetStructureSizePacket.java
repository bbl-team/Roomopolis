package com.benbenlaw.roomopolis.network.packet;

import com.benbenlaw.caveopolis.screen.WorktableMenu;
import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class GetStructureSizePacket {
    public static final GetStructureSizePacket INSTANCE = new GetStructureSizePacket();

    public static GetStructureSizePacket get() {
        return INSTANCE;
    }

    public void handle(final GetStructureSizePayload payload, IPayloadContext context) {

        Vec3i size = payload.size();
        ResourceLocation templateID = ResourceLocation.parse(payload.templateID());
        KeyItemSizeCache.setTemplateSize(templateID, size);
        System.out.println("added template size to cache client: " + templateID + " " + size);
    }
}
