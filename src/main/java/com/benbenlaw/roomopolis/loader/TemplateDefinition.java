package com.benbenlaw.roomopolis.loader;

import com.benbenlaw.roomopolis.loader.options.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;

public record TemplateDefinition(Identifier templateId, BlockTarget blockTarget, TemplatePlacementOptions placement, TemplateDoorOptions door, TemplateRestrictionOptions flags) {

    public static final Codec<TemplateDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("template_id").forGetter(TemplateDefinition::templateId),
            BlockTargetCodec.CODEC.optionalFieldOf("block_target", BlockTarget.fromString("minecraft:air")).forGetter(TemplateDefinition::blockTarget),
            TemplatePlacementOptions.CODEC.optionalFieldOf("placement",
                    new TemplatePlacementOptions(0, 0, 256, Rotation.NONE)).forGetter(TemplateDefinition::placement),
            TemplateDoorOptions.CODEC.optionalFieldOf("door", new TemplateDoorOptions(1,1,2,0)).forGetter(TemplateDefinition::door),
            TemplateRestrictionOptions.CODEC.optionalFieldOf("flags",
                            new TemplateRestrictionOptions(false, false, false, false, false))
                    .forGetter(TemplateDefinition::flags)

    ).apply(instance, TemplateDefinition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplateDefinition> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, TemplateDefinition::templateId,
            BlockTargetCodec.STREAM_CODEC, TemplateDefinition::blockTarget,
            TemplatePlacementOptions.STREAM_CODEC, TemplateDefinition::placement,
            TemplateDoorOptions.STREAM_CODEC, TemplateDefinition::door,
            TemplateRestrictionOptions.STREAM_CODEC, TemplateDefinition::flags,
            TemplateDefinition::new
    );
}