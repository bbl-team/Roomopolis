package com.benbenlaw.roomopolis.loader.options;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTargetCodec {

    public static final Codec<BlockTarget> CODEC = Codec.either(
            BlockState.CODEC.xmap(
                    BlockTarget.Single::new,
                    BlockTarget.Single::blockState
            ),
            TagKey.hashedCodec(Registries.BLOCK).xmap(
                    BlockTarget.Tag::new,
                    BlockTarget.Tag::tag
            )
    ).xmap(
            either -> either.map(b -> b, t -> t),
            target ->
                    target instanceof BlockTarget.Single single
                            ? Either.left(single)
                            : Either.right((BlockTarget.Tag) target)
    );

    private static final StreamCodec<
            RegistryFriendlyByteBuf,
            BlockState
            > BLOCK_STATE_STREAM_CODEC = StreamCodec.of(
            (buf, state) ->
                    ByteBufCodecs.fromCodecTrusted(BlockState.CODEC).encode(buf, state),

            buf -> ByteBufCodecs.fromCodecTrusted(BlockState.CODEC).decode(buf)
    );

    private static final StreamCodec<
            RegistryFriendlyByteBuf,
            TagKey<Block>
            > BLOCK_TAG_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
            TagKey.hashedCodec(Registries.BLOCK)
    );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            BlockTarget
            > STREAM_CODEC = StreamCodec.of(
            BlockTargetCodec::encode,
            BlockTargetCodec::decode
    );

    private static void encode(RegistryFriendlyByteBuf buf, BlockTarget target) {
        if (target instanceof BlockTarget.Single(BlockState blockState)) {
            buf.writeBoolean(true);

            BLOCK_STATE_STREAM_CODEC.encode(buf, blockState);
        } else if (target instanceof BlockTarget.Tag(TagKey<Block> tag1)) {
            buf.writeBoolean(false);

            BLOCK_TAG_STREAM_CODEC.encode(buf, tag1);
        }
    }

    private static BlockTarget decode(RegistryFriendlyByteBuf buf) {
        boolean isSingle = buf.readBoolean();

        if (isSingle) {
            return new BlockTarget.Single(BLOCK_STATE_STREAM_CODEC.decode(buf));
        }

        return new BlockTarget.Tag(BLOCK_TAG_STREAM_CODEC.decode(buf));
    }
}
