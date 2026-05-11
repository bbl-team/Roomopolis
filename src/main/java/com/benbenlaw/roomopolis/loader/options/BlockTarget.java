package com.benbenlaw.roomopolis.loader.options;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public sealed interface BlockTarget permits BlockTarget.Single, BlockTarget.Tag {

    boolean matches(BlockState state);

    record Single(BlockState blockState) implements BlockTarget {

        @Override
        public boolean matches(BlockState state) {
            return state.is(blockState.getBlock());
        }
    }

    record Tag(TagKey<Block> tag) implements BlockTarget {

        @Override
        public boolean matches(BlockState state) {
            return state.is(tag);
        }
    }

    static BlockTarget fromString(String id) {

        if (id.startsWith("#")) {
            Identifier tagId = Identifier.parse(id.substring(1));
            return new BlockTarget.Tag(TagKey.create(Registries.BLOCK, tagId));
        }

        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
        return new BlockTarget.Single(block.defaultBlockState());
    }

    static String toString(BlockTarget target) {
        if (target instanceof BlockTarget.Single single) {
            return BuiltInRegistries.BLOCK.getKey(single.blockState.getBlock()).toString();
        } else if (target instanceof BlockTarget.Tag tag) {
            return "#" + tag.tag.location();
        }
        throw new IllegalArgumentException("Unknown BlockTarget type: " + target.getClass());
    }
}
