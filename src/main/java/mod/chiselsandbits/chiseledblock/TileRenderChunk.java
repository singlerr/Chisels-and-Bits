package mod.chiselsandbits.chiseledblock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.ClientSide;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

public class TileRenderChunk
{

	public boolean rebuild = false;
	public int lastRenderedFrame;
	public int lastRenderedFrames[];
	public int displayList[];

	private Stopwatch lastUpdated;
	private int updates = 0;

	public boolean isDyanmic;
	private List<TileEntityBlockChiseled> tiles = new ArrayList<TileEntityBlockChiseled>();

	public TileRenderChunk()
	{
		displayList = new int[EnumWorldBlockLayer.values().length];
		lastRenderedFrames = new int[EnumWorldBlockLayer.values().length];
	}

	@Override
	protected void finalize() throws Throwable
	{
		for ( final EnumWorldBlockLayer wbl : EnumWorldBlockLayer.values() )
		{
			if ( displayList[wbl.ordinal()] != 0 )
			{
				GLAllocation.deleteDisplayLists( displayList[wbl.ordinal()] );
			}
		}
	}

	// upon registration convert new tiles into the correct type...
	void register(
			final TileEntityBlockChiseled which )
	{
		if ( which == null )
		{
			throw new NullPointerException();
		}

		rebuild = true;

		if ( which instanceof TileEntityBlockChiseledTESR && !isDyanmic )
		{
			final TileEntityBlockChiseled te = new TileEntityBlockChiseled();
			te.copyFrom( which );

			which.getWorld().setTileEntity( which.getPos(), te );
			tiles.add( te );
			return;
		}

		if ( !( which instanceof TileEntityBlockChiseledTESR ) && isDyanmic )
		{
			final TileEntityBlockChiseledTESR te = new TileEntityBlockChiseledTESR();
			te.copyFrom( which );

			which.getWorld().setTileEntity( which.getPos(), te );
			tiles.add( te );
			return;
		}

		tiles.add( which );
	}

	// nothing special here...
	void unregister(
			final TileEntityBlockChiseled which )
	{
		tiles.remove( which );
	}

	public BlockPos chunkOffset()
	{
		if ( getTiles().isEmpty() )
		{
			return new BlockPos( 0, 0, 0 );
		}

		final int bitMask = ~0xf;
		final BlockPos tilepos = getTiles().get( 0 ).getPos();
		return new BlockPos( tilepos.getX() & bitMask, tilepos.getY() & bitMask, tilepos.getZ() & bitMask );
	}

	public EnumTESRRenderState update(
			final EnumWorldBlockLayer layer,
			final int updateCost )
	{
		final int lastRF = ClientSide.instance.lastRenderedFrame;

		// update geometry ? Lighting? fish?
		if ( lastRenderedFrame != lastRF )
		{
			lastRenderedFrame = lastRF;

			if ( lastUpdated != null )
			{
				final int sec = (int) lastUpdated.elapsed( TimeUnit.SECONDS );

				if ( sec > 0 )
				{
					lastUpdated = Stopwatch.createStarted();
				}

				updates = Math.min( 30, Math.max( 0, updateCost + updates - sec ) );

				if ( updates > 20 && !isDyanmic )
				{
					final List<TileEntityBlockChiseled> oldTiles = tiles;
					setTiles( new ArrayList<TileEntityBlockChiseled>() );

					for ( final TileEntityBlockChiseled te : oldTiles )
					{
						final TileEntityBlockChiseledTESR tesr = new TileEntityBlockChiseledTESR();
						tesr.copyFrom( te );

						final World w = te.getWorld();
						w.setTileEntity( te.getPos(), tesr );
						w.markBlockForUpdate( tesr.getPos() );

						tiles.add( tesr );
					}

					isDyanmic = true;
				}
				else if ( updates < 1 && isDyanmic )
				{
					final List<TileEntityBlockChiseled> oldTiles = tiles;
					setTiles( new ArrayList<TileEntityBlockChiseled>() );

					for ( final TileEntityBlockChiseled te : oldTiles )
					{
						final TileEntityBlockChiseled notTesr = new TileEntityBlockChiseled();
						notTesr.copyFrom( te );

						final World w = te.getWorld();
						w.setTileEntity( te.getPos(), notTesr );
						w.markBlockForUpdate( notTesr.getPos() );

						tiles.add( notTesr );
					}

					isDyanmic = false;
				}
			}
			else
			{
				lastUpdated = Stopwatch.createStarted();
			}
		}

		// render?
		if ( layer != null && lastRenderedFrames[layer.ordinal()] != lastRF )
		{
			lastRenderedFrames[layer.ordinal()] = lastRF;
			return EnumTESRRenderState.RENDER;
		}

		return EnumTESRRenderState.SKIP;
	}

	public List<TileEntityBlockChiseled> getTiles()
	{
		return tiles;
	}

	private void setTiles(
			final List<TileEntityBlockChiseled> tiles )
	{
		this.tiles = tiles;
		rebuild = true;
	}

}
