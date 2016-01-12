package mod.chiselsandbits.core.api;

import java.util.HashMap;
import java.util.Map;

import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class BitAccess implements IBitAccess
{

	private final World w;
	private final BlockPos pos;
	private final VoxelBlob blob;

	private final Map<Integer, IBitBrush> brushes = new HashMap<Integer, IBitBrush>();

	public BitAccess(
			final World w,
			final BlockPos pos,
			final VoxelBlob blob )
	{
		this.w = w;
		this.pos = pos;
		this.blob = blob;
	}

	@Override
	public IBitBrush getBitAt(
			final int x,
			final int y,
			final int z )
	{
		final int state = blob.get( x, y, z );

		IBitBrush brush = brushes.get( state );

		if ( brush == null )
		{
			brushes.put( state, brush = new BitBrush( state ) );
		}

		return brush;
	}

	@Override
	public void setBitAt(
			final int x,
			final int y,
			final int z,
			final IBitBrush bit ) throws SpaceOccupied
	{
		int state = 0;

		if ( bit instanceof BitBrush )
		{
			state = ( (BitBrush) bit ).stateID;
		}

		blob.set( x, y, z, state );
	}

	@Override
	public void commitChanges()
	{
		// TODO IMPLEMENT
	}

	@Override
	public ItemStack getBitsAsItem()
	{
		// TODO IMPLEMENT
		return null;
	}

}
