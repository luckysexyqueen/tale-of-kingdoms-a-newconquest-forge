package com.convallyria.taleofkingdoms.mixin.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.stream.Stream;

@Mixin(StructureBlockEntity.class)
public interface StructureBlockAccessor {

    @Invoker("getRelatedCorners")
    Stream<BlockPos> getRelatedCorners(BlockPos pos2, BlockPos pos3);

    @Invoker("updateBlockState")
    void updateBlockState();
}