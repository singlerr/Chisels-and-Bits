package mod.chiselsandbits.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventFullBlockRestoration extends Event
{

	private final World w;
	private final BlockPos pos;
	private final IBlockState restoredState;

	public EventFullBlockRestoration(
			final World w,
			final BlockPos pos,
			final IBlockState restoredState )
	{

		this.w = w;
		this.pos = pos;
		this.restoredState = restoredState;
	}

	public World getWorld()
	{
		return w;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public IBlockState getState()
	{
		return restoredState;
	}

}
