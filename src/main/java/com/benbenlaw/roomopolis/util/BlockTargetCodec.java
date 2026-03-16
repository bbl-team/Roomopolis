package com.benbenlaw.roomopolis.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTargetCodec {

    public static final Codec<BlockTarget> CODEC = Codec.either(
            BlockState.CODEC.xmap(BlockTarget.Single::new, BlockTarget.Single::blockState),
            TagKey.hashedCodec(Registries.BLOCK).xmap(BlockTarget.Tag::new, BlockTarget.Tag::tag)
    ).xmap(
            either -> either.map(b -> b, t -> t),
            target -> target instanceof BlockTarget.Single single
                    ? Either.left(single)
                    : Either.right((BlockTarget.Tag) target)
    );
}