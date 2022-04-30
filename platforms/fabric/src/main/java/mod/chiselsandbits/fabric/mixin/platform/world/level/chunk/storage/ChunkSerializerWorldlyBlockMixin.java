package mod.chiselsandbits.fabric.mixin.platform.world.level.chunk.storage;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerWorldlyBlockMixin
{

    private static final ThreadLocal<ChunkAccess> chunkAccessHolder = new ThreadLocal<>();
    private static final ThreadLocal<BlockPos>    blockPosHolder    = new ThreadLocal<>();

    @Redirect(
      method = "read",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
      )
    )
    private static BlockState redirectGetChunkAccessBlockState(ChunkAccess chunkAccess, BlockPos blockPos)
    {
        chunkAccessHolder.set(chunkAccess);
        blockPosHolder.set(blockPos);
        return chunkAccess.getBlockState(blockPos);
    }

    @Redirect(
      method = "read",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"
      )
    )
    private static int redirectGetBlockStateLightEmission(BlockState blockState)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getLightEmission(
              blockState, chunkAccessHolder.get(), blockPosHolder.get()
            );
        }
        return blockState.getLightEmission();
    }
}
