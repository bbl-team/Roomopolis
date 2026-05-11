package com.benbenlaw.roomopolis.loader;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class TemplateData extends SimpleJsonResourceReloadListener<TemplateDefinition> {

    public static final Map<Identifier, TemplateDefinition> DATA = new HashMap<>();

    public TemplateData() {
        super(TemplateDefinition.CODEC, FileToIdConverter.json("templates"));
    }

    @Override
    protected Map<Identifier, TemplateDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return super.prepare(manager, profiler);
    }

    @Override
    protected void apply(
            Map<Identifier, TemplateDefinition> prepared,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        DATA.clear();

        prepared.forEach((jsonId, definition) -> {
            DATA.put(definition.templateId(), definition);
        });

        System.out.println("Loaded " + DATA.size() + " RoomKeyDefinitions");
    }

    public static TemplateDefinition getTemplateDefinition(Identifier id) {
        return DATA.get(id);
    }

    public static TemplateDefinition addTemplateDefinition(Identifier id, TemplateDefinition definition) {
        return DATA.put(id, definition);
    }
}
