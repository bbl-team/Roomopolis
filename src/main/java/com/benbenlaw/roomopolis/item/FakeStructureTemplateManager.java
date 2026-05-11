package com.benbenlaw.roomopolis.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeStructureTemplateManager {

    public static final FakeStructureTemplateManager INSTANCE = new FakeStructureTemplateManager();
    public final Map<Identifier, StructureTemplate> templates = new HashMap<>();

    private FakeStructureTemplateManager() {
    }

    public void addTemplate(Identifier id, StructureTemplate template) {
        templates.put(id, template);
    }

    public Optional<StructureTemplate> get(Identifier id) {
        return Optional.ofNullable(templates.get(id));
    }

    public boolean contains(Identifier id) {
        return templates.containsKey(id);
    }

    public void clear() {
        templates.clear();
    }

    public Map<Identifier, StructureTemplate> getAll() {
        return Map.copyOf(templates);
    }
}