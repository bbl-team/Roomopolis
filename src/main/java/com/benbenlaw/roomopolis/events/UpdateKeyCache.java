package com.benbenlaw.roomopolis.events;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.item.KeyItemPaletteCache;
import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.network.payload.GetStructurePalettePayload;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import com.benbenlaw.roomopolis.util.RoomopolisTags;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = Roomopolis.MOD_ID)

public class UpdateKeyCache {

    @SubscribeEvent
    public static void updateKeyCache(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Objects.requireNonNull(serverPlayer.getServer()).execute(() -> {
            ServerLevel level = (ServerLevel) serverPlayer.level();
            StructureTemplateManager structureManager = level.getStructureManager();
            for (Item item : BuiltInRegistries.ITEM) {

                // System.out.println("item: " + item);
                if (item instanceof KeyItem keyItem) {
                    ResourceLocation templateId = keyItem.templateId;
                    Optional<StructureTemplate> optionalTemplate = structureManager.get(templateId);

                    optionalTemplate.ifPresent(template -> {
                        Vec3i size = template.getSize();
                        KeyItemSizeCache.setTemplateSize(templateId, size);
                        //System.out.println("added template size to cache server: " + templateId + " " + size);
                        PacketDistributor.sendToPlayer(serverPlayer, new GetStructureSizePayload(templateId.toString(), size));
                    });

                    if (optionalTemplate.isPresent()) {
                        StructureTemplate.Palette palette = optionalTemplate.get().palettes.getFirst();

                        Map<Block, Integer> blockCounts = new HashMap<>();
                        Map<Block, Integer> halfCountMap = new HashMap<>();

                        for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                            Block block = blockInfo.state().getBlock();
                            if (block == Blocks.AIR) continue;

                            if (block.builtInRegistryHolder().is(RoomopolisTags.Blocks.DOUBLE_BLOCKS)) {
                                halfCountMap.put(block, halfCountMap.getOrDefault(block, 0) + 1);
                                continue;
                            }

                            blockCounts.put(block, blockCounts.getOrDefault(block, 0) + 1);
                        }

                        // Convert half-counted blocks (e.g., doors) to full items with 0.5/block logic
                        for (Map.Entry<Block, Integer> entry : halfCountMap.entrySet()) {
                            int adjusted = (entry.getValue() + 1) / 2; // round up if odd
                            blockCounts.put(entry.getKey(), adjusted);
                        }

                        KeyItemPaletteCache.setTemplatePalette(templateId, blockCounts);
                        PacketDistributor.sendToPlayer(serverPlayer, new GetStructurePalettePayload(templateId.toString(), blockCounts));
                    }


                }
            }
        });
    }
}
