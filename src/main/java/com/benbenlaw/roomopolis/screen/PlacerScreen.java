package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.item.TemplatePaletteCache;
import com.benbenlaw.roomopolis.item.TemplateSizeCache;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.renderer.GuiStructureRenderState;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.*;

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
        searchBox.setTooltip(Tooltip.create(Component.translatable("tooptip.rooms.placer.search_bar")));
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

        List<Map.Entry<Identifier, StructureTemplate>> allTemplates = getFilteredTemplates();

        int maxPage = Math.max(0, (allTemplates.size() - 1) / ITEMS_PER_PAGE);
        page = Math.min(page, maxPage);

        int startIndex = page * ITEMS_PER_PAGE;

        List<Map.Entry<Identifier, StructureTemplate>> templates =
                allTemplates.stream().skip(startIndex).limit(ITEMS_PER_PAGE).toList();

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;
        int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();

        int index = 0;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {

                int areaX = startX + col * 54;
                int areaY = startY + row * 54;

                if (index < templates.size()) {

                    var entry = templates.get(index);
                    Identifier id = entry.getKey();
                    StructureTemplate template = entry.getValue();

                    /*
                    if (TemplatePaletteCache.getTemplatePalette(id).size() > 1000) {
                        graphics.fill(areaX, areaY, areaX + 52, areaY + 52, 0x55FF0000);
                        graphics.text(Minecraft.getInstance().font, Component.literal("Too many palettes!"), areaX + 2, areaY + 2, 0xFFFFFFFF, false);
                        continue;
                    }
                     */

                    GuiStructureRenderState state = GuiStructureRenderState.simpleGuiRenderState(
                            template.getSize(),
                            rotationTime,
                            areaX + 2,
                            areaY + 2,
                            areaX + 50,
                            areaY + 50,
                            1.0f,
                            id,
                            35.0f
                    );

                    net.minecraft.client.renderer.state.gui.GuiRenderState stateObject =
                            ((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();

                    GuiRenderer renderer = renderers.computeIfAbsent(
                            id,
                            key -> new GuiRenderer(Minecraft.getInstance().renderBuffers().bufferSource())
                    );

                    renderer.prepare(state, stateObject, guiScale);

                    if (id.equals(selectedTemplateId)) {
                        graphics.fill(areaX, areaY, areaX + 52, areaY + 52, 0x55FFFF00);
                    }

                }

                index++;
            }
        }

        if (selectedTemplateId != null) {
            renderSelectedTemplateInfo(graphics, x, y);
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

        List<Map.Entry<Identifier, StructureTemplate>> allTemplates = getFilteredTemplates();
        int startIndex = page * ITEMS_PER_PAGE;
        List<Map.Entry<Identifier, StructureTemplate>> templates = allTemplates.stream().skip(startIndex).limit(ITEMS_PER_PAGE).toList();

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

    private List<Map.Entry<Identifier, StructureTemplate>> getFilteredTemplates() {
        return FakeStructureTemplateManager.INSTANCE.templates.entrySet()
                .stream()
                .filter(e -> e.getKey().getPath().toLowerCase().contains(searchQuery))
                .toList();
    }
}