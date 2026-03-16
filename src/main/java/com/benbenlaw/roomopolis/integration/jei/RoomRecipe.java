package com.benbenlaw.roomopolis.integration.jei;

import com.benbenlaw.roomopolis.util.BlockTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.Optional;

public record RoomRecipe(
        Identifier templateId,
        ItemStack keyItem,
        Map<ItemStack, Integer> requiredItems,
        boolean requiresBlocks,
        BlockTarget blockTarget
) {}
