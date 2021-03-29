package mod.chiselsandbits.api.events;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EventFullBlockRestoration extends Event
{

	private final World      w;
	private final BlockPos   pos;
	private final BlockState restoredState;

	public EventFullBlockRestoration(
			final World w,
			final BlockPos pos,
			final BlockState restoredState )
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

	public BlockState getState()
	{
		return restoredState;
	}

}
