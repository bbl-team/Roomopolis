package com.benbenlaw.roomopolis.util;

import com.benbenlaw.Roomopolis;
import com.benbenlaw.core.util.CoreTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class RoomopolisTags {

    public static class Blocks extends CoreTags.Blocks {

        // Blocks
        public static final TagKey<Block> DOUBLE_BLOCKS = tag(Roomopolis.MOD_ID, "double_blocks");
    }


}
