package com.benbenlaw.roomopolis.renderer;

import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.item.TemplatePaletteCache;
import com.benbenlaw.roomopolis.item.TemplateSizeCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class GuiRenderer extends PictureInPictureRenderer<GuiStructureRenderState> {

    private final ModelBlockRenderer blockRenderer;

    // Unique texture label per renderer instance
    private final String textureLabel;

    public GuiRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);

        this.textureLabel = "roomopolis_placer_" + System.identityHashCode(this);

        this.blockRenderer = new ModelBlockRenderer(
                Minecraft.getInstance().options.ambientOcclusion().get(),
                false,
                Minecraft.getInstance().getBlockColors()
        );
    }

    @Override
    public Class<GuiStructureRenderState> getRenderStateClass() {
        return GuiStructureRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiStructureRenderState state, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || state.templateId() == null) {
            return;
        }

        Identifier id = state.templateId();

        Vec3i size = TemplateSizeCache.getTemplateSize(id);
        List<TemplatePaletteCache.BlockPosWithState> blocks =
                TemplatePaletteCache.getRenderPalette(id);

        // Auto-cache template if needed
        if (blocks == null || size == null) {

            var template = FakeStructureTemplateManager.INSTANCE.templates.get(id);

            if (template != null) {

                TemplatePaletteCache.cacheTemplate(id, template);
                TemplateSizeCache.setTemplateSize(id, template.getSize());

                size = template.getSize();
                blocks = TemplatePaletteCache.getRenderPalette(id);

                System.out.println(
                        "Cached template " + id +
                                " size=" + size +
                                " blocks=" + blocks.size()
                );
            }
        }

        if (size == null || blocks == null || blocks.isEmpty()) {
            return;
        }

        float maxDim = Math.max(
                size.getX(),
                Math.max(size.getY(), size.getZ())
        );

        float scale = state.inViewScale() / Math.max(1.0f, maxDim);

        poseStack.pushPose();

        /*
         * Center texture
         */
        poseStack.translate(0, 0, 100.0f);

        /*
         * Scale into slot
         */
        poseStack.scale(scale, scale, scale);

        /*
         * Isometric rotation
         */
        poseStack.mulPose(Axis.XP.rotationDegrees(210f));
        poseStack.mulPose(Axis.YP.rotationDegrees(state.rotationTime() / 3));

        /*
         * Center structure pivot
         */
        poseStack.translate(
                -size.getX() / 2.0f,
                -size.getY() / 2.0f,
                -size.getZ() / 2.0f
        );

        /*
         * Render blocks
         */
        for (var blockEntry : blocks) {

            // IMPORTANT:
            // Use ACTUAL STORED BLOCK STATE
            // NOT defaultBlockState()
            BlockState blockState = blockEntry.block().defaultBlockState();

            this.renderBlock(
                    mc.level,
                    blockState,
                    blockEntry.pos(),
                    poseStack
            );
        }

        poseStack.popPose();

        /*
         * Flush buffers to texture
         */
        this.bufferSource.endBatch();
    }

    private void renderBlock(
            ClientLevel level,
            BlockState state,
            BlockPos pos,
            PoseStack poseStack
    ) {

        poseStack.pushPose();

        poseStack.translate(
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );

        BlockStateModel model =
                Minecraft.getInstance()
                        .getModelManager()
                        .getBlockStateModelSet()
                        .get(state);

        int lightCoords = 15728880;

        var renderType =
                model.hasMaterialFlag(
                        net.minecraft.client.resources.model.geometry.BakedQuad.FLAG_TRANSLUCENT
                )
                        ? Sheets.translucentBlockSheet()
                        : Sheets.cutoutBlockSheet();

        VertexConsumer buffer =
                this.bufferSource.getBuffer(renderType);

        BlockQuadOutput output = (_, _, _, quad, instance) -> {

            instance.setLightCoords(lightCoords);

            buffer.putBakedQuad(
                    poseStack.last(),
                    quad,
                    instance
            );
        };

        this.blockRenderer.tesselateBlock(
                output,
                0,
                0,
                0,
                new GhostRenderState(level, pos, state),
                pos,
                state,
                model,
                0
        );

        poseStack.popPose();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return this.textureLabel;
    }
}