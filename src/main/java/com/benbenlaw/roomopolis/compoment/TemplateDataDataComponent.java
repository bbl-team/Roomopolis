package com.benbenlaw.roomopolis.compoment;

import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public record TemplateDataDataComponent(Map<Identifier, TemplateDefinition> templateData) {
    public static TemplateDataDataComponent fromTemplateData() {
        return new TemplateDataDataComponent(TemplateData.DATA);
    }

    public static final Codec<TemplateDataDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, TemplateDefinition.CODEC)
                    .fieldOf("template_data")
                    .forGetter(TemplateDataDataComponent::templateData)
            ).apply(instance, TemplateDataDataComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplateDataDataComponent> STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, TemplateDefinition.STREAM_CODEC)
                    .map(TemplateDataDataComponent::new, templateDataDataComponent -> new HashMap<>(templateDataDataComponent.templateData()))
                    .cast();
}
