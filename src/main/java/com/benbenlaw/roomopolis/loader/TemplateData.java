package com.benbenlaw.roomopolis.loader;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemplateData extends SimpleJsonResourceReloadListener<TemplateDefinition> {

    public static final Map<Identifier, TemplateDefinition> DATA = new HashMap<>();
    public static final Map<Identifier, TemplateDefinition> ACTIVE = new HashMap<>();
    public static final Map<UUID, Map<Identifier, Map<Block, Block>>> ACTIVE_PALETTES = new HashMap<>();

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
            DATA.put(jsonId, definition);
        });

        ACTIVE.clear();
        ACTIVE_PALETTES.clear();

        System.out.println("Loaded " + DATA.size() + " RoomKeyDefinitions");
    }

    public static TemplateDefinition getTemplateDefinition(Identifier id) {
        return ACTIVE.getOrDefault(id, DATA.get(id));
    }

    public static void setActiveTemplate(Identifier id, TemplateDefinition definition) {
        ACTIVE.put(id, definition);
    }

    public static void clearActiveTemplate(Identifier id) {
        ACTIVE.remove(id);
    }

    public static Map<Block, Block> getActivePalette(UUID playerUUID, Identifier templateId) {

        return ACTIVE_PALETTES
                .getOrDefault(playerUUID, Map.of())
                .getOrDefault(templateId, Map.of());
    }

    public static void setPaletteMapping(UUID playerUUID, Identifier templateId, Block from, Block to) {

        ACTIVE_PALETTES
                .computeIfAbsent(playerUUID, uuid -> new HashMap<>())
                .computeIfAbsent(templateId, id -> new HashMap<>())
                .put(from, to);
    }

    public static void clearPalette(UUID playerUUID, Identifier templateId) {

        Map<Identifier, Map<Block, Block>> playerPalettes =
                ACTIVE_PALETTES.get(playerUUID);

        if (playerPalettes == null) {
            return;
        }

        playerPalettes.remove(templateId);

        if (playerPalettes.isEmpty()) {
            ACTIVE_PALETTES.remove(playerUUID);
        }
    }
}