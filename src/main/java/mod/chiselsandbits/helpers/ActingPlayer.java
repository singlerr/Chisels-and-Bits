package mod.chiselsandbits.helpers;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import mod.chiselsandbits.api.EventBlockBitModification;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.localization.ChiselErrors;
import mod.chiselsandbits.localization.LocalizedMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ActingPlayer
{
	private final IInventory storage;

	// used to test permission and stuff...
	private final EntityPlayer innerPlayer;
	private final boolean realPlayer; // are we a real player?
	private final EnumHand hand;
	private final boolean isCreativeMode;

	private ActingPlayer(
			final EntityPlayer player,
			final boolean realPlayer,
			final EnumHand hand )
	{
		innerPlayer = player;
		this.hand = hand;
		this.realPlayer = realPlayer;
		isCreativeMode = player.capabilities.isCreativeMode;
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
		return isCreativeMode;
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
			final @Nonnull EnumFacing side,
			final @Nonnull ItemStack is,
			final boolean placement )
	{
		// only re-test if something changes.
		if ( permissionResult == null || lastPermissionBit != is || lastPos != pos || placement != lastPlacement )
		{
			lastPos = pos;
			lastPlacement = placement;
			lastPermissionBit = is;

			if ( innerPlayer.canPlayerEdit( pos, side, is ) && innerPlayer.worldObj.isBlockModifiable( innerPlayer, pos ) )
			{
				final EventBlockBitModification event = new EventBlockBitModification( innerPlayer.worldObj, pos, innerPlayer, hand, is, placement );
				MinecraftForge.EVENT_BUS.post( event );

				permissionResult = !event.isCanceled();
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
			stack.damageItem( amount, innerPlayer );
		}
		else
		{
			stack.setItemDamage( stack.getItemDamage() + amount );
		}
	}

	public void playerDestroyItem(
			final @Nonnull ItemStack stack,
			final EnumHand hand )
	{
		if ( realPlayer )
		{
			net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem( innerPlayer, stack, hand );
		}
	}

	@Nonnull
	public static ActingPlayer actingAs(
			final EntityPlayer player,
			final EnumHand hand )
	{
		return new ActingPlayer( player, true, hand );
	}

	@Nonnull
	public static ActingPlayer testingAs(
			final EntityPlayer player,
			final EnumHand hand )
	{
		return new ActingPlayer( player, false, hand );
	}

	public World getWorld()
	{
		return innerPlayer.worldObj;
	}

	/**
	 * only call this is you require a player, and only as a last resort.
	 */
	public EntityPlayer getPlayer()
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
	public EnumHand getHand()
	{
		return hand;
	}

	public BlockPos getPosition()
	{
		return getPlayer().getPosition();
	}

	// errors produced by operations are accumulated for display.
	private final Set<LocalizedMessage> errors = new HashSet<LocalizedMessage>();

	@SideOnly( Side.CLIENT )
	private void innerDisplayError()
	{
		for ( final LocalizedMessage err : errors )
		{
			ClientSide.instance.getPlayer().addChatMessage( new TextComponentString( err.toString() ) );
		}
	}

	public void displayError()
	{
		if ( FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT )
		{
			this.innerDisplayError();
		}

		errors.clear();
	}

	public void report(
			ChiselErrors string,
			Object... vars )
	{
		if ( this.getWorld().isRemote )
		{
			errors.add( new LocalizedMessage( string, vars ) );
		}
	}

	public boolean hasBagWithRoom(
			int state,
			int requiredRoom )
	{
		IInventory inv = getInventory();
		int emptyRoom = 0;

		for ( int zz = 0; zz < inv.getSizeInventory(); zz++ )
		{
			final ItemStack which = inv.getStackInSlot( zz );

			if ( BagInventory.isBag( which ) )
			{
				BagInventory bi = new BagInventory( which );

				for ( int x = 0; x < bi.getSizeInventory(); x++ )
				{
					ItemStack g = bi.getStackInSlot( x );
					if ( ModUtil.isEmpty( g ) )
						emptyRoom += bi.getInventoryStackLimit();
					else if ( ItemChiseledBit.sameBit( g, state ) )
						emptyRoom += bi.getInventoryStackLimit() - ModUtil.getStackSize( g );
				}
			}
		}

		return emptyRoom >= requiredRoom;
	}

}
