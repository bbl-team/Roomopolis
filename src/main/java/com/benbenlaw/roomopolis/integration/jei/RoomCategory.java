package com.benbenlaw.roomopolis.integration.jei;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.block.RoomopolisBlocks;
import com.benbenlaw.roomopolis.item.FakeStructureTemplateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IScrollGridWidgetFactory;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RoomCategory implements IRecipeCategory<RoomRecipe> {

    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Roomopolis.MOD_ID, "room_category");
    public final static ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Roomopolis.MOD_ID, "textures/gui/jei_room_category.png");


    static final RecipeType<RoomRecipe> RECIPE_TYPE = RecipeType.create(Roomopolis.MOD_ID, "room_category", RoomRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final IScrollGridWidgetFactory<?> scrollGridWidgetFactory;

    public RoomCategory(IGuiHelper helper) {

        this.background = helper.createDrawable(TEXTURE, 0, 0, 140, 100);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(RoomopolisBlocks.ROOM_BLOCK.get().asItem()));
        this.scrollGridWidgetFactory = helper.createScrollGridFactory(1, 5);
        this.scrollGridWidgetFactory.setPosition(103, 6);
    }

    @Override
    public RecipeType<RoomRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.roomopolis.room_category");
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public @Nullable IDrawable getBackground() {
        return background;
    }


    @Override
    public @Nullable ResourceLocation getRegistryName(RoomRecipe recipe) {
        return recipe.templateId();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RoomRecipe recipe, IFocusGroup iFocusGroup) {

        builder.addSlot(RecipeIngredientRole.INPUT,1 ,1).addItemStack(recipe.keyItem());

        if (recipe.keyBlock().isPresent()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 37, 1).addItemStack(recipe.keyBlock().get().asItem().getDefaultInstance());
        }

        if (recipe.keyBlockTag().isPresent()) {
            TagKey<Block> tag = recipe.keyBlockTag().get();

            List<ItemStack> tagStack = new ArrayList<>();
            BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(block -> tagStack.add(new ItemStack(block.value().asItem())));
            builder.addSlot(RecipeIngredientRole.CATALYST, 37, 1).addItemStacks(tagStack);
        }


        if (recipe.requiresBlocks()) {

            Map<ItemStack, Integer> requiredBlocks = recipe.requiredItems();

            for (Map.Entry<ItemStack, Integer> entry : requiredBlocks.entrySet()) {
                ItemStack stack = entry.getKey().copy();
                int count = entry.getValue();
                stack.setCount(count);
                builder.addSlotToWidget(RecipeIngredientRole.INPUT, this.scrollGridWidgetFactory)
                        .addItemStack(stack);
            }
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, RoomRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        double left = 1;
        double top = 20;
        double right = 115;
        double bottom = 100;

        if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {

            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            Optional<StructureTemplate> optionalTemplate = FakeStructureTemplateManager.INSTANCE.get(recipe.templateId());

            if (optionalTemplate.isPresent()) {
                StructureTemplate template = optionalTemplate.get();
                int sizeX = template.getSize().getX();
                int sizeY = template.getSize().getY();
                int sizeZ = template.getSize().getZ();

                tooltip.add(Component.translatable("roomopolis.jei.tooltip.rotate", sizeX, sizeY, sizeZ));
            }

        }
    }

    @Override
    public void draw(RoomRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        assert level != null;
        assert mc.getSingleplayerServer() != null;
        Optional<StructureTemplate> optionalTemplate = FakeStructureTemplateManager.INSTANCE.get(recipe.templateId());

        if (optionalTemplate.isEmpty()) {
            guiGraphics.drawString(mc.font, Component.literal("BROKEN" + recipe.templateId().toString()), 5, 5, 0x404040, false);
        }

        else {

            StructureTemplate template = optionalTemplate.get();
            PoseStack poseStack = guiGraphics.pose();

            RenderSystem.enableDepthTest();
            Lighting.setupFor3DItems();

            poseStack.pushPose();
            poseStack.translate(50, 60, 50);

            float scale = 70f / Math.max(template.getSize().getX(), Math.max(template.getSize().getY(), template.getSize().getZ()));
            poseStack.scale(scale, -scale, scale);

            float angle = (System.currentTimeMillis() % 10000L) / 10000.0F * 360.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));

            renderStructure(template, poseStack);

            poseStack.popPose();

            Lighting.setupForFlatItems();
            RenderSystem.disableDepthTest();
        }

    }

    private void renderStructure(StructureTemplate template, PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        // Compute center of the structure
        double centerX = template.getSize().getX() / 2.0;
        double centerY = template.getSize().getY() / 2.0;
        double centerZ = template.getSize().getZ() / 2.0;

        Set<BlockPos> placedDoubleBlocks = new HashSet<>();

        for (StructureTemplate.StructureBlockInfo blockInfo : template.palettes.getFirst().blocks()) {
            BlockState state = blockInfo.state();
            Block block = state.getBlock();
            BlockPos pos = blockInfo.pos();


            if (state.isAir()) continue;

           //if (block.builtInRegistryHolder().is(RoomopolisTags.Blocks.DOUBLE_BLOCKS)) {
           //    if(placedDoubleBlocks.contains(pos)
           //}



            poseStack.pushPose();

            // ✅ Translate each block relative to the center
            poseStack.translate(
                    pos.getX() - centerX,
                    pos.getY() - centerY,
                    pos.getZ() - centerZ
            );

            dispatcher.renderSingleBlock(
                    state,
                    poseStack,
                    buffer,
                    0xF000F0,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    RenderType.TRANSLUCENT
            );

            poseStack.popPose();
        }

        buffer.endBatch();
    }


}
