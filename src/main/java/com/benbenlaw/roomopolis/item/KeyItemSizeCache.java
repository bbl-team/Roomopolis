package com.benbenlaw.roomopolis.item;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyItemSizeCache {
    public static final Map<Identifier, Vec3i> templateSizes = new ConcurrentHashMap<>();

    public static Vec3i getTemplateSize(Identifier templateId) {
     //   System.out.println("templateSizes: " + templateSizes);
        return templateSizes.get(templateId);

    }

    public static void setTemplateSize(Identifier templateId, Vec3i size) {
        templateSizes.put(templateId, size);
    }
}