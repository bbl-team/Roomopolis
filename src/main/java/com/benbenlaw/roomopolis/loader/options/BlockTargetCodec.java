package com.benbenlaw.roomopolis.loader.options;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTargetCodec {

    public static final Codec<BlockTarget> CODEC = Codec.STRING.xmap(
            BlockTargetCodec::fromString,
            BlockTargetCodec::toString
    );


    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTarget> STREAM_CODEC =
            StreamCodec.of(BlockTargetCodec::encode, BlockTargetCodec::decode);

    private static final StreamCodec<RegistryFriendlyByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC =
            StreamCodec.of(
                    (buf, state) -> ByteBufCodecs.fromCodecTrusted(BlockState.CODEC).encode(buf, state),
                    buf -> ByteBufCodecs.fromCodecTrusted(BlockState.CODEC).decode(buf)
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, TagKey<Block>> BLOCK_TAG_STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(TagKey.hashedCodec(Registries.BLOCK));


    private static void encode(RegistryFriendlyByteBuf buf, BlockTarget target) {

        if (target instanceof BlockTarget.Any) {
            buf.writeByte(0);
        }
        else if (target instanceof BlockTarget.Single(BlockState state)) {
            buf.writeByte(1);
            BLOCK_STATE_STREAM_CODEC.encode(buf, state);
        }
        else if (target instanceof BlockTarget.Tag(TagKey<Block> tag)) {
            buf.writeByte(2);
            BLOCK_TAG_STREAM_CODEC.encode(buf, tag);
        }
    }

    private static BlockTarget decode(RegistryFriendlyByteBuf buf) {

        return switch (buf.readByte()) {
            case 0 -> new BlockTarget.Any();
            case 1 -> new BlockTarget.Single(BLOCK_STATE_STREAM_CODEC.decode(buf));
            case 2 -> new BlockTarget.Tag(BLOCK_TAG_STREAM_CODEC.decode(buf));
            default -> throw new IllegalStateException("Invalid BlockTarget type");
        };
    }

    static BlockTarget fromString(String id) {

        if (id == null || id.isBlank() || id.equals("*")) {
            return new BlockTarget.Any();
        }

        if (id.startsWith("#")) {
            var tagId = net.minecraft.resources.Identifier.parse(id.substring(1));
            return new BlockTarget.Tag(TagKey.create(Registries.BLOCK, tagId));
        }

        var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getValue(net.minecraft.resources.Identifier.parse(id));

        return new BlockTarget.Single(block.defaultBlockState());
    }

    static String toString(BlockTarget target) {

        if (target instanceof BlockTarget.Any) {
            return "*";
        }

        if (target instanceof BlockTarget.Single single) {
            return net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getKey(single.blockState().getBlock())
                    .toString();
        }

        if (target instanceof BlockTarget.Tag tag) {
            return "#" + tag.tag().location();
        }

        throw new IllegalArgumentException("Unknown BlockTarget: " + target);
    }
}