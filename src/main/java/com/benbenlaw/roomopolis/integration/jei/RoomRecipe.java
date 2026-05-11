package com.benbenlaw.roomopolis.integration.jei;

import com.benbenlaw.roomopolis.loader.options.BlockTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public record RoomRecipe(
        Identifier templateId,
        ItemStack keyItem,
        Map<ItemStack, Integer> requiredItems,
        boolean requiresBlocks,
        BlockTarget blockTarget
) {}
