package com.benbenlaw.roomopolis.loader;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.*;

public class TemplateData extends SimpleJsonResourceReloadListener<TemplateDefinition> {

    public static final Map<Identifier, TemplateDefinition> DATA = new HashMap<>();
    public static final Map<Identifier, TemplateDefinition> ACTIVE = new HashMap<>();
    public static final Map<UUID, Map<Identifier, Map<Block, Block>>> ACTIVE_PALETTES = new HashMap<>();

    public static final Map<Identifier, Map<Block, List<Block>>> RESOLVED_PALETTES = new HashMap<>();

    public TemplateData() {
        super(TemplateDefinition.CODEC, FileToIdConverter.json("templates"));
    }

    @Override
    protected Map<Identifier, TemplateDefinition> prepare(ResourceManager manager, ProfilerFiller profiler) {
        return super.prepare(manager, profiler);
    }

    @Override
    protected void apply(Map<Identifier, TemplateDefinition> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        RESOLVED_PALETTES.clear();
        prepared.forEach((jsonId, definition) -> {
            DATA.put(jsonId, definition);
            RESOLVED_PALETTES.put(jsonId, resolvePalettes(definition.palettes()));
        });
        ACTIVE.clear();
        ACTIVE_PALETTES.clear();
        System.out.println("Loaded " + DATA.size() + " RoomKeyDefinitions");
    }

    private static Map<Block, List<Block>> resolvePalettes(Map<String, List<String>> raw) {
        Map<Block, List<Block>> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
            List<Block> sources = resolveEntry(entry.getKey());
            List<Block> targets = entry.getValue().stream()
                    .flatMap(s -> resolveEntry(s).stream())
                    .toList();
            for (Block source : sources) {
                resolved.put(source, targets);
            }
        }
        return resolved;
    }

    private static List<Block> resolveEntry(String entry) {
        if (entry.startsWith("#")) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(entry.substring(1)));
            List<Block> blocks = new ArrayList<>();
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                blocks.add(holder.value());
            }
            return blocks;
        }
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(entry));
        return block == Blocks.AIR ? List.of() : List.of(block);
    }

    public static Map<Block, List<Block>> getResolvedPalettes(Identifier templateId) {
        return RESOLVED_PALETTES.getOrDefault(templateId, Map.of());
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

    public static Map<Identifier, TemplateDefinition> getAllTemplates() {
        Map<Identifier, TemplateDefinition> merged = new HashMap<>(DATA);
        merged.putAll(ACTIVE);
        return merged;
    }

    public static void setResolvedPalette(Identifier templateId, Map<Block, List<Block>> resolved) {
        RESOLVED_PALETTES.put(templateId, resolved);
    }

    public static void onTagsUpdated(TagsUpdatedEvent event) {
        RESOLVED_PALETTES.clear();
        for (Map.Entry<Identifier, TemplateDefinition> entry : DATA.entrySet()) {
            RESOLVED_PALETTES.put(entry.getKey(), resolvePalettes(entry.getValue().palettes()));
        }
    }
}