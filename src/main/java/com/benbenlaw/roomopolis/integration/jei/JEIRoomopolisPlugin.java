package com.benbenlaw.roomopolis.integration.jei;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.roomopolis.block.RoomopolisBlocks;
import com.benbenlaw.roomopolis.item.KeyItem;
import com.benbenlaw.roomopolis.item.KeyItemPaletteCache;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class JEIRoomopolisPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(Roomopolis.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new RoomCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        List<RoomRecipe> recipes = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof KeyItem keyItem) {
                Map<Block, Integer> palette = KeyItemPaletteCache.getTemplatePalette(keyItem.definition().templateId());
                if (palette != null) {

                    Map<ItemStack, Integer> requiredItems = new HashMap<>();
                    for (Map.Entry<Block, Integer> entry : palette.entrySet()) {
                        ItemStack stack = new ItemStack(entry.getKey().asItem());
                        requiredItems.put(stack, entry.getValue());
                    }

                    recipes.add(new RoomRecipe(
                            keyItem.definition().templateId(),
                            keyItem.asItem().getDefaultInstance(),
                            requiredItems,
                            keyItem.definition().blocksRequired(),
                            keyItem.definition().blockTarget()));
                }
            }
        }

        registration.addRecipes(RoomCategory.RECIPE_TYPE, recipes);


    }

}