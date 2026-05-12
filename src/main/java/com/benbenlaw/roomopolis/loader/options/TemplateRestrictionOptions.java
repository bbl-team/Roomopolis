package com.benbenlaw.roomopolis.loader.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TemplateRestrictionOptions(boolean blocksRequired, boolean overrideExistingBlocks, boolean replaceWaterLoggedBlocks) {

    public static final Codec<TemplateRestrictionOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("blocks_required", false).forGetter(TemplateRestrictionOptions::blocksRequired),
            Codec.BOOL.optionalFieldOf("override_existing_blocks", false).forGetter(TemplateRestrictionOptions::overrideExistingBlocks),
            Codec.BOOL.optionalFieldOf("replace_waterlogged_blocks", false).forGetter(TemplateRestrictionOptions::replaceWaterLoggedBlocks)
    ).apply(instance, TemplateRestrictionOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplateRestrictionOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TemplateRestrictionOptions::blocksRequired,
            ByteBufCodecs.BOOL, TemplateRestrictionOptions::overrideExistingBlocks,
            ByteBufCodecs.BOOL, TemplateRestrictionOptions::replaceWaterLoggedBlocks,
            TemplateRestrictionOptions::new
    );
}