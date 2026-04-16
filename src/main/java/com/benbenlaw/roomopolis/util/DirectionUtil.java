package com.benbenlaw.roomopolis.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public class DirectionUtil {
    public static Direction adjustPosition(Direction facing, Direction direction) {
        return switch (facing) {
            case NORTH, UP, DOWN -> direction;
            case SOUTH -> direction.getOpposite();
            case EAST -> direction.getClockWise();
            case WEST -> direction.getCounterClockWise();
        };
    }

    public static Rotation getRotationFromDirection(Direction direction) {
        return switch (direction) {
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public static Rotation combineRotation(Rotation base, Rotation extra) {
        int baseDeg = getDegrees(base);
        int extraDeg = getDegrees(extra);
        int total = (baseDeg + extraDeg) % 360;

        return switch (total) {
            case 90 -> Rotation.CLOCKWISE_90;
            case 180 -> Rotation.CLOCKWISE_180;
            case 270 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static int getDegrees(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case COUNTERCLOCKWISE_90 -> 270;
            default -> 0;
        };
    }
}