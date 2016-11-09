package mod.chiselsandbits.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EventBlockBitPostModification extends Event
{

	private final World w;
	private final BlockPos pos;

	public EventBlockBitPostModification(
			final World w,
			final BlockPos pos )
	{

		this.w = w;
		this.pos = pos;
	}

	public World getWorld()
	{
		return w;
	}

	public BlockPos getPos()
	{
		return pos;
	}

}
