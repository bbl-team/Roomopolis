package com.benbenlaw.roomopolis.renderer;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public record GuiStructureRenderState(Vec3i size, float rotationTime, Rotation facingRotation, @Nullable BlockPos highlightPos, Map<BlockPos, BlockEntity> blockEntityCache,
                                      Set<BlockEntity> erroredBlockEntities, RandomSource randomSource, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea,
                                      @Nullable ScreenRectangle bounds, Identifier templateId, float inViewScale) implements PictureInPictureRenderState {

    public GuiStructureRenderState(Vec3i size, float rotationTime, Rotation facingRotation, @Nullable BlockPos highlightPos, Map<BlockPos, BlockEntity> blockEntityCache,
                                   Set<BlockEntity> erroredBlockEntities, RandomSource randomSource, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea,
                                   @Nullable Identifier templateId, float inViewScale) {

        this(size, rotationTime, facingRotation, highlightPos, blockEntityCache, erroredBlockEntities, randomSource,
                x0, y0, x1, y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea), templateId, inViewScale);
    }


    public static GuiStructureRenderState simpleGuiRenderState(Vec3i size, float rotationTime, int x0, int y0, int x1, int y1, float scale, @Nullable Identifier templateId, float inViewScale) {
        return new GuiStructureRenderState(size, rotationTime, Rotation.NONE, null, Map.of(), Set.of(), RandomSource.create(), x0, y0, x1, y1, scale, null, templateId, inViewScale);



    }


}