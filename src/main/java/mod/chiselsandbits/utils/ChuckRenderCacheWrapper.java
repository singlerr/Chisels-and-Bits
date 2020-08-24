package mod.chiselsandbits.utils;

import mod.chiselsandbits.core.Log;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ChuckRenderCacheWrapper implements IBlockDisplayReader
{
    private final ChunkRenderCache chunkRenderCache;

    public ChuckRenderCacheWrapper(final ChunkRenderCache chunkRenderCache) {this.chunkRenderCache = chunkRenderCache;}

    @Override
    public float func_230487_a_(final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return chunkRenderCache.func_230487_a_(p_230487_1_, p_230487_2_);
    }

    @Override
    public WorldLightManager getLightManager()
    {
        return chunkRenderCache.getLightManager();
    }

    @Override
    public int getBlockColor(final BlockPos blockPosIn, final ColorResolver colorResolverIn)
    {
        return this.whenPosValidOrElse(
          blockPosIn,
          () -> chunkRenderCache.getBlockColor(blockPosIn, colorResolverIn),
          () -> 0
        );
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        return this.whenPosValidOrElse(pos,
          () -> chunkRenderCache.getTileEntity(pos),
          () -> null);
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        return this.whenPosValidOrElse(pos,
          () -> chunkRenderCache.getBlockState(pos),
          Blocks.AIR::getDefaultState);
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return this.whenPosValidOrElse(pos,
          () -> chunkRenderCache.getFluidState(pos),
          Fluids.EMPTY::getDefaultState
        );
    }

    private boolean falseWhenInvalidPos(final BlockPos pos, final Supplier<Boolean> validSupplier) {
        return this.whenPosValidOrElse(pos, validSupplier, () -> false);
    }

    private <T> T whenPosValidOrElse(final BlockPos pos, final Supplier<T> validSupplier, final Supplier<T> invalidSupplier) {
        if (pos.getY() < 0 || pos.getY() > 255) {
            return invalidSupplier.get();
        }

        try {
            return validSupplier.get();
        }
        catch (Exception e)
        {
            Log.logError("Failed to process cached wrapped info for: " + pos, e);
            return invalidSupplier.get();
        }
    }
}
