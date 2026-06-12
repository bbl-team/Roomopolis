package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.network.packet.SyncPaletteSelection;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import com.benbenlaw.roomopolis.renderer.GuiStructureRenderState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

public class PaletteScreen extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/template_gui.png");

    private static final int PALETTE_X = 95;
    private static final int PALETTE_Y = 17;
    private static final int RESULTS_X = 130;
    private static final int RESULTS_Y = 17;
    private static final int SLOT_SPACING = 20;
    private static final int SLOT_COLUMNS = 4;

    private final Identifier templateId;
    private final Screen parent;

    private final int imageWidth = 230;
    private final int imageHeight = 166;

    private TemplateDefinition definition;
    private GuiRenderer renderer;

    private Block selectedSource;

    public PaletteScreen(Identifier id, Screen parent) {
        super(Component.literal("Palette Editor"));
        this.templateId = id;
        this.parent = parent;
    }

    @Override
    protected void init() {

        this.definition = TemplateData.DATA.get(templateId);

        this.renderer = new GuiRenderer(Minecraft.getInstance().renderBuffers().bufferSource());

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addRenderableWidget(
                Button.builder(Component.literal("<"), b -> Minecraft.getInstance().setScreen(parent))
                        .bounds(x + 6, y + 140, 20, 20)
                        .build()
        );
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 230, 166,230, 166);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        if (definition == null || definition.pallets() == null) {
            return;
        }

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        drawStructurePreview(graphics, x, y);
        drawPaletteUI(graphics, x, y);
    }

    private void drawStructurePreview(GuiGraphicsExtractor graphics,
                                      int x,
                                      int y) {

        graphics.text(Minecraft.getInstance().font, Component.translatable("tooltip.rooms.palette_preview").withStyle(ChatFormatting.DARK_GRAY),
                x + 8, y + 6, 0xFFFFFFFF, false);

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;

        GuiStructureRenderState preview = GuiStructureRenderState.simpleGuiRenderState(null, rotationTime, x + 8, y + 18, x + 90, y + 100, 1.0f, templateId, 50.0f);

        GuiRenderState state = ((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();

        if (state != null) {
            renderer.prepare(preview, state, Minecraft.getInstance().getWindow().getGuiScale());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        if (definition == null) {
            return super.mouseClicked(event, doubleClick);
        }

        int guiX = (width - imageWidth) / 2;
        int guiY = (height - imageHeight) / 2;


        int i = 0;

        for (Block source : definition.pallets().keySet()) {

            int col = i / SLOT_COLUMNS;
            int row = i % SLOT_COLUMNS;

            int xPos = guiX + PALETTE_X + (col * SLOT_SPACING);
            int yPos = guiY + PALETTE_Y + (row * SLOT_SPACING);

            if (isInside(event.x(), event.y(), xPos, yPos)) {
                selectedSource = source;
                return true;
            }

            i++;
        }

        if (selectedSource != null) {

            List<Block> replacements = definition.pallets().get(selectedSource);

            if (replacements != null) {

                int j = 0;

                for (Block target : replacements) {

                    int col = j % SLOT_COLUMNS;
                    int row = j / SLOT_COLUMNS;

                    int rx =
                            guiX + RESULTS_X + (col * SLOT_SPACING);

                    int ry =
                            guiY + RESULTS_Y + (row * SLOT_SPACING);

                    if (isInside(event.x(), event.y(), rx, ry)) {

                        TemplateData.setPaletteMapping(Minecraft.getInstance().player.getUUID(), templateId, selectedSource, target);

                        ClientPacketDistributor.sendToServer(new SyncPaletteSelection(templateId, selectedSource, target));

                        return true;
                    }

                    j++;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private boolean isInside(double mx, double my, int x, int y) {
        return mx >= x && mx <= x + 16 && my >= y && my <= y + 16;
    }

    private void drawPaletteUI(GuiGraphicsExtractor graphics, int x, int y) {

        if (definition == null) {
            return;
        }

        int i = 0;

        for (Block source : definition.pallets().keySet()) {

            int col = i / SLOT_COLUMNS;
            int row = i % SLOT_COLUMNS;

            int xPos = x + PALETTE_X + (col * SLOT_SPACING);
            int yPos = y + PALETTE_Y + (row * SLOT_SPACING);

            graphics.fakeItem(new ItemStack(source), xPos, yPos);

            if (source.equals(selectedSource)) {
                graphics.text(Minecraft.getInstance().font, Component.literal("->"), xPos + 20, yPos + 4, 0xFFFFAA00, false);
            }

            i++;
        }

        if (selectedSource != null) {

            List<Block> list = definition.pallets().get(selectedSource);

            if (list != null) {

                int j = 0;

                for (Block target : list) {

                    int col = j % SLOT_COLUMNS;
                    int row = j / SLOT_COLUMNS;
                    int rx = x + RESULTS_X + (col * SLOT_SPACING);
                    int ry = y + RESULTS_Y + (row * SLOT_SPACING);

                    graphics.fakeItem(new ItemStack(target), rx, ry );
                    j++;
                }
            }
        }
    }

    @Override
    public void onClose() {

        if (renderer != null) {
            renderer.close();
        }

        Minecraft.getInstance().setScreen(parent);
    }
}