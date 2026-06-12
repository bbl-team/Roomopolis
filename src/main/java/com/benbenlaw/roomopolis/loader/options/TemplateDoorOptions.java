package com.benbenlaw.roomopolis.loader.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TemplateDoorOptions(int left, int right, int up, int down) {

    public static final Codec<TemplateDoorOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("left", 0).forGetter(TemplateDoorOptions::left),
            Codec.INT.optionalFieldOf("right", 0).forGetter(TemplateDoorOptions::right),
            Codec.INT.optionalFieldOf("up", 0).forGetter(TemplateDoorOptions::up),
            Codec.INT.optionalFieldOf("down", 0).forGetter(TemplateDoorOptions::down)
    ).apply(instance, TemplateDoorOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplateDoorOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, TemplateDoorOptions::left,
            ByteBufCodecs.INT, TemplateDoorOptions::right,
            ByteBufCodecs.INT, TemplateDoorOptions::up,
            ByteBufCodecs.INT, TemplateDoorOptions::down,
            TemplateDoorOptions::new
    );

    public boolean isValid() {
        return left + right + up + down > 0;
    }
}