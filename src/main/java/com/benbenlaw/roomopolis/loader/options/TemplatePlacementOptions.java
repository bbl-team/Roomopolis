package com.benbenlaw.roomopolis.loader.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Rotation;

public record TemplatePlacementOptions(int heightAdjustment, int frontAdjustment, int maxHeight, Rotation rotation) {

    public static final Codec<TemplatePlacementOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("height_adjustment", 0).forGetter(TemplatePlacementOptions::heightAdjustment),
            Codec.INT.optionalFieldOf("front_adjustment", 0).forGetter(TemplatePlacementOptions::frontAdjustment),
            Codec.INT.optionalFieldOf("max_height", 256).forGetter(TemplatePlacementOptions::maxHeight),
            Rotation.CODEC.optionalFieldOf("rotation", Rotation.NONE).forGetter(TemplatePlacementOptions::rotation)
    ).apply(instance, TemplatePlacementOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplatePlacementOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, TemplatePlacementOptions::heightAdjustment,
            ByteBufCodecs.INT, TemplatePlacementOptions::frontAdjustment,
            ByteBufCodecs.INT, TemplatePlacementOptions::maxHeight,
            Rotation.STREAM_CODEC, TemplatePlacementOptions::rotation,
            TemplatePlacementOptions::new
    );
}