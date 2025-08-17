package com.benbenlaw.roomopolis.util;

import com.benbenlaw.Roomopolis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class RoomopolisTags {

    public static class Blocks {

        // Blocks
        public static final TagKey<Block> DOUBLE_BLOCKS = tag("double_blocks");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Roomopolis.MOD_ID, name));
        }
    }


}
