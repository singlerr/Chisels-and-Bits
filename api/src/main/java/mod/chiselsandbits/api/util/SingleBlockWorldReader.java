package mod.chiselsandbits.api.util;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class SingleBlockWorldReader extends SingleBlockBlockReader implements LevelReader
{
    private final LevelReader reader;

    public SingleBlockWorldReader(final IBlockInformation blockInformation, final Block blk, final LevelReader reader)
    {
        super(blockInformation, blk, reader);
        this.reader = reader;
    }

    public SingleBlockWorldReader(final IBlockInformation blockInformation, final LevelReader reader)
    {
        super(blockInformation, blockInformation.getBlockState().getBlock(), reader);
        this.reader = reader;
    }

    public SingleBlockWorldReader(final IBlockInformation blockInformation, final Block blk, final BlockPos pos, final LevelReader reader)
    {
        super(blockInformation, blk, pos, reader);
        this.reader = reader;
    }

    public SingleBlockWorldReader(final IBlockInformation blockInformation, final BlockPos pos, final LevelReader reader)
    {
        super(blockInformation, pos, reader);
        this.reader = reader;
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(final int x, final int z, @NotNull final ChunkStatus requiredStatus, final boolean nonnull)
    {
        return this.reader.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public boolean hasChunk(final int chunkX, final int chunkZ)
    {
        return this.reader.hasChunk(chunkX, chunkZ);
    }

    @Override
    public int getHeight(@NotNull final Heightmap.Types heightmapType, final int x, final int z)
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
    public Holder<Biome> getUncachedNoiseBiome(final int x, final int y, final int z)
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
    public LevelLightEngine getLightEngine()
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
    public List<VoxelShape> getEntityCollisions(@Nullable final Entity entity, final @NotNull AABB aabb)
    {
        return this.reader.getEntityCollisions(entity, aabb);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(@NotNull final BlockPos pos)
    {
        if (pos == this.pos && blk instanceof EntityBlock)
        {
            return ((EntityBlock) blk).newBlockEntity(this.pos, blockInformation.getBlockState());
        }

        return this.reader.getBlockEntity(pos);
    }

    @NotNull
    @Override
    public BlockState getBlockState(@NotNull final BlockPos pos)
    {
        if (pos == this.pos)
        {
            return blockInformation.getBlockState();
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
