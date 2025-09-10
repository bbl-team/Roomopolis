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
        public static final TagKey<Block> NOT_NEEDED_FOR_BLOCK_REQUIREMENTS = tag("not_needed_for_block_requirements");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Roomopolis.MOD_ID, name));
        }
    }


}
