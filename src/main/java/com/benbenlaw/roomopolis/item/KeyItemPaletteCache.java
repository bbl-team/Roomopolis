package com.benbenlaw.roomopolis.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyItemPaletteCache {

    // Existing cache for counts (used in JEI scroll/grid)
    public static final Map<Identifier, Map<Block, Integer>> templatePalettes = new ConcurrentHashMap<>();

    public static Map<Block, Integer> getTemplatePalette(Identifier templateId) {
        return templatePalettes.get(templateId);
    }

    public static void setTemplatePalette(Identifier templateId, Map<Block, Integer> palette) {
        templatePalettes.put(templateId, palette);
    }

    // New cache for rendering positions
    public static final Map<Identifier, List<BlockPosWithState>> renderPalettes = new ConcurrentHashMap<>();

    public static List<BlockPosWithState> getRenderPalette(Identifier templateId) {
        return renderPalettes.get(templateId);
    }

    public static void setRenderPalette(Identifier templateId, List<BlockPosWithState> palette) {
        renderPalettes.put(templateId, palette);
    }

    // Record to store block + position
    public record BlockPosWithState(Block block, BlockPos pos) {}

    // ✅ Utility method to populate both caches
    public static void cacheTemplate(Identifier templateId, StructureTemplate template) {
        Map<Block, Integer> countPalette = new HashMap<>();
        List<BlockPosWithState> renderPalette = new ArrayList<>();

        template.palettes.getFirst().blocks().forEach(blockInfo -> {
            Block block = blockInfo.state().getBlock();
            if (!blockInfo.state().isAir()) {
                countPalette.put(block, countPalette.getOrDefault(block, 0) + 1);
                renderPalette.add(new BlockPosWithState(block, blockInfo.pos()));
            }
        });

        setTemplatePalette(templateId, countPalette);
        setRenderPalette(templateId, renderPalette);
    }
}