package com.benbenlaw.roomopolis.screen;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.loader.TemplateDefinition;
import com.benbenlaw.roomopolis.compoment.RoomsDataComponents;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.benbenlaw.roomopolis.loader.TemplateData;
import com.benbenlaw.roomopolis.mixin.GuiGraphicsExtractorAccessor;
import com.benbenlaw.roomopolis.network.packet.SyncPlacerStack;
import com.benbenlaw.roomopolis.renderer.GuiRenderState;
import com.benbenlaw.roomopolis.renderer.GuiRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.*;

public class PlacerScreen extends Screen {

    private static final Identifier TEXTURE = Roomopolis.identifier("textures/gui/placer_gui.png");

    private final int imageWidth = 176;
    private final int imageHeight = 166;
    private Identifier selectedTemplateId = null;

    private final Map<Identifier, GuiRenderer> renderers = new HashMap<>();

    public PlacerScreen(Component title) {
        super(title);
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

        graphics.text(Minecraft.getInstance().font, Component.translatable("menu.placer.name"), x + 8, y + 6, 0xFFFFFFFF, true);

        int startX = x + 8;
        int startY = y + 18;

        List<Map.Entry<Identifier, StructureTemplate>> templates = FakeStructureTemplateManager.INSTANCE.templates.entrySet()
                .stream()
                .toList();

        float rotationTime = (System.currentTimeMillis() % 36000) / 10.0f;
        int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();

        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int areaX = startX + col * 54;
                int areaY = startY + row * 54;

                graphics.fill(areaX, areaY, areaX + 52, areaY + 52, 0xFF2B2B2B);

                if (index < templates.size()) {
                    var entry = templates.get(index);
                    Identifier id = entry.getKey();
                    StructureTemplate template = entry.getValue();

                    GuiRenderState state = new GuiRenderState(
                            template.getSize(),
                            rotationTime,
                            Rotation.NONE,
                            null,
                            Map.of(),
                            Set.of(),
                            RandomSource.create(),
                            areaX + 2,   // x0
                            areaY + 2,   // y0
                            areaX + 50,  // x1 (50 - 2 = 48px wide)
                            areaY + 50,  // y1 (50 - 2 = 48px high)
                            1.0f,
                            null,
                            id
                    );

                    // 2. Call prepare using the GuiRenderState from the extractor
                    // This triggers the renderToTexture and subsequent blit
                    net.minecraft.client.renderer.state.gui.GuiRenderState stateObject = ((GuiGraphicsExtractorAccessor) graphics).getGuiRenderState();

                    GuiRenderer renderer = renderers.computeIfAbsent(
                            id,
                            key -> new GuiRenderer(
                                    Minecraft.getInstance().renderBuffers().bufferSource()
                            )
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

        var templates = FakeStructureTemplateManager.INSTANCE.templates.entrySet().stream().toList();

        int index = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int areaX = startX + col * 54;
                int areaY = startY + row * 54;

                if (event.x() >= areaX && event.x() < areaX + 52 && event.y() >= areaY && event.y() < areaY + 52) {
                    if (index < templates.size()) {
                        var entry = templates.get(index);
                        selectedTemplateId = entry.getKey();

                        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
                        stack.set(RoomsDataComponents.TEMPLATE_ID, selectedTemplateId);
                        ClientPacketDistributor.sendToServer(new SyncPlacerStack(stack));
                    }
                    return true;
                }
                index++;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}