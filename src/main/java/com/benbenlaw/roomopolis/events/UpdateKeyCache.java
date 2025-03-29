package com.benbenlaw.roomopolis.events;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.item.KeyItemPaletteCache;
import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.network.payload.GetStructurePalettePayload;
import com.benbenlaw.roomopolis.network.payload.GetStructureSizePayload;
import com.google.common.collect.Lists;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSCommon;
import dev.latvian.mods.kubejs.bindings.event.StartupEvents;
import dev.latvian.mods.kubejs.core.ItemKJS;
import dev.latvian.mods.kubejs.ingredient.KubeJSIngredients;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.lwjgl.system.Platform;

import java.util.*;

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

                        for (StructureTemplate.StructureBlockInfo blockInfo : palette.blocks()) {
                            Block block = blockInfo.state().getBlock();
                            if (block == Blocks.AIR) continue;
                            blockCounts.put(block, blockCounts.getOrDefault(block, 0) + 1);
                        }

                        KeyItemPaletteCache.setTemplatePalette(templateId, blockCounts);
                        PacketDistributor.sendToPlayer(serverPlayer, new GetStructurePalettePayload(templateId.toString(), blockCounts));
                    }
                }
            }
        });
    }
}
