package mod.chiselsandbits.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventBlockBitModification extends Event
{

	private final World w;
	private final BlockPos pos;
	private final EntityPlayer player;
	private final EnumHand hand;
	private final ItemStack stackUsed;
	private final boolean placement;

	public EventBlockBitModification(
			final World w,
			final BlockPos pos,
			final EntityPlayer player,
			final EnumHand hand,
			final ItemStack stackUsed,
			final boolean placement )
	{

		this.w = w;
		this.pos = pos;
		this.player = player;
		this.hand = hand;
		this.stackUsed = stackUsed;
		this.placement = placement;
	}

	public World getWorld()
	{
		return w;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public EntityPlayer getPlayer()
	{
		return player;
	}

	public EnumHand getHand()
	{
		return hand;
	}

	public ItemStack getItemUsed()
	{
		return stackUsed;
	}

	public boolean isPlacing()
	{
		return placement;
	}

}
