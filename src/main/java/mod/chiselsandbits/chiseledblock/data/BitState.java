package mod.chiselsandbits.chiseledblock.data;

import mod.chiselsandbits.api.IBitState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class BitState implements IBitState
{

	protected int used = 0;

	final public VoxelBlob blob;
	final public int index;
	final private int stateID;
	final private IBlockState state;
	final private IStringSerializable extra;

	public BitState(
			final VoxelBlob blob,
			final int index,
			final IBlockState state,
			final IStringSerializable extra )
	{
		this.blob = blob;
		this.index = index;
		this.state = state;
		this.extra = extra;
		this.stateID = Block.getStateId( state );
	}

	public BitState(
			final VoxelBlob blob,
			final int index,
			final BitState state )
	{
		this.blob = blob;
		this.index = index;
		this.state = state.state;
		this.extra = state.extra;
		this.stateID = state.stateID;
	}

	public BitState(
			final int stateID,
			final IBlockState state )
	{
		this.blob = null;
		this.index = -1;
		this.state = state;
		this.stateID = stateID;
		this.extra = null;
	}

	public BitState(
			final Block stone )
	{
		this( Block.getStateId( stone.getDefaultState() ), stone.getDefaultState() );
	}

	@Override
	public IBlockState getBlockState()
	{
		return state;
	}

	@Override
	public int getStateID()
	{
		return stateID;
	}

	@Override
	public IStringSerializable getExtra()
	{
		return extra;
	}

	@Override
	public boolean isEmpty()
	{
		return stateID == 0;
	}

	@Override
	public boolean isFilled()
	{
		return stateID != 0;
	}
}
