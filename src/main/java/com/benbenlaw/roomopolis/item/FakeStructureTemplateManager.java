package com.benbenlaw.roomopolis.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeStructureTemplateManager {

    public static final FakeStructureTemplateManager INSTANCE = new FakeStructureTemplateManager();

    private final Map<ResourceLocation, StructureTemplate> templates = new HashMap<>();

    private FakeStructureTemplateManager() {}

    public void addTemplate(ResourceLocation id, StructureTemplate template) {
        templates.put(id, template);
    }

    public Optional<StructureTemplate> get(ResourceLocation id) {
        return Optional.ofNullable(templates.get(id));
    }
}