package com.benbenlaw.roomopolis.event.client;

import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.item.KeyItemSizeCache;
import com.benbenlaw.roomopolis.util.DirectionUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Optional;

public class KeyPreviewRenderer {

    private static ItemStack lastStack = ItemStack.EMPTY;
    private static KeyItem lastKeyItem = null;
    private static BlockPos lastValidPos = null;
    private static Direction lastValidFace = null;
    static BlockStateModelSet blockStateModelSet;

    public static void onRenderLevel(RenderLevelStageEvent event) {

        blockStateModelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) return;

        ItemStack currentStack = player.getMainHandItem();

        if (!ItemStack.isSameItem(currentStack, lastStack)) {
            lastStack = currentStack;
            lastKeyItem = (currentStack.getItem() instanceof KeyItem ki) ? ki : null;
            lastValidPos = null;
            lastValidFace = null;
        }

        if (lastKeyItem == null) return;

        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos clickedPos = blockHit.getBlockPos();
            Direction face = blockHit.getDirection();

            if (canPreviewPlace(lastKeyItem, player, clickedPos, face)) {
                lastValidPos = clickedPos;
                lastValidFace = face;
            }
        }

        if (lastValidPos == null || lastValidFace == null) return;

        Rotation baseRotation = DirectionUtil.getRotationFromDirection(lastValidFace);
        BlockPos placePosition = lastValidPos;

        if (lastValidFace == Direction.UP) {
            placePosition = lastValidPos.above(3);
            baseRotation = DirectionUtil.getRotationFromDirection(player.getDirection().getOpposite());
        }

        Rotation finalRotation = DirectionUtil.combineRotation(baseRotation, lastKeyItem.definition().rotation());
        Direction facing = lastValidFace.getOpposite();

        Vec3i templateSize = KeyItemSizeCache.getTemplateSize(lastKeyItem.definition().templateId());
        Optional<StructureTemplate> optionalTemplate = FakeStructureTemplateManager.INSTANCE.get(lastKeyItem.definition().templateId());

        if (templateSize == null || optionalTemplate.isEmpty()) return;

        StructureTemplate template = optionalTemplate.get();
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(finalRotation).setMirror(Mirror.NONE);

        BlockPos centerOffset = new BlockPos(-templateSize.getX() / 2, -templateSize.getY() / 2, -templateSize.getZ() / 2);
        BlockPos adjustedOffset = StructureTemplate.calculateRelativePosition(settings, centerOffset);

        int forwardShift = Math.max(templateSize.getX() / 2, 1) + 1 + lastKeyItem.definition().frontAdjustment();
        BlockPos forwardOffset = placePosition.relative(facing, forwardShift);
        BlockPos placementPos = forwardOffset.offset(adjustedOffset).above(lastKeyItem.definition().heightAdjustment());

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = mc.gameRenderer.getMainCamera().position();

        poseStack.pushPose();
        poseStack.translate(placementPos.getX() - camPos.x, placementPos.getY() - camPos.y, placementPos.getZ() - camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        var blockRenderer = new ModelBlockRenderer(false, false, BlockColors.createDefault());
        StructureTemplate.Palette palette = template.palettes.getFirst();

        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
            if (info.state().isAir()) continue;

            BlockPos rotatedPos = StructureTemplate.calculateRelativePosition(settings, info.pos());
            BlockState rotatedState = info.state().rotate(level, placementPos, finalRotation);

            poseStack.pushPose();
            poseStack.translate(rotatedPos.getX(), rotatedPos.getY(), rotatedPos.getZ());

            BlockQuadOutput solidOutput = (x, y, z, quad, instance) ->
                    putBakedQuad(poseStack, bufferSource, x, y, z, quad, instance, ChunkSectionLayer.CUTOUT);

            blockRenderer.tesselateBlock(
                    solidOutput,
                    0.0F, 0.0F, 0.0F,
                    (BlockAndTintGetter) level,
                    placementPos,
                    rotatedState,
                    blockStateModelSet.get(rotatedState),
                    rotatedState.getSeed(placementPos)
            );
            poseStack.popPose();
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static void putBakedQuad(
            final PoseStack poseStack,
            final MultiBufferSource.BufferSource bufferSource,
            final float x,
            final float y,
            final float z,
            final BakedQuad quad,
            final QuadInstance instance,
            final ChunkSectionLayer layer
    ) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        VertexConsumer buffer = bufferSource.getBuffer(switch (layer) {
            case SOLID -> RenderTypes.solidMovingBlock();
            case CUTOUT -> RenderTypes.cutoutMovingBlock();
            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
        });
        buffer.putBakedQuad(poseStack.last(), quad, instance);
        poseStack.popPose();
    }

    private static boolean canPreviewPlace(KeyItem key, Player player, BlockPos lookPos, Direction face) {
        if (!key.definition().blockTarget().matches(player.level().getBlockState(lookPos))) return false;
        if (key.definition().topOnlyPlacement() && face != Direction.UP) return false;
        if (key.definition().sideOnlyPlacement() && face.getAxis().isVertical()) return false;
        return true;
    }
}