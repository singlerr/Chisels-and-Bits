package mod.chiselsandbits.helpers;

import javax.annotation.Nonnull;

import mod.chiselsandbits.api.EventBlockBitModification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.Consumer;

public class ActingPlayer
{
	private final IInventory storage;

	// used to test permission and stuff...
	private final PlayerEntity innerPlayer;
	private final boolean realPlayer; // are we a real player?
	private final Hand hand;

	private ActingPlayer(
			final PlayerEntity player,
			final boolean realPlayer,
			final Hand hand )
	{
		innerPlayer = player;
		this.hand = hand;
		this.realPlayer = realPlayer;
		storage = realPlayer ? player.inventory : new PlayerCopiedInventory( player.inventory );
	}

	public IInventory getInventory()
	{
		return storage;
	}

	public int getCurrentItem()
	{
		return innerPlayer.inventory.currentItem;
	}

	public boolean isCreative()
	{
		return innerPlayer.isCreative();
	}

	public ItemStack getCurrentEquippedItem()
	{
		return storage.getStackInSlot( getCurrentItem() );
	}

	// permission check cache.
	BlockPos lastPos = null;
	Boolean lastPlacement = null;
	ItemStack lastPermissionBit = null;
	Boolean permissionResult = null;

	public boolean canPlayerManipulate(
			final @Nonnull BlockPos pos,
			final @Nonnull Direction side,
			final @Nonnull ItemStack is,
			final boolean placement )
	{
		// only re-test if something changes.
		if ( permissionResult == null || lastPermissionBit != is || lastPos != pos || placement != lastPlacement )
		{
			lastPos = pos;
			lastPlacement = placement;
			lastPermissionBit = is;

			if ( innerPlayer.canPlayerEdit( pos, side, is ) && innerPlayer.getEntityWorld().isBlockModifiable( innerPlayer, pos ) )
			{
				final EventBlockBitModification event = new EventBlockBitModification( innerPlayer.getEntityWorld(), pos, innerPlayer, hand, is, placement );
				permissionResult = !MinecraftForge.EVENT_BUS.post( event );
			}
			else
			{
				permissionResult = false;
			}
		}

		return permissionResult;
	}

	public void damageItem(
			final ItemStack stack,
			final int amount )
	{
		if ( realPlayer )
		{
			stack.damageItem(amount, innerPlayer, playerEntity -> {});
		}
		else
		{
			stack.setDamage( stack.getDamage() + amount );
		}
	}

	public void playerDestroyItem(
			final @Nonnull ItemStack stack,
			final Hand hand )
	{
		if ( realPlayer )
		{
			net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem( innerPlayer, stack, hand );
		}
	}

	@Nonnull
	public static ActingPlayer actingAs(
			final PlayerEntity player,
			final Hand hand )
	{
		return new ActingPlayer( player, true, hand );
	}

	@Nonnull
	public static ActingPlayer testingAs(
			final PlayerEntity player,
			final Hand hand )
	{
		return new ActingPlayer( player, false, hand );
	}

	public World getWorld()
	{
		return innerPlayer.getEntityWorld();
	}

	/**
	 * only call this is you require a player, and only as a last resort.
	 */
	public PlayerEntity getPlayer()
	{
		return innerPlayer;
	}

	public boolean isReal()
	{
		return realPlayer;
	}

	/**
	 * @return the hand
	 */
	public Hand getHand()
	{
		return hand;
	}

}
