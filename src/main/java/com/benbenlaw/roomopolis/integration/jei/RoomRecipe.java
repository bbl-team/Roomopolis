package com.benbenlaw.roomopolis.integration.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.Optional;

public record RoomRecipe(
        ResourceLocation templateId,
        ItemStack keyItem,
        Map<ItemStack, Integer> requiredItems,
        boolean requiresBlocks,
        Optional<Block> keyBlock,
        Optional<TagKey<Block>> keyBlockTag

) {}
