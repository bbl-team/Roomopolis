package com.benbenlaw.roomopolis.item;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyItemSizeCache {
    public static final Map<ResourceLocation, Vec3i> templateSizes = new ConcurrentHashMap<>();

    public static Vec3i getTemplateSize(ResourceLocation templateId) {
     //   System.out.println("templateSizes: " + templateSizes);
        return templateSizes.get(templateId);

    }

    public static void setTemplateSize(ResourceLocation templateId, Vec3i size) {
        templateSizes.put(templateId, size);
    }
}