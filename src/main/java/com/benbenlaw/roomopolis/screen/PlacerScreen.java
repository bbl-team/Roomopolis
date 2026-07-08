package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import com.benbenlaw.roomopolis.renderer.GuiStructureRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacerScreen extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/placer_gui.png");

    private final int imageWidth = 176;
    private final int imageHeight = 166;

    private Identifier selectedTemplateId = null;

    private int page = 0;
    private static final int ITEMS_PER_PAGE = 6;
    private String searchQuery = "";

    private final Map<Identifier, GuiRenderer> renderers = new HashMap<>();

    public PlacerScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addRenderableWidget(Button.builder(Component.literal("<"), b -> page = Math.max(0, page - 1)).bounds(x + 6, y + 140, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> page++).bounds(x + 150, y + 140, 20, 20).build());
        EditBox searchBox = new EditBox(Minecraft.getInstance().font, x + 28, y + 140, 120, 20, Component.literal("Search"));
        searchBox.setTooltip(Tooltip.create(Component.translatable("tooltip.rooms.placer.search_bar")));
        searchBox.setResponder(text -> { searchQuery = text.toLowerCase(); page = 0; });
        addRenderableWidget(searchBox);
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

        graphics.text(Minecraft.getInstance().font, Component.translatable("item.rooms.placer"), x + 8, y + 6, 0xFFFFFFFF, true);

        int startX = x + 8;
        int startY = y + 18;

        List<Map.Entry<Identifier, TemplateDefinition>> allTemplates = getFilteredTemplates();

        int maxPage = Math.max(0, (allTemplates.size() - 1) / ITEMS_PER_PAGE);
        page = Math.min(page, maxPage);

        int startIndex = page * ITEMS_PER_PAGE;

        List<Map.Entry<Identifier, TemplateDefinition>> templates =
                allTemplates.stream().skip(startIndex).limit(ITEMS_PER_PAGE).toList();

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;
        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();

        int index = 0;

        TemplateDefinition hoveredData = null;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {

                int areaX = startX + col * 54;
                int areaY = startY + row * 54;

                if (index < templates.size()) {

                    var entry = templates.get(index);
                    Identifier definitionId = entry.getKey();
                    TemplateDefinition data = entry.getValue();
                    Identifier structureId = data.templateId();

                    StructureTemplate template = FakeStructureTemplateManager.INSTANCE.templates.get(structureId);

                    if (template == null) {
                        index++;
                        continue;
                    }

                    StructureTemplate.Palette palette = template.palettes.getFirst();
                    int totalPos = palette.blocks().size();
                    boolean tooLarge = totalPos > 2000;
                    boolean showInPlacer = data.restrictions().showInPlacer();

                    boolean isMouseHovering = mouseX >= areaX && mouseX <= areaX + 52 &&
                            mouseY >= areaY && mouseY <= areaY + 52;

                    if (!showInPlacer) {
                        graphics.fill(areaX, areaY, areaX + 52, areaY + 52, 0x55888888);
                        graphics.text(Minecraft.getInstance().font,
                                Component.translatable("tooltip.rooms.placer.hidden"),
                                areaX + 2, areaY + 2, 0xFFAAAAAA, false);
                        graphics.text(Minecraft.getInstance().font,
                                Component.translatable("tooltip.rooms.placer.click_for"),
                                areaX + 2, areaY + 22, 0xFFFFFFFF, false);
                        graphics.text(Minecraft.getInstance().font,
                                Component.translatable("tooltip.rooms.placer.options"),
                                areaX + 2, areaY + 32, 0xFFFFFFFF, false);
                        if (isMouseHovering) {
                            hoveredData = data;
                        }

                    } else if (tooLarge) {
                        graphics.fill(areaX, areaY, areaX + 52, areaY + 52, 0x55FF0000);
                        graphics.text(Minecraft.getInstance().font,
                                Component.translatable("tooltip.rooms.placer.too_large"),
                                areaX + 2, areaY + 2, 0xFFFFFFFF, false);
                        graphics.text(Minecraft.getInstance().font,
                                Component.translatable("tooltip.rooms.placer.click_to"),
                                areaX + 2, areaY + 22, 0xFFFFFFFF, false);
                        graphics.text(Minecraft.getInstance().font,
                                Component.translatable("tooltip.rooms.placer.view"),
                                areaX + 2, areaY + 32, 0xFFFFFFFF, false);

                        if (isMouseHovering) {
                            hoveredData = data;
                        }

                    } else {

                        GuiStructureRenderState state = GuiStructureRenderState.simpleGuiRenderState(
                                template.getSize(), rotationTime,
                                areaX + 2, areaY + 2, areaX + 50, areaY + 50,
                                1.0f, structureId, definitionId, 35.0f
                        );

                        GuiRenderState stateObject = ((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();
                        GuiRenderer renderer = renderers.computeIfAbsent(
                                definitionId, key -> new GuiRenderer(Minecraft.getInstance().renderBuffers().bufferSource())
                        );
                        renderer.prepare(state, stateObject, guiScale);

                        if (definitionId.equals(selectedTemplateId)) {
                            graphics.fill(areaX, areaY, areaX + 52, areaY + 52, 0x55FFFF00);
                        }

                        if (isMouseHovering) {
                            hoveredData = data;
                        }
                    }
                }

                index++;
            }
        }

        if (selectedTemplateId != null) {
            renderSelectedTemplateInfo(graphics, x, y);
        }

        if (hoveredData != null) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable(hoveredData.translatableName()), mouseX, mouseY);
        }
    }

    private void renderSelectedTemplateInfo(GuiGraphicsExtractor graphics, int x, int y) {

        TemplateDefinition def = TemplateData.DATA.get(selectedTemplateId);
        if (def == null) return;

        int panelX = x + 8;
        int panelY = y + 130;

        graphics.text(Minecraft.getInstance().font, Component.literal("Selected: " + selectedTemplateId.getPath()), panelX, panelY, 0xFFFFFFFF, false);

        var placement = def.placement();
        graphics.text(Minecraft.getInstance().font, Component.literal("Max Height: " + placement.maxHeight()), panelX, panelY + 12, 0xFFFFFFFF, false);

        var door = def.door();
        graphics.text(Minecraft.getInstance().font, Component.literal("Door: L:" + door.left() + " R:" + door.right()), panelX + 90, panelY + 12, 0xFFFFFFFF, false);
    }

    @Override
    public void onClose() {
        super.onClose();
        for (GuiRenderer renderer : renderers.values()) {
            renderer.close();
        }
        renderers.clear();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int startX = x + 8;
        int startY = y + 18;

        List<Map.Entry<Identifier, TemplateDefinition>> allTemplates = getFilteredTemplates();
        int startIndex = page * ITEMS_PER_PAGE;
        List<Map.Entry<Identifier, TemplateDefinition>> templates = allTemplates.stream().skip(startIndex).limit(ITEMS_PER_PAGE).toList();

        int index = 0;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {

                int areaX = startX + col * 54;
                int areaY = startY + row * 54;

                if (event.x() >= areaX && event.x() < areaX + 52 && event.y() >= areaY && event.y() < areaY + 52) {

                    if (index < templates.size()) {
                        var entry = templates.get(index);
                        Minecraft.getInstance().setScreen(new TemplateScreen(entry.getKey(), this));
                    }

                    return true;
                }

                index++;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private List<Map.Entry<Identifier, TemplateDefinition>> getFilteredTemplates() {
        return TemplateData.DATA.entrySet()
                .stream()
                .filter(e -> e.getKey().getPath().toLowerCase().contains(searchQuery))
                .sorted(Comparator.comparingInt(e -> e.getValue().restrictions().placerPosition()))
                .toList();
    }
}