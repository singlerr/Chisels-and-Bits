package mod.chiselsandbits.render.chiseledblock.tesr;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseledTESR;
import net.minecraft.util.BlockPos;

public class TileRenderChunk extends TileRenderCache
{

	private final List<TileEntityBlockChiseledTESR> tiles = new ArrayList<TileEntityBlockChiseledTESR>();
	public boolean singleInstanceMode = false;

	// upon registration convert new tiles into the correct type...
	public void register(
			final TileEntityBlockChiseledTESR which )
	{
		if ( which == null )
		{
			throw new NullPointerException();
		}

		rebuild( true );

		tiles.add( which );
	}

	@Override
	public void rebuild(
			final boolean conversion )
	{
		if ( singleInstanceMode )
		{
			for ( final TileEntityBlockChiseledTESR te : tiles )
			{
				te.getCache().rebuild( conversion );
			}

			return;
		}

		super.rebuild( conversion );
	}

	// nothing special here...
	public void unregister(
			final TileEntityBlockChiseledTESR which )
	{
		tiles.remove( which );

		super.rebuild( true );
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

	@Override
	public List<TileEntityBlockChiseledTESR> getTiles()
	{
		return tiles;
	}

}
