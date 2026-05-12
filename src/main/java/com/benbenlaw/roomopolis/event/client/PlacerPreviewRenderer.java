package com.benbenlaw.roomopolis.event.client;

import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.item.TemplateSizeCache;
import com.benbenlaw.roomopolis.item.PlacerItem;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.util.DirectionUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

public class PlacerPreviewRenderer {

    private static ItemStack lastStack = ItemStack.EMPTY;

    private static BlockPos lastValidPos = null;
    private static Direction lastValidFace = null;

    private static BlockStateModelSet blockStateModelSet;

    public static void onRenderLevel(RenderLevelStageEvent event) {

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) return;
        ItemStack currentStack = player.getMainHandItem();
        if (!(currentStack.getItem() instanceof PlacerItem)) return;
        Identifier definitionId = currentStack.get(RoomsDataComponents.TEMPLATE_ID);

        if (definitionId == null) return;

        TemplateDefinition definition = TemplateData.DATA.get(definitionId);

        if (definition == null) return;
        Identifier templateId = definition.templateId();

        if (!ItemStack.isSameItemSameComponents(currentStack, lastStack)) {
            lastStack = currentStack.copy();
            lastValidPos = null;
            lastValidFace = null;
        }

        Optional<StructureTemplate> optionalTemplate = FakeStructureTemplateManager.INSTANCE.get(templateId);

        if (optionalTemplate.isEmpty()) {
            System.out.println("Missing structure template: " + templateId);
            return;
        }

        StructureTemplate template = optionalTemplate.get();
        HitResult hit = mc.hitResult;

        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {

            BlockPos clickedPos = blockHit.getBlockPos();
            Direction face = blockHit.getDirection();

            if (canPreviewPlace(definition, player, clickedPos, face)) {

                lastValidPos = clickedPos;
                lastValidFace = face;
            }
        } else {
            lastValidPos = null;
            lastValidFace = null;
        }

        if (lastValidPos == null || lastValidFace == null) {
            return;
        }

        Vec3i templateSize = TemplateSizeCache.getTemplateSize(templateId);
        if (templateSize == null) return;

        Rotation baseRotation = DirectionUtil.getRotationFromDirection(lastValidFace);
        BlockPos placePosition = lastValidPos;
        if (lastValidFace == Direction.UP) {

            placePosition = lastValidPos.above(3);
            baseRotation = DirectionUtil.getRotationFromDirection(player.getDirection().getOpposite());
        }

        Rotation finalRotation = baseRotation;
        Direction facing = lastValidFace.getOpposite();

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(finalRotation).setMirror(Mirror.NONE);

        BlockPos centerOffset = new BlockPos(-templateSize.getX() / 2, -templateSize.getY() / 2, -templateSize.getZ() / 2);
        BlockPos adjustedOffset =StructureTemplate.calculateRelativePosition(settings,centerOffset);

        int forwardShift = Math.max(templateSize.getX() / 2, 1) + 1 + definition.placement().frontAdjustment();
        BlockPos forwardOffset = placePosition.relative(facing, forwardShift);
        BlockPos placementPos = forwardOffset.offset(adjustedOffset).above(definition.placement().heightAdjustment());

        blockStateModelSet = mc.getModelManager().getBlockStateModelSet();

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = mc.gameRenderer.getMainCamera().position();

        poseStack.pushPose();
        poseStack.translate(placementPos.getX() - camPos.x, placementPos.getY() - camPos.y, placementPos.getZ() - camPos.z);
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        ModelBlockRenderer blockRenderer = new ModelBlockRenderer(false, false, BlockColors.createDefault());
        StructureTemplate.Palette palette = template.palettes.getFirst();

        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {

            if (info.state().isAir()) continue;

            BlockPos rotatedPos = StructureTemplate.calculateRelativePosition(settings, info.pos());
            BlockState baseState = info.state().rotate(level, placementPos, finalRotation);

            Block block = baseState.getBlock();

            Block replacement = TemplateData
                    .getActivePalette(Minecraft.getInstance().player.getUUID(), definition.templateId())
                    .get(block);

            BlockState rotatedState =
                    (replacement != null)
                            ? replacement.defaultBlockState()
                            : baseState;

            poseStack.pushPose();

            poseStack.translate(rotatedPos.getX(),rotatedPos.getY(),rotatedPos.getZ());

            BlockQuadOutput output = (x, y, z, quad, instance) ->
                    putBakedQuad(poseStack, bufferSource, x, y, z, quad, instance, ChunkSectionLayer.CUTOUT);

            blockRenderer.tesselateBlock(output, 0.0F, 0.0F, 0.0F, (BlockAndTintGetter) level, placementPos,
                    rotatedState, blockStateModelSet.get(rotatedState),rotatedState.getSeed(placementPos));

            poseStack.popPose();
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    private static void putBakedQuad(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float x,float y, float z, BakedQuad quad, QuadInstance instance, ChunkSectionLayer layer) {

        poseStack.pushPose();

        poseStack.translate(x, y, z);

        VertexConsumer buffer = bufferSource.getBuffer(switch (layer) {
                            case SOLID -> RenderTypes.solidMovingBlock();
                            case CUTOUT -> RenderTypes.cutoutMovingBlock();
                            case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
                        }
                );

        buffer.putBakedQuad(poseStack.last(), quad, instance);
        poseStack.popPose();
    }

    public static boolean canPreviewPlace(TemplateDefinition definition, Player player, BlockPos lookPos, Direction face) {

        BlockState state = player.level().getBlockState(lookPos);

        if (!definition.blockTarget().matches(state)) return false;
        if (definition.placement().topOnlyPlacement()&& face != Direction.UP) return false;
        if (definition.placement().sideOnlyPlacement() && face.getAxis().isVertical()) return false;

        return true;
    }
}