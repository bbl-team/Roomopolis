package com.benbenlaw.roomopolis.loader.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Optional;

public record TemplateRestrictionOptions(boolean blocksRequired, boolean overrideExistingBlocks, boolean replaceWaterLoggedBlocks, Optional<ItemStackTemplate> itemRequiredAndConsumed, boolean showInPlacer, int placerPosition) {

    public static final Codec<TemplateRestrictionOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("blocks_required", true).forGetter(TemplateRestrictionOptions::blocksRequired),
            Codec.BOOL.optionalFieldOf("override_existing_blocks", false).forGetter(TemplateRestrictionOptions::overrideExistingBlocks),
            Codec.BOOL.optionalFieldOf("replace_waterlogged_blocks", false).forGetter(TemplateRestrictionOptions::replaceWaterLoggedBlocks),
            ItemStackTemplate.CODEC.optionalFieldOf("required_item").forGetter(TemplateRestrictionOptions::itemRequiredAndConsumed),
            Codec.BOOL.optionalFieldOf("show_in_placer", true).forGetter(TemplateRestrictionOptions::showInPlacer),
            Codec.INT.optionalFieldOf("placer_position", 1).forGetter(TemplateRestrictionOptions::placerPosition)
            ).apply(instance, TemplateRestrictionOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemplateRestrictionOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TemplateRestrictionOptions::blocksRequired,
            ByteBufCodecs.BOOL, TemplateRestrictionOptions::overrideExistingBlocks,
            ByteBufCodecs.BOOL, TemplateRestrictionOptions::replaceWaterLoggedBlocks,
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs::optional), TemplateRestrictionOptions::itemRequiredAndConsumed,
            ByteBufCodecs.BOOL, TemplateRestrictionOptions::showInPlacer,
            ByteBufCodecs.INT, TemplateRestrictionOptions::placerPosition,
            TemplateRestrictionOptions::new
    );
}