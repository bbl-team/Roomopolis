package com.benbenlaw.roomopolis.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemplatePaletteCache {

    public static final Map<Identifier, Map<Block, Integer>> templatePalettes = new ConcurrentHashMap<>();

    public static Map<Block, Integer> getTemplatePalette(Identifier templateId) {
        return templatePalettes.get(templateId);
    }

    public static void setTemplatePalette(Identifier templateId, Map<Block, Integer> palette) {
        templatePalettes.put(templateId, palette);
    }

    public static final Map<Identifier, List<BlockPosWithState>> renderPalettes = new ConcurrentHashMap<>();

    public static List<BlockPosWithState> getRenderPalette(Identifier templateId) {
        return renderPalettes.get(templateId);
    }

    public static void setRenderPalette(Identifier templateId, List<BlockPosWithState> palette) {
        renderPalettes.put(templateId, palette);
    }

    // Record to store block + position
    public record BlockPosWithState(
            BlockState state,
            BlockPos pos
    ) {}

    public static void cacheTemplate(Identifier templateId, StructureTemplate template) {
        Map<Block, Integer> countPalette = new HashMap<>();
        List<BlockPosWithState> renderPalette = new ArrayList<>();

        template.palettes.getFirst().blocks().forEach(blockInfo -> {
            BlockState state = blockInfo.state();
            Block block = state.getBlock();

            if (!state.isAir()) {
                countPalette.put(block, countPalette.getOrDefault(block, 0) + 1);
                renderPalette.add(new BlockPosWithState(state, blockInfo.pos())
                );
            }
        });

        setTemplatePalette(templateId, countPalette);
        setRenderPalette(templateId, renderPalette);
    }

    public static void clear() {
        templatePalettes.clear();
        renderPalettes.clear();
    }
}