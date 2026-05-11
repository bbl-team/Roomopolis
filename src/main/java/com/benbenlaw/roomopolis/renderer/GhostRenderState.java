package com.benbenlaw.roomopolis.renderer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record GhostRenderState(ClientLevel realLevel, BlockPos pos, BlockState state) implements DelegatingBlockRenderFakeLevel {}