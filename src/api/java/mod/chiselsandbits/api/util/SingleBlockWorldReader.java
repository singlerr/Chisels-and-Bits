package mod.chiselsandbits.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.lighting.WorldLightManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class SingleBlockWorldReader extends SingleBlockBlockReader implements IWorldReader
{
    private final IWorldReader reader;

    public SingleBlockWorldReader(final BlockState state, final Block blk, final IWorldReader reader)
    {
        super(state, blk, reader);
        this.reader = reader;
    }

    public SingleBlockWorldReader(final BlockState state, final IWorldReader reader)
    {
        super(state, state.getBlock(), reader);
        this.reader = reader;
    }

    public SingleBlockWorldReader(final BlockState state, final Block blk, final BlockPos pos, final IWorldReader reader)
    {
        super(state, blk, pos, reader);
        this.reader = reader;
    }

    public SingleBlockWorldReader(final BlockState state, final BlockPos pos, final IWorldReader reader)
    {
        super(state, pos, reader);
        this.reader = reader;
    }

    @Nullable
    @Override
    public IChunk getChunk(final int x, final int z, @NotNull final ChunkStatus requiredStatus, final boolean nonnull)
    {
        return this.reader.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public boolean hasChunk(final int chunkX, final int chunkZ)
    {
        return this.reader.hasChunk(chunkX, chunkZ);
    }

    @Override
    public int getHeight(@NotNull final Heightmap.Type heightmapType, final int x, final int z)
    {
        return this.reader.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkyDarken()
    {
        return 15;
    }

    @NotNull
    @Override
    public BiomeManager getBiomeManager()
    {
        return this.reader.getBiomeManager();
    }

    @NotNull
    @Override
    public Biome getUncachedNoiseBiome(final int x, final int y, final int z)
    {
        return this.reader.getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public boolean isClientSide()
    {
        return this.reader.isClientSide();
    }

    @Override
    public int getSeaLevel()
    {
        return this.reader.getSeaLevel();
    }

    @NotNull
    @Override
    public DimensionType dimensionType()
    {
        return this.reader.dimensionType();
    }

    @Override
    public float getShade(@NotNull final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return this.reader.getShade(p_230487_1_, p_230487_2_);
    }

    @NotNull
    @Override
    public WorldLightManager getLightEngine()
    {
        return this.reader.getLightEngine();
    }

    @NotNull
    @Override
    public WorldBorder getWorldBorder()
    {
        return this.reader.getWorldBorder();
    }

    @NotNull
    @Override
    public Stream<VoxelShape> getEntityCollisions(
      @Nullable final Entity p_230318_1_, @NotNull final AxisAlignedBB p_230318_2_, @NotNull final Predicate<Entity> p_230318_3_)
    {
        return this.reader.getEntityCollisions(p_230318_1_, p_230318_2_, p_230318_3_);
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(@NotNull final BlockPos pos)
    {
        if (pos == this.pos && blk.hasTileEntity(state))
        {
            return blk.createTileEntity(state, this);
        }

        return this.reader.getBlockEntity(pos);
    }

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        if (pos == this.pos)
        {
            return state;
        }
        return this.reader.getBlockState(pos);
    }

    @NotNull
    @Override
    public FluidState getFluidState(@NotNull final BlockPos pos)
    {
        return this.getBlockState(pos).getFluidState();
    }
}
