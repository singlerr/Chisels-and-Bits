package mod.chiselsandbits.chiseledblock;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.chiseledblock.data.VoxelNeighborRenderTracker;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.chiseledblock.tesr.TileRenderCache;
import mod.chiselsandbits.render.chiseledblock.tesr.TileRenderChunk;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TileEntityBlockChiseledTESR extends TileEntityBlockChiseled
{
    public static final ModelProperty<VoxelBlobStateReference> MP_VBSR = new ModelProperty<>();
    public static final ModelProperty<VoxelNeighborRenderTracker> MP_VNRT = new ModelProperty<>();
    public static final ModelProperty<Integer> MP_PBSI = new ModelProperty<>();

	private TileRenderChunk renderChunk;
	private TileRenderCache singleCache;
	private int previousLightLevel = -1;

    public TileEntityBlockChiseledTESR(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

	public TileRenderChunk getRenderChunk()
	{
		return renderChunk;
	}

    @Override
    protected void onDataUpdate()
    {
        ModelDataManager.requestModelDataRefresh(this);
    }

    @Override
	protected void tesrUpdate(
			final IBlockReader access,
			final VoxelNeighborRenderTracker vns )
	{
		if ( renderChunk == null )
		{
			renderChunk = findRenderChunk( access );
			renderChunk.register( this );
		}

		renderChunk.update( null, 1 );

		final int old = previousLightLevel;
		previousLightLevel = getWorld().getLightFor( LightType.BLOCK, getPos() );

		if ( previousLightLevel != old )
		{
			vns.triggerUpdate();
		}

		if ( vns.isShouldUpdate() )
		{
			renderChunk.rebuild( false );
		}
	}

	private TileRenderChunk findRenderChunk(
			final IBlockReader access )
	{
		int chunkPosX = getPos().getX();
		int chunkPosY = getPos().getY();
		int chunkPosZ = getPos().getZ();

		final int mask = ~0xf;
		chunkPosX = chunkPosX & mask;
		chunkPosY = chunkPosY & mask;
		chunkPosZ = chunkPosZ & mask;

		for ( int x = 0; x < 16; ++x )
		{
			for ( int y = 0; y < 16; ++y )
			{
				for ( int z = 0; z < 16; ++z )
				{
					final TileEntityBlockChiseled te = ModUtil.getChiseledTileEntity( access, new BlockPos( chunkPosX + x, chunkPosY + y, chunkPosZ + z ) );
					if ( te instanceof TileEntityBlockChiseledTESR )
					{
						final TileRenderChunk trc = ( (TileEntityBlockChiseledTESR) te ).renderChunk;
						if ( trc != null )
						{
							return trc;
						}
					}
				}
			}
		}

		return new TileRenderChunk();
	}

	public TileRenderCache getCache()
	{
		final TileEntityBlockChiseledTESR self = this;

		if ( singleCache == null )
		{
			singleCache = new TileRenderCache() {

				@Override
				public List<TileEntityBlockChiseledTESR> getTileList()
				{
					return Collections.singletonList( self );
				}

			};
		}

		return singleCache;
	}

	private void detatchRenderer()
	{
		if ( renderChunk != null )
		{
			renderChunk.unregister( this );
			renderChunk = null;
		}
	}

    @Override
    public void remove()
    {
        super.remove();
        detatchRenderer();
    }

    @Override
    public void onChunkUnloaded()
    {
        detatchRenderer();
    }

	@Override
	protected void finalize() throws Throwable
	{
		// in a perfect world this would never happen...
		detatchRenderer();
	}

	public BlockState getBlockState(
			final IBlockReader world )
	{
		return getState( true, 0, world );
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if ( getRenderChunk() != null )
		{
			return getRenderChunk().getBounds();
		}

		return super.getRenderBoundingBox();
	}

	@Override
	public boolean isSideOpaque(
			final Direction side )
	{
		return false; // since TESRs can blink out of existence never block..
	}

	@Override
	public double getMaxRenderDistanceSquared()
	{
		return ChiselsAndBits.getConfig().getClient().dynamicModelRange.get() * ChiselsAndBits.getConfig().getClient().dynamicModelRange.get();
	}

	@Override
	public void completeEditOperation(
			final VoxelBlob vb )
	{
		super.completeEditOperation( vb );
		finishUpdate();
	}

	@Override
	public void finishUpdate()
	{
		if ( renderChunk != null )
		{
			if ( renderChunk.singleInstanceMode )
			{
				getCache().rebuild( true );
			}
			else
			{
				renderChunk.rebuild( true );
			}
		}
	}

    @NotNull
    @Override
    public IModelData getModelData()
    {
        return new ModelDataMap.Builder().withInitial(
          MP_PBSI, getPrimaryBlockStateId()
        ).withInitial(
          MP_VBSR, getBlobStateReference()
        ).withInitial(
          MP_VNRT, getNeighborRenderTracker()
        ).build();
    }
}
