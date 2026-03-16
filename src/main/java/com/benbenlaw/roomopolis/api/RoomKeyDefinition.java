package com.benbenlaw.roomopolis.api;

import com.benbenlaw.roomopolis.util.BlockTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;

public record RoomKeyDefinition(

        Identifier templateId,
        BlockTarget blockTarget,

        int heightAdjustment,
        int frontAdjustment,

        boolean consumeKey,
        boolean removeDoorArea,
        boolean sideOnlyPlacement,
        boolean topOnlyPlacement,

        boolean blocksRequired,
        boolean overrideExistingBlocks,
        boolean replaceWaterLoggedBlocks,

        int doorLeft,
        int doorRight,
        int doorUp,
        int doorDown,

        int maxHeight,

        Rotation rotation

) {}