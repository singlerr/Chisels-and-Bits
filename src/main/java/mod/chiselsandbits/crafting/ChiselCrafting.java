package mod.chiselsandbits.crafting;

import java.util.ArrayList;
import java.util.HashMap;

import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.IntegerRef;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ChiselCrafting implements IRecipe
{

	class CCReq
	{
		private final NBTTagCompound patternTag;
		private final ItemStack pattern;
		private final VoxelBlob voxelBlob;

		private Boolean isValid = null;

		private final ItemStack[] pile;
		private final ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		private final ArrayList<BagInventory> bags = new ArrayList<BagInventory>();

		public CCReq(
				final IInventory inv,
				final ItemStack pattern,
				final boolean copy )
		{
			pile = new ItemStack[inv.getSizeInventory()];
			this.pattern = pattern;
			patternTag = pattern.getTagCompound();

			for ( int x = 0; x < inv.getSizeInventory(); x++ )
			{
				final ItemStack is = inv.getStackInSlot( x );
				pile[x] = is;

				if ( !copy )
				{
					// if we are not copying.. then we remove it...
					inv.setInventorySlotContents( x, null );
				}

				if ( is == null )
				{
					continue;
				}

				if ( is.getItem() instanceof ItemBitBag )
				{
					bags.add( new BagInventory( copy ? is.copy() : is ) );
				}

				if ( is.getItem() instanceof ItemChiseledBit )
				{
					stacks.add( copy ? is.copy() : is );
				}
			}

			final TileEntityBlockChiseled tec = new TileEntityBlockChiseled();
			tec.readChisleData( patternTag );
			voxelBlob = tec.getBlob();
		}

		public boolean isValid()
		{
			if ( isValid != null )
			{
				return isValid;
			}

			final HashMap<Integer, IntegerRef> count = voxelBlob.getBlockCounts();

			isValid = true;
			for ( final IntegerRef ref : count.values() )
			{
				if ( ref.ref != 0 )
				{

					for ( final ItemStack is : stacks )
					{
						if ( ItemChiseledBit.getStackState( is ) == ref.ref && is.stackSize > 0 )
						{
							final int original = is.stackSize;
							is.stackSize = Math.max( 0, is.stackSize - ref.total );
							ref.total -= original - is.stackSize;
						}
					}

					for ( final BagInventory bag : bags )
					{
						ref.total -= bag.extractBit( ref.ref, ref.total );
					}

					if ( ref.total > 0 )
					{
						isValid = false;
						break;
					}
				}
			}
			return isValid;
		}
	};

	/**
	 * Find the bag and pattern...
	 *
	 * @param inv
	 * @return
	 */
	private CCReq getCraftingReqs(
			final InventoryCrafting inv,
			final boolean copy )
	{
		ItemStack pattern = null;

		for ( int x = 0; x < inv.getSizeInventory(); x++ )
		{
			final ItemStack is = inv.getStackInSlot( x );

			if ( is == null )
			{
				continue;
			}

			if ( is.getItem() == ChiselsAndBits.getItems().itemPositiveprint && pattern == null )
			{
				pattern = is;
			}
			else if ( is.getItem() instanceof ItemBitBag )
			{
				continue;
			}
			else if ( is.getItem() instanceof ItemChiseledBit )
			{
				continue;
			}
			else
			{
				return null;
			}
		}

		if ( pattern == null || pattern.hasTagCompound() == false )
		{
			return null;
		}

		final CCReq r = new CCReq( inv, pattern, copy );
		if ( r.isValid() )
		{
			return r;
		}

		return null;
	}

	@Override
	public boolean matches(
			final InventoryCrafting inv,
			final World worldIn )
	{
		return getCraftingReqs( inv, true ) != null;
	}

	@Override
	public ItemStack getCraftingResult(
			final InventoryCrafting inv )
	{
		final CCReq req = getCraftingReqs( inv, true );

		if ( req != null )
		{
			return ChiselsAndBits.getItems().itemPositiveprint.getPatternedItem( req.pattern );
		}

		return null;
	}

	@Override
	public int getRecipeSize()
	{
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		// no inputs, means no output.
		return null;
	}

	@Override
	public ItemStack[] getRemainingItems(
			final InventoryCrafting inv )
	{
		final ItemStack[] out = new ItemStack[inv.getSizeInventory()];

		// just getting this will alter the stacks..
		final CCReq r = getCraftingReqs( inv, false );

		if ( inv.getSizeInventory() != r.pile.length )
		{
			throw new RuntimeException( "Inventory Changed Size!" );
		}

		for ( int x = 0; x < r.pile.length; x++ )
		{
			out[x] = r.pile[x];

			if ( out[x] != null && out[x].stackSize <= 0 )
			{
				out[x] = null;
			}
		}

		return out;
	}

}
