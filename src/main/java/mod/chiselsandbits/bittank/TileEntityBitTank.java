package mod.chiselsandbits.bittank;

import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public class TileEntityBitTank extends TileEntity implements IItemHandler
{

	public static final int MAX_CONTENTS = 4096;

	// best conversion...
	// 125mb = 512bits
	public static final int MB_PER_BIT_CONVERSION = 125;
	public static final int BITS_PER_MB_CONVERSION = 512;

	private Fluid myFluid = null;
	private int bits = 0;

	private int oldLV = -1;

	@Override
	public void onDataPacket(
			final NetworkManager net,
			final S35PacketUpdateTileEntity pkt )
	{
		deserializeFromNBT( pkt.getNbtCompound() );
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	public Packet getDescriptionPacket()
	{
		final NBTTagCompound t = new NBTTagCompound();
		serializeToNBT( t );
		return new S35PacketUpdateTileEntity( getPos(), 0, t );
	}

	public void deserializeFromNBT(
			final NBTTagCompound compound )
	{
		final String fluid = compound.getString( "fluid" );

		if ( fluid == null || fluid.equals( "" ) )
		{
			myFluid = null;
		}
		else
		{
			myFluid = FluidRegistry.getFluid( fluid );
		}

		bits = compound.getInteger( "bits" );
	}

	public void serializeToNBT(
			final NBTTagCompound compound )
	{
		compound.setString( "fluid", myFluid == null ? "" : myFluid.getName() );
		compound.setInteger( "bits", bits );
	}

	@Override
	public void readFromNBT(
			final NBTTagCompound compound )
	{
		deserializeFromNBT( compound );
		super.readFromNBT( compound );
	}

	@Override
	public void writeToNBT(
			final NBTTagCompound compound )
	{
		serializeToNBT( compound );
		super.writeToNBT( compound );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getCapability(
			final Capability<T> capability,
			final EnumFacing facing )
	{
		if ( capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return (T) this;
		}

		return super.getCapability( capability, facing );
	}

	@Override
	public boolean hasCapability(
			final Capability<?> capability,
			final EnumFacing facing )
	{
		if ( capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
		{
			return true;
		}

		return super.hasCapability( capability, facing );
	}

	@Override
	public int getSlots()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(
			final int slot )
	{
		if ( bits > 0 && slot == 0 )
		{
			return getFluidBitStack( myFluid, bits );
		}

		return null;
	}

	public ItemStack getFluidBitStack(
			final Fluid liquid,
			final int amount )
	{
		if ( liquid == null || liquid.getBlock() == null )
		{
			return null;
		}

		return ItemChiseledBit.createStack( Block.getStateId( liquid.getBlock().getDefaultState() ), amount, false );
	}

	@Override
	public ItemStack insertItem(
			final int slot,
			final ItemStack stack,
			final boolean simulate )
	{
		if ( stack != null && stack.getItem() instanceof ItemChiseledBit )
		{
			final int state = ItemChiseledBit.getStackState( stack );
			final IBlockState blk = Block.getStateById( state );

			Fluid f = null;
			for ( final Fluid fl : FluidRegistry.getRegisteredFluids().values() )
			{
				if ( fl.getBlock() == blk.getBlock() )
				{
					f = fl;
					break;
				}
			}

			if ( f == null )
			{
				return stack;
			}

			final ItemStack bitItem = getFluidBitStack( myFluid, bits );
			final boolean canInsert = bitItem == null || ItemStack.areItemStackTagsEqual( bitItem, stack ) && bitItem.getItem() == stack.getItem();

			if ( canInsert )
			{
				final int merged = bits + stack.stackSize;
				final int amount = Math.min( merged, MAX_CONTENTS );

				if ( !simulate )
				{
					final Fluid oldFluid = myFluid;
					final int oldBits = bits;

					myFluid = f;
					bits = amount;

					if ( bits != oldBits || myFluid != oldFluid )
					{
						saveAndUpdate();
					}
				}

				if ( amount < merged )
				{
					final ItemStack out = stack.copy();
					out.stackSize = merged - amount;
					return out;
				}

				return null;
			}
		}
		return stack;
	}

	private void saveAndUpdate()
	{
		markDirty();
		worldObj.markBlockForUpdate( getPos() );

		final int lv = getLightValue();
		if ( oldLV != lv )
		{
			getWorld().checkLight( getPos() );
			oldLV = lv;
		}
	}

	/**
	 * Dosn't limit to stack size...
	 *
	 * @param slot
	 * @param amount
	 * @param simulate
	 * @return
	 */
	public ItemStack extractBits(
			final int slot,
			final int amount,
			final boolean simulate )
	{
		final ItemStack contents = getStackInSlot( slot );

		if ( contents != null && amount > 0 )
		{
			// how many to extract?
			contents.stackSize = Math.min( amount, contents.stackSize );

			// modulate?
			if ( !simulate )
			{
				final int oldBits = bits;

				bits -= contents.stackSize;
				if ( bits == 0 )
				{
					myFluid = null;
				}

				if ( bits != oldBits )
				{
					saveAndUpdate();
				}
			}

			return contents;
		}

		return null;
	}

	@Override
	public boolean shouldRenderInPass(
			final int pass )
	{
		return true;
	}

	@Override
	public ItemStack extractItem(
			final int slot,
			final int amount,
			final boolean simulate )
	{
		return extractBits( slot, Math.min( amount, ChiselsAndBits.getItems().itemBlockBit.getItemStackLimit() ), simulate );
	}

	public FluidStack getAccessableFluid()
	{
		int mb = ( bits - bits % BITS_PER_MB_CONVERSION ) / BITS_PER_MB_CONVERSION;
		mb *= MB_PER_BIT_CONVERSION;

		if ( mb > 0 && myFluid != null )
		{
			return new FluidStack( myFluid, mb );
		}

		return null;
	}

	@Override
	public boolean hasFastRenderer()
	{
		return true;
	}

	FluidStack getBitsAsFluidStack()
	{
		if ( bits > 0 && myFluid != null )
		{
			return new FluidStack( myFluid, bits );
		}

		return null;
	}

	public int getLightValue()
	{
		if ( myFluid == null || myFluid.getBlock() == null )
		{
			return 0;
		}

		final int lv = myFluid.getBlock().getLightValue();
		return lv;

	}

	boolean extractBits(
			final EntityPlayer playerIn,
			final float hitX,
			final float hitY,
			final float hitZ,
			final BlockPos pos )
	{
		if ( !playerIn.isSneaking() )
		{
			final ItemStack is = extractItem( 0, 64, false );
			if ( is != null )
			{
				ChiselsAndBits.getApi().giveBitToPlayer( playerIn, is, new Vec3( (double) hitX + pos.getX(), (double) hitY + pos.getY(), (double) hitZ + pos.getZ() ) );
			}
			return true;
		}

		return false;
	}

	boolean addAllPossibleBits(
			final EntityPlayer playerIn )
	{
		if ( playerIn.isSneaking() )
		{
			boolean change = false;
			for ( int x = 0; x < playerIn.inventory.getSizeInventory(); x++ )
			{
				final ItemStack stackInSlot = playerIn.inventory.getStackInSlot( x );
				if ( ChiselsAndBits.getApi().getItemType( stackInSlot ) == ItemType.CHISLED_BIT )
				{
					playerIn.inventory.setInventorySlotContents( x, insertItem( 0, stackInSlot, false ) );
					change = true;
				}
			}

			if ( change )
			{
				playerIn.inventory.markDirty();
			}

			return change;
		}

		return false;
	}

	boolean addHeldBits(
			final ItemStack current,
			final EntityPlayer playerIn )
	{
		if ( playerIn.isSneaking() )
		{
			if ( ChiselsAndBits.getApi().getItemType( current ) == ItemType.CHISLED_BIT )
			{
				playerIn.inventory.setInventorySlotContents( playerIn.inventory.currentItem, insertItem( 0, current, false ) );
				playerIn.inventory.markDirty();
				return true;
			}
		}

		return false;
	}

	boolean putFluidIntoItem(
			final ItemStack current,
			final FluidStack liquid,
			final EntityPlayer playerIn )
	{
		if ( FluidContainerRegistry.isEmptyContainer( current ) )
		{
			final FluidStack outFluid = getAccessableFluid();
			final int capacity = FluidContainerRegistry.getContainerCapacity( outFluid, current );

			if ( capacity % TileEntityBitTank.MB_PER_BIT_CONVERSION == 0 )
			{
				int requiredBits = capacity / TileEntityBitTank.MB_PER_BIT_CONVERSION;
				requiredBits *= TileEntityBitTank.BITS_PER_MB_CONVERSION;

				final ItemStack output = extractBits( 0, requiredBits, true );
				if ( output != null && output.stackSize == requiredBits )
				{
					outFluid.amount = capacity;
					final ItemStack filled = FluidContainerRegistry.fillFluidContainer( outFluid, current );

					if ( filled != null )
					{
						extractBits( 0, requiredBits, false );
						playerIn.inventory.setInventorySlotContents( playerIn.inventory.currentItem, filled );
						playerIn.inventory.markDirty();
					}
				}
			}
			return true;
		}

		return false;
	}

	boolean addFluidFromItem(
			final ItemStack current,
			final FluidStack liquid,
			final EntityPlayer playerIn )
	{
		if ( liquid != null && liquid.amount % TileEntityBitTank.MB_PER_BIT_CONVERSION == 0 && liquid.amount > 0 )
		{
			final int bitCount = liquid.amount * TileEntityBitTank.BITS_PER_MB_CONVERSION / TileEntityBitTank.MB_PER_BIT_CONVERSION;
			final ItemStack bitItems = getFluidBitStack( liquid.getFluid(), bitCount );

			if ( insertItem( 0, bitItems, true ) == null )
			{
				final ItemStack empty = FluidContainerRegistry.drainFluidContainer( current );

				if ( empty != null )
				{
					// insert items.. and drain the container.
					insertItem( 0, bitItems, false );
					playerIn.inventory.setInventorySlotContents( playerIn.inventory.currentItem, empty );
					playerIn.inventory.markDirty();
				}
			}
			return true;
		}

		return false;
	}
}
