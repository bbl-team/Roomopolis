package com.benbenlaw.roomopolis.event.client;

/*
@OnlyIn(Dist.CLIENT)
public class KeyPreviewRenderer {

    private static ItemStack lastStack = ItemStack.EMPTY;
    private static KeyItem lastKeyItem = null;
    private static BlockPos lastValidPos = null;
    private static Direction lastValidFace = null;

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) return;

        ItemStack currentStack = player.getMainHandItem();

        // Update cached KeyItem if player switched items
        if (!ItemStack.isSameItem(currentStack, lastStack)) {
            lastStack = currentStack;
            if (currentStack.getItem() instanceof KeyItem keyItem) {
                lastKeyItem = keyItem;
            } else {
                lastKeyItem = null;
            }
            lastValidPos = null;
            lastValidFace = null;
        }

        if (lastKeyItem == null) return;

        // Use current hit result for possible update
        HitResult hit = mc.hitResult;
        BlockPos clickedPos = null;
        Direction face = null;

        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            clickedPos = blockHit.getBlockPos();
            face = blockHit.getDirection();

            if (canPreviewPlace(lastKeyItem, player, clickedPos, face)) {
                lastValidPos = clickedPos;
                lastValidFace = face;
            }
        }

        if (lastValidPos == null || lastValidFace == null) return;

        Rotation rotation = DirectionUtil.getRotationFromDirection(lastValidFace);
        Direction facing = lastValidFace.getOpposite();

        BlockPos placePosition = lastValidPos;
        if (lastValidFace == Direction.UP) {
            placePosition = lastValidPos.above(3);
            rotation = DirectionUtil.getRotationFromDirection(player.getDirection().getOpposite());
        }

        Vec3i templateSize = KeyItemSizeCache.getTemplateSize(lastKeyItem.templateId);
        if (templateSize == null) return;

        Optional<StructureTemplate> optionalTemplate = FakeStructureTemplateManager.INSTANCE.get(lastKeyItem.templateId);
        if (optionalTemplate.isEmpty()) return;

        StructureTemplate template = optionalTemplate.get();
        StructurePlaceSettings settings = new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE);

        BlockPos centerOffset = new BlockPos(-templateSize.getX() / 2, -templateSize.getY() / 2, -templateSize.getZ() / 2);
        BlockPos adjustedOffset = StructureTemplate.calculateRelativePosition(settings, centerOffset);
        int forwardShift = Math.max(templateSize.getX() / 2, 1) + 1 + lastKeyItem.frontAdjustment;
        BlockPos forwardOffset = placePosition.relative(facing, forwardShift);
        BlockPos placementPos = forwardOffset.offset(adjustedOffset).above(lastKeyItem.heightAdjustment);

        PoseStack poseStack = event.getPoseStack();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(
                placementPos.getX() - camPos.x,
                placementPos.getY() - camPos.y,
                placementPos.getZ() - camPos.z
        );

        StructureTemplate.Palette palette = template.palettes.getFirst();
        var blockRenderer = mc.getBlockRenderer();
        MultiBufferSource.BufferSource translucentBuffer = mc.renderBuffers().bufferSource();
        MultiBufferSource.BufferSource lineBufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lineBuffer = lineBufferSource.getBuffer(RenderType.LINES);

        // Track overall structure bounds
        BlockPos min = null;
        BlockPos max = null;

        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
            BlockPos rel = info.pos();
            BlockPos rotatedPos = StructureTemplate.calculateRelativePosition(settings, rel);

            BlockState state = info.state();
            if (state.isAir()) continue;

            BlockState rotatedState = state.rotate(settings.getRotation());

            // Render ghost block
            poseStack.pushPose();
            poseStack.translate(rotatedPos.getX(), rotatedPos.getY(), rotatedPos.getZ());
            blockRenderer.renderSingleBlock(rotatedState, poseStack, translucentBuffer, 0xF000F0, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();

            // Safe bounds calculation
            var shape = rotatedState.getShape(level, placementPos.offset(rotatedPos));
            if (!shape.isEmpty()) {
                AABB shapeBounds = shape.bounds();

                BlockPos blockMin = new BlockPos(
                        rotatedPos.getX() + (int) Math.floor(shapeBounds.minX),
                        rotatedPos.getY() + (int) Math.floor(shapeBounds.minY),
                        rotatedPos.getZ() + (int) Math.floor(shapeBounds.minZ)
                );
                BlockPos blockMax = new BlockPos(
                        rotatedPos.getX() + (int) Math.ceil(shapeBounds.maxX),
                        rotatedPos.getY() + (int) Math.ceil(shapeBounds.maxY),
                        rotatedPos.getZ() + (int) Math.ceil(shapeBounds.maxZ)
                );

                if (min == null) min = blockMin;
                else min = new BlockPos(
                        Math.min(min.getX(), blockMin.getX()),
                        Math.min(min.getY(), blockMin.getY()),
                        Math.min(min.getZ(), blockMin.getZ())
                );

                if (max == null) max = blockMax;
                else max = new BlockPos(
                        Math.max(max.getX(), blockMax.getX()),
                        Math.max(max.getY(), blockMax.getY()),
                        Math.max(max.getZ(), blockMax.getZ())
                );
            }
        }

        // Draw bounding box
        if (min != null && max != null) {
            Vec3 minVec = new Vec3(min.getX(), min.getY(), min.getZ());
            Vec3 maxVec = new Vec3(max.getX(), max.getY(), max.getZ());
            AABB structureBox = new AABB(minVec, maxVec);
            LevelRenderer.renderLineBox(poseStack, lineBuffer, structureBox, 0.1F, 1.0F, 0.1F, 1.0F);
        }

        poseStack.popPose();
        translucentBuffer.endBatch();
        lineBufferSource.endBatch();
    }


    @OnlyIn(Dist.CLIENT)
    private static boolean canPreviewPlace(KeyItem key, Player player, BlockPos lookPos, Direction face) {
        if (key.keyBlock.isPresent() && !player.level().getBlockState(lookPos).is(key.keyBlock.get())) return false;
        if (key.keyBlockTag.isPresent() && !player.level().getBlockState(lookPos).is(key.keyBlockTag.get())) return false;
        if (key.topOnlyPlacement && face != Direction.UP) return false;
        if (key.sideOnlyPlacement && face.getAxis().isVertical()) return false;
        return true;
    }
}
*/