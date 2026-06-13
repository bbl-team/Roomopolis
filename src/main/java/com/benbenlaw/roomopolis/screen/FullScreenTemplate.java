package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import com.benbenlaw.roomopolis.renderer.GuiStructureRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;

public class FullScreenTemplate extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/fullscreen_gui.png");
    private final Identifier templateId;

    private final int imageWidth = 352;
    private final int imageHeight = 332;

    private Identifier selectedTemplateId = null;

    private GuiRenderer renderer;
    private Rotation rotation = Rotation.NONE;
    private final Screen parent;

    public FullScreenTemplate(Identifier templateId, Screen parent) {
        super(Component.literal("Template Config"));
        this.templateId = templateId;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        renderer = new GuiRenderer(Minecraft.getInstance().renderBuffers().bufferSource());

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addRenderableWidget(Button.builder(Component.literal("<"), b -> Minecraft.getInstance().setScreen(parent)).bounds(x + 6, y + 140, 20, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;


        GuiStructureRenderState previewState = GuiStructureRenderState.simpleGuiRenderState(
                null,
                rotationTime,
                x + 8,
                y + 18,
                x + 90,
                y + 100,
                1.0f,
                templateId,
                200.0f
        );

        GuiRenderState stateObject =((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();
        renderer.prepare(previewState, stateObject, Minecraft.getInstance().getWindow().getGuiScale());

        graphics.text(Minecraft.getInstance().font, Component.literal(templateId.getPath()), x - 100, y - 80, 0xFFFFFFFF, false);
    }

    @Override
    public void onClose() {

        if (renderer != null) {
            renderer.close();
        }

        Minecraft.getInstance().setScreen(null);
    }

}