package mod.chiselsandbits.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EventBlockBitModification extends Event
{

	private final World        w;
	private final BlockPos     pos;
	private final PlayerEntity player;
	private final Hand         hand;
	private final ItemStack    stackUsed;
	private final boolean      placement;

	public EventBlockBitModification(
			final World w,
			final BlockPos pos,
			final PlayerEntity player,
			final Hand hand,
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

	public PlayerEntity getPlayer()
	{
		return player;
	}

	public Hand getHand()
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
