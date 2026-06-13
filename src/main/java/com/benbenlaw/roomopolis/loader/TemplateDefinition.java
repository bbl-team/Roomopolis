package com.benbenlaw.roomopolis.loader;

import com.benbenlaw.roomopolis.loader.options.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record TemplateDefinition(
        Identifier templateId,
        String translatableName,
        BlockTarget blockTarget,
        TemplatePlacementOptions placement,
        TemplateDoorOptions door,
        TemplateRestrictionOptions restrictions,
        Map<Block, List<Block>> pallets
) {

    public static final Codec<Map<Block, List<Block>>> PALLET_CODEC =
            Codec.unboundedMap(
                    BuiltInRegistries.BLOCK.byNameCodec(),
                    BuiltInRegistries.BLOCK.byNameCodec().listOf()
            );
    public static final Codec<TemplateDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(

            Identifier.CODEC.fieldOf("template_id").forGetter(TemplateDefinition::templateId),
            Codec.STRING.fieldOf("name").forGetter(TemplateDefinition::translatableName),
            BlockTargetCodec.CODEC.optionalFieldOf("block_target", BlockTarget.fromString("*")).forGetter(TemplateDefinition::blockTarget),
            TemplatePlacementOptions.CODEC.optionalFieldOf("placement_options",
                    new TemplatePlacementOptions(0, 0, 256, Rotation.NONE, false, false)
            ).forGetter(TemplateDefinition::placement),
            TemplateDoorOptions.CODEC.optionalFieldOf(
                    "door",
                    new TemplateDoorOptions(0, 0, 0, 0)
            ).forGetter(TemplateDefinition::door),
            TemplateRestrictionOptions.CODEC.optionalFieldOf(
                    "restriction_options",
                    new TemplateRestrictionOptions(false, false, false, Optional.empty(), true, 1)
            ).forGetter(TemplateDefinition::restrictions),
            PALLET_CODEC.optionalFieldOf("pallets", Map.of()).forGetter(TemplateDefinition::pallets)

    ).apply(instance, TemplateDefinition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplateDefinition> STREAM_CODEC = StreamCodec.composite(

            Identifier.STREAM_CODEC, TemplateDefinition::templateId,
            ByteBufCodecs.STRING_UTF8, TemplateDefinition::translatableName,
            BlockTargetCodec.STREAM_CODEC, TemplateDefinition::blockTarget,
            TemplatePlacementOptions.STREAM_CODEC, TemplateDefinition::placement,
            TemplateDoorOptions.STREAM_CODEC, TemplateDefinition::door,
            TemplateRestrictionOptions.STREAM_CODEC, TemplateDefinition::restrictions,
            ByteBufCodecs.fromCodec(PALLET_CODEC), TemplateDefinition::pallets,
            TemplateDefinition::new
    );
}