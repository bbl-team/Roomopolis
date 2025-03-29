package com.benbenlaw.roomopolis.item;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyItemPaletteCache {
    public static final Map<ResourceLocation, Map<Block, Integer>> templatePalettes = new ConcurrentHashMap<>();

    public static Map<Block, Integer> getTemplatePalette(ResourceLocation templateId) {
        return templatePalettes.get(templateId);
    }

    public static void setTemplatePalette(ResourceLocation templateId, Map<Block, Integer> palette) {
        templatePalettes.put(templateId, palette);
    }
}