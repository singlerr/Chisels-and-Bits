package mod.chiselsandbits.fabric.mixin.platform.world.level.chunk;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Mixin(value = LevelChunk.class, priority = Integer.MIN_VALUE)
public abstract class LevelChunkWorldlyBlockMixin implements ChunkAccess
{

    @Shadow @Final private ChunkPos chunkPos;

    @Shadow public abstract int getMinBuildHeight();

    @Shadow public abstract BlockState getBlockState(final BlockPos param0);

    @Shadow public abstract Level getLevel();

    /**
     * @author Chisels and Bits
     * @reason It is not possible to efficiently inject into the lambda for now.
     */
    @Overwrite
    public Stream<BlockPos> getLights()
    {
        return StreamSupport.stream(BlockPos.betweenClosed(this.chunkPos.getMinBlockX(),
          this.getMinBuildHeight(),
          this.chunkPos.getMinBlockZ(),
          this.chunkPos.getMaxBlockX(),
          this.getMaxBuildHeight() - 1,
          this.chunkPos.getMaxBlockZ()).spliterator(), false).filter((blockPos) -> {
            final BlockState blockState = getBlockState(blockPos);
            if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties) {
                return blockWithWorldlyProperties.getLightEmission(
                  blockState,getLevel(),blockPos
                ) != 0;
            }

            return blockState.getLightEmission() != 0;
        });
    }
}
