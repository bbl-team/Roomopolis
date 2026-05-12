package com.benbenlaw.roomopolis.loader.options;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public sealed interface BlockTarget permits BlockTarget.Any, BlockTarget.Single, BlockTarget.Tag {

    boolean matches(BlockState state);

    Component getDisplayName();

    record Single(BlockState blockState) implements BlockTarget {
        @Override
        public boolean matches(BlockState state) {
            return state.is(blockState.getBlock());
        }

        @Override
        public Component getDisplayName() {
            return blockState.getBlock().getName();
        }
    }

    record Tag(TagKey<Block> tag) implements BlockTarget {
        @Override
        public boolean matches(BlockState state) {
            return state.is(tag);
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("#" + tag.location());
        }
    }

    record Any() implements BlockTarget {
        @Override
        public boolean matches(BlockState state) {
            return true;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("Any Block");
        }
    }

    static BlockTarget fromString(String id) {
        if (id == null || id.isBlank() || id.equals("*")) {
            return new BlockTarget.Any();
        }

        if (id.startsWith("#")) {
            Identifier tagId = Identifier.parse(id.substring(1));
            return new BlockTarget.Tag(TagKey.create(Registries.BLOCK, tagId));
        }

        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
        return new BlockTarget.Single(block.defaultBlockState());
    }

    static String toString(BlockTarget target) {
        if (target instanceof BlockTarget.Any) {
            return "*";
        } else if (target instanceof Single(BlockState blockState)) {
            return BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString();
        } else if (target instanceof Tag(TagKey<Block> tag1)) {
            return "#" + tag1.location();
        }

        throw new IllegalArgumentException("Unknown BlockTarget type: " + target.getClass());
    }



}