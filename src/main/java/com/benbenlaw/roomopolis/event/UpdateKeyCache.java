package com.benbenlaw.roomopolis.event;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.item.TemplatePaletteCache;
import com.benbenlaw.roomopolis.item.TemplateSizeCache;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.network.packet.*;
import com.benbenlaw.roomopolis.util.RoomopolisTags;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber(modid = Roomopolis.MOD_ID)
public class UpdateKeyCache {

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {

        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer)) return;

        Objects.requireNonNull(serverPlayer.level().getServer()).execute(() -> {

            ServerLevel level = serverPlayer.level();
            StructureTemplateManager structureManager = level.getStructureManager();

            if (TemplateData.DATA.isEmpty()) return;

            TemplateData.DATA.forEach((key, value) -> {

                Identifier templateId = value.templateId();

                TemplateDefinition def = value;

                Optional<StructureTemplate> optionalTemplate =
                        structureManager.get(def.templateId());

                if (optionalTemplate.isEmpty()) {
                    return;
                }

                StructureTemplate template = optionalTemplate.get();

                StructureTemplate.Palette palette =
                        template.palettes.getFirst();

                Map<Block, Integer> blockCounts = new HashMap<>();
                Map<Block, Integer> halfCountMap = new HashMap<>();

                for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {

                    Block block = blockInfo.state().getBlock();

                    if (block == Blocks.AIR) continue;

                    if (block.defaultBlockState().typeHolder().is(RoomopolisTags.Blocks.NOT_NEEDED_FOR_BLOCK_REQUIREMENTS)) continue;
                    if (block.defaultBlockState().typeHolder().is(RoomopolisTags.Blocks.DOUBLE_BLOCKS)) {
                        halfCountMap.put(block, halfCountMap.getOrDefault(block, 0) + 1);
                        continue;
                    }

                    blockCounts.put(block,blockCounts.getOrDefault(block, 0) + 1);
                }

                for (Map.Entry<Block, Integer> entry : halfCountMap.entrySet()) {
                    int adjusted = (entry.getValue() + 1) / 2;
                    blockCounts.put(entry.getKey(), adjusted);
                }

                //PACKETS :( TODO make this into a single packet
                TemplatePaletteCache.setTemplatePalette(templateId, blockCounts);

                PacketDistributor.sendToPlayer(serverPlayer, new GetStructurePalettePacket(templateId, blockCounts));
                Vec3i templateSize = new Vec3i(template.getSize().getX(),template.getSize().getY(),template.getSize().getZ());

                TemplateSizeCache.setTemplateSize(templateId, templateSize);
                PacketDistributor.sendToPlayer(serverPlayer, new GetStructureSizePacket(templateId, templateSize));

                CompoundTag nbt = template.save(new CompoundTag());
                PacketDistributor.sendToPlayer(serverPlayer, new StructureTemplatePacket(templateId, nbt));

                Map<Identifier, TemplateDefinition> currentTemplate = new HashMap<>();
                currentTemplate.put(templateId, def);
                PacketDistributor.sendToPlayer(serverPlayer, new TemplateDefinitionPacket(currentTemplate));

                Map<Block, List<Block>> resolvedPalette = TemplateData.getResolvedPalettes(templateId);
                PacketDistributor.sendToPlayer(serverPlayer, new SyncResolvedPalettePacket(templateId, resolvedPalette));
            });
        });
    }
}