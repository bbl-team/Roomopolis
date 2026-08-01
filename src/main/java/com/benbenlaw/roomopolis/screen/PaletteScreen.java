package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.network.packet.SyncPaletteSelection;
import com.benbenlaw.roomopolis.network.packet.SyncPlacerStack;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import com.benbenlaw.roomopolis.renderer.GuiStructureRenderState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Map;

public class PaletteScreen extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/template_gui.png");

    private static final int PALETTE_X = 92;
    private static final int PALETTE_Y = 17;
    private static final int RESULTS_X = 115;
    private static final int RESULTS_Y = 17;
    private static final int SLOT_SPACING = 16;
    private static final int SLOT_COLUMNS = 7;
    private static final int VISIBLE_ROWS = 7;
    private static final int SCROLL_AREA_HEIGHT = VISIBLE_ROWS * SLOT_SPACING;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int SCROLLBAR_TRACK_COLOR = 0xFF444444;
    private static final int SCROLLBAR_THUMB_COLOR = 0xFFAAAAAA;

    private final Identifier templateId;
    private final Screen parent;

    private final int imageWidth = 230;
    private final int imageHeight = 166;

    private TemplateDefinition definition;
    private GuiRenderer renderer;

    private Block selectedSource;

    private int paletteScrollOffset = 0;
    private int resultsScrollOffset = 0;

    private String searchQuery = "";

    public PaletteScreen(Identifier id, Screen parent) {
        super(Component.literal("Palette Editor"));
        this.templateId = id;
        this.parent = parent;
    }

    private Map<Block, List<Block>> resolvedPalettes() {
        return TemplateData.getResolvedPalettes(templateId);
    }

    private List<Block> filteredReplacements() {
        if (selectedSource == null) return List.of();
        List<Block> replacements = resolvedPalettes().get(selectedSource);
        if (replacements == null) return List.of();
        if (searchQuery.isEmpty()) return replacements;
        return replacements.stream()
                .filter(block -> {
                    String id = BuiltInRegistries.BLOCK.getKey(block).toString();
                    String name = block.getName().getString().toLowerCase();
                    return id.contains(searchQuery) || name.contains(searchQuery);
                })
                .toList();
    }

    private int paletteColumnCount() {
        return (int) Math.ceil((double) resolvedPalettes().size() / SLOT_COLUMNS);
    }

    private int maxPaletteScroll() {
        return Math.max(0, paletteColumnCount() - 1);
    }

    private int maxResultsScroll() {
        if (selectedSource == null) return 0;
        List<Block> replacements = filteredReplacements();
        if (replacements.isEmpty()) return 0;
        int rowsWithout = (int) Math.ceil((double) replacements.size() / SLOT_COLUMNS);
        if (rowsWithout <= VISIBLE_ROWS) return 0;
        int rowsWith = (int) Math.ceil((double) replacements.size() / (SLOT_COLUMNS - 1));
        return Math.max(0, rowsWith - VISIBLE_ROWS);
    }

    private int visibleResultColumns() {
        return maxResultsScroll() > 0 ? SLOT_COLUMNS - 1 : SLOT_COLUMNS;
    }

    private void scrollPalette(int delta) {
        paletteScrollOffset = Math.clamp(paletteScrollOffset + delta, 0, maxPaletteScroll());
    }

    private void scrollResults(int delta) {
        resultsScrollOffset = Math.clamp(resultsScrollOffset + delta, 0, maxResultsScroll());
    }

    @Override
    protected void init() {
        this.definition = TemplateData.DATA.get(templateId);
        this.renderer = new GuiRenderer(Minecraft.getInstance().renderBuffers().bufferSource());

        paletteScrollOffset = 0;
        resultsScrollOffset = 0;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addRenderableWidget(
                Button.builder(Component.literal("<"), b -> Minecraft.getInstance().setScreen(parent))
                        .bounds(x + 6, y + 140, 20, 20)
                        .build()
        );

        addRenderableWidget(
                Button.builder(Component.literal("Reset"), b -> {
                            TemplateData.clearPalette(Minecraft.getInstance().player.getUUID(), templateId);
                            ClientPacketDistributor.sendToServer(new SyncPaletteSelection(templateId, null, null));
                        })
                        .bounds(x + 28, y + 140, 40, 20)
                        .build()
        );

        EditBox searchBox = new EditBox(Minecraft.getInstance().font, x + 92, y + 140, 70, 20, Component.literal("Search"));
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.literal("Search...").withStyle(ChatFormatting.DARK_GRAY));
        searchBox.setResponder(text -> {
            searchQuery = text.toLowerCase();
            resultsScrollOffset = 0;
        });
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("tooltip.rooms.apply"),
                        b -> applyTemplate())
                .bounds(x + 164, y + 140, 60, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int guiX = (width - imageWidth) / 2;
        int guiY = (height - imageHeight) / 2;

        int delta = scrollY > 0 ? -1 : 1;

        boolean overPalette = mouseX >= guiX + PALETTE_X
                && mouseX < guiX + RESULTS_X
                && mouseY >= guiY + PALETTE_Y
                && mouseY < guiY + PALETTE_Y + SCROLL_AREA_HEIGHT;

        boolean overResults = mouseX >= guiX + RESULTS_X
                && mouseX < guiX + imageWidth
                && mouseY >= guiY + RESULTS_Y
                && mouseY < guiY + RESULTS_Y + SCROLL_AREA_HEIGHT;

        if (overPalette) {
            scrollPalette(delta);
            return true;
        } else if (overResults) {
            scrollResults(delta);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        Map<Block, List<Block>> palettes = resolvedPalettes();
        if (palettes.isEmpty()) return super.mouseClicked(event, doubleClick);

        int guiX = (width - imageWidth) / 2;
        int guiY = (height - imageHeight) / 2;

        int i = 0;
        for (Block source : palettes.keySet()) {
            int col = i / SLOT_COLUMNS;
            int row = i % SLOT_COLUMNS;

            int xPos = guiX + PALETTE_X + ((col - paletteScrollOffset) * SLOT_SPACING);
            int yPos = guiY + PALETTE_Y + (row * SLOT_SPACING);

            if (xPos >= guiX + PALETTE_X && xPos < guiX + RESULTS_X) {
                if (isInside(event.x(), event.y(), xPos, yPos)) {
                    selectedSource = source;
                    resultsScrollOffset = 0;
                    searchQuery = "";
                    return true;
                }
            }
            i++;
        }

        if (selectedSource != null) {
            List<Block> replacements = filteredReplacements();
            if (!replacements.isEmpty()) {
                int visibleCols = visibleResultColumns();

                int j = 0;
                for (Block target : replacements) {
                    int col = j % visibleCols;
                    int row = j / visibleCols;

                    if (col >= visibleCols) {
                        j++;
                        continue;
                    }

                    int rx = guiX + RESULTS_X + (col * SLOT_SPACING);
                    int ry = guiY + RESULTS_Y + ((row - resultsScrollOffset) * SLOT_SPACING);

                    if (ry >= guiY + RESULTS_Y && ry < guiY + RESULTS_Y + SCROLL_AREA_HEIGHT) {
                        if (isInside(event.x(), event.y(), rx, ry)) {
                            TemplateData.setPaletteMapping(Minecraft.getInstance().player.getUUID(), templateId, selectedSource, target);
                            ClientPacketDistributor.sendToServer(new SyncPaletteSelection(templateId, selectedSource, target));
                            return true;
                        }
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

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, 230, 166, 230, 166);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        if (definition == null || resolvedPalettes().isEmpty()) return;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        drawStructurePreview(graphics, x, y);
        drawPaletteUI(graphics, x, y);
        drawScrollbars(graphics, x, y);
    }

    private void drawStructurePreview(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.text(Minecraft.getInstance().font,
                Component.translatable("tooltip.rooms.palette_preview").withStyle(ChatFormatting.DARK_GRAY),
                x + 8, y + 6, 0xFFFFFFFF, false);

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;

        GuiStructureRenderState preview = GuiStructureRenderState.simpleGuiRenderState(
                null, rotationTime, x + 8, y + 18, x + 90, y + 100, 1.0f,
                definition.templateId(), templateId, definition.placement().rotation(), 50.0f);

        GuiRenderState state = ((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();
        if (state != null) {
            renderer.prepare(preview, state, Minecraft.getInstance().getWindow().getGuiScale());
        }
    }

    private void drawPaletteUI(GuiGraphicsExtractor graphics, int x, int y) {
        Map<Block, List<Block>> palettes = resolvedPalettes();

        int i = 0;
        for (Block source : palettes.keySet()) {
            int col = i / SLOT_COLUMNS;
            int row = i % SLOT_COLUMNS;

            int xPos = x + PALETTE_X + ((col - paletteScrollOffset) * SLOT_SPACING);
            int yPos = y + PALETTE_Y + (row * SLOT_SPACING);

            if (xPos >= x + PALETTE_X && xPos < x + RESULTS_X) {
                graphics.fakeItem(new ItemStack(source), xPos, yPos);

                if (source.equals(selectedSource)) {
                    graphics.text(Minecraft.getInstance().font,
                            Component.literal(">"), xPos + 18, yPos + 4, 0xFFFFAA00, false);
                }
            }
            i++;
        }

        if (selectedSource != null) {
            List<Block> list = filteredReplacements();
            int visibleCols = visibleResultColumns();

            int j = 0;
            for (Block target : list) {
                int col = j % visibleCols;
                int row = j / visibleCols;

                int rx = x + RESULTS_X + (col * SLOT_SPACING);
                int ry = y + RESULTS_Y + ((row - resultsScrollOffset) * SLOT_SPACING);

                if (ry >= y + RESULTS_Y && ry < y + RESULTS_Y + SCROLL_AREA_HEIGHT) {
                    graphics.fakeItem(new ItemStack(target), rx, ry);
                }
                j++;
            }
        }
    }

    private void drawScrollbars(GuiGraphicsExtractor graphics, int x, int y) {
        int paletteMax = maxPaletteScroll();
        if (paletteMax > 0) {
            int trackX = x + PALETTE_X;
            int trackY = y + PALETTE_Y + SCROLL_AREA_HEIGHT + 2;
            int trackW = RESULTS_X - PALETTE_X - 1;
            int trackH = SCROLLBAR_WIDTH;

            graphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, SCROLLBAR_TRACK_COLOR);

            int thumbW = Math.max(4, trackW / (paletteMax + 1));
            int thumbX = trackX + (int) ((trackW - thumbW) * ((float) paletteScrollOffset / paletteMax));

            graphics.fill(thumbX, trackY, thumbX + thumbW, trackY + trackH, SCROLLBAR_THUMB_COLOR);
        }

        int resultsMax = maxResultsScroll();
        if (resultsMax > 0) {
            int trackX = x + imageWidth - 8;
            int trackY = y + RESULTS_Y;
            int trackW = SCROLLBAR_WIDTH;
            int trackH = SCROLL_AREA_HEIGHT;

            graphics.fill(trackX, trackY, trackX + trackW, trackY + trackH, SCROLLBAR_TRACK_COLOR);

            int thumbH = Math.max(4, trackH / (resultsMax + 1));
            int thumbY = trackY + (int) ((trackH - thumbH) * ((float) resultsScrollOffset / resultsMax));

            graphics.fill(trackX, thumbY, trackX + trackW, thumbY + thumbH, SCROLLBAR_THUMB_COLOR);
        }
    }

    @Override
    public void onClose() {
        if (renderer != null) renderer.close();
        Minecraft.getInstance().setScreen(parent);
    }

    private void applyTemplate() {
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        stack.set(RoomsDataComponents.TEMPLATE_ID, templateId);
        ClientPacketDistributor.sendToServer(new SyncPlacerStack(stack));
        onClose();
    }
}