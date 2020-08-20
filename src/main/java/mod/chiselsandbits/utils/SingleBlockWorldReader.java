package mod.chiselsandbits.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
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
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class SingleBlockWorldReader extends SingleBlockBlockReader implements IWorldReader
{
    private final IWorldReader reader;

    public SingleBlockWorldReader(final BlockState state, final Block blk, final IWorldReader reader)
    {
        super(state, blk);
        this.reader = reader;
    }

    @Nullable
    @Override
    public IChunk getChunk(final int x, final int z, final ChunkStatus requiredStatus, final boolean nonnull)
    {
        return this.reader.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public boolean chunkExists(final int chunkX, final int chunkZ)
    {
        return this.reader.chunkExists(chunkX, chunkZ);
    }

    @Override
    public int getHeight(final Heightmap.Type heightmapType, final int x, final int z)
    {
        return this.reader.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkylightSubtracted()
    {
        return 15;
    }

    @Override
    public BiomeManager getBiomeManager()
    {
        return this.reader.getBiomeManager();
    }

    @Override
    public Biome getNoiseBiomeRaw(final int x, final int y, final int z)
    {
        return this.reader.getNoiseBiomeRaw(x, y, z);
    }

    @Override
    public boolean isRemote()
    {
        return this.reader.isRemote();
    }

    @Override
    public int getSeaLevel()
    {
        return 64;
    }

    @Override
    public DimensionType func_230315_m_()
    {
        return this.reader.func_230315_m_();
    }

    @Override
    public float func_230487_a_(final Direction p_230487_1_, final boolean p_230487_2_)
    {
        return this.reader.func_230487_a_(p_230487_1_, p_230487_2_);
    }

    @Override
    public WorldLightManager getLightManager()
    {
        return this.reader.getLightManager();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        return this.reader.getWorldBorder();
    }

    @Override
    public Stream<VoxelShape> func_230318_c_(
      @Nullable final Entity p_230318_1_, final AxisAlignedBB p_230318_2_, final Predicate<Entity> p_230318_3_)
    {
        return this.reader.func_230318_c_(p_230318_1_, p_230318_2_, p_230318_3_);
    }
}
