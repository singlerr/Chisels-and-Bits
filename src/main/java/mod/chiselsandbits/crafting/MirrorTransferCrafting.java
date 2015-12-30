package mod.chiselsandbits.crafting;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class MirrorTransferCrafting implements IRecipe
{

	@Override
	public boolean matches(
			final InventoryCrafting craftingInv,
			final World worldIn )
	{
		return analzyeCraftingInventory( craftingInv, true ) != null;
	}

	public ItemStack analzyeCraftingInventory(
			final InventoryCrafting craftingInv,
			final boolean generatePattern )
	{
		ItemStack targetA = null;
		ItemStack targetB = null;

		boolean isNegative = false;

		for ( int x = 0; x < craftingInv.getSizeInventory(); x++ )
		{
			final ItemStack f = craftingInv.getStackInSlot( x );
			if ( f == null )
			{
				continue;
			}

			if ( f.getItem() == ChiselsAndBits.instance.items.itemMirrorprint )
			{
				if ( f.hasTagCompound() )
				{
					if ( targetA != null )
					{
						return null;
					}

					targetA = f;
				}
				else
				{
					return null;
				}
			}

			else if ( f.getItem() == ChiselsAndBits.instance.items.itemNegativeprint )
			{
				if ( !f.hasTagCompound() )
				{
					if ( targetB != null )
					{
						return null;
					}

					isNegative = true;
					targetB = f;
				}
				else
				{
					return null;
				}
			}
			else if ( f.getItem() == ChiselsAndBits.instance.items.itemPositiveprint )
			{
				if ( !f.hasTagCompound() )
				{
					if ( targetB != null )
					{
						return null;
					}

					isNegative = false;
					targetB = f;
				}
				else
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}

		if ( targetA != null && targetB != null )
		{
			if ( generatePattern )
			{
				return targetA;
			}

			final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
			tmp.readChisleData( targetA.getTagCompound() );

			final VoxelBlob bestBlob = tmp.getBlob();

			if ( isNegative )
			{
				bestBlob.binaryReplacement( 0, Block.getStateId( Blocks.stone.getDefaultState() ) );
			}

			tmp.setBlob( bestBlob );

			final NBTTagCompound comp = (NBTTagCompound) targetA.getTagCompound().copy();
			tmp.writeChisleData( comp );

			final ItemStack outputPattern = new ItemStack( targetB.getItem() );
			outputPattern.setTagCompound( comp );

			return outputPattern;
		}

		return null;
	}

	@Override
	public ItemStack getCraftingResult(
			final InventoryCrafting inv )
	{
		return analzyeCraftingInventory( inv, false );
	}

	@Override
	public int getRecipeSize()
	{
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return null; // nope
	}

	@Override
	public ItemStack[] getRemainingItems(
			final InventoryCrafting p_179532_1_ )
	{
		final ItemStack[] aitemstack = new ItemStack[p_179532_1_.getSizeInventory()];

		for ( int i = 0; i < aitemstack.length; ++i )
		{
			final ItemStack itemstack = p_179532_1_.getStackInSlot( i );
			if ( itemstack != null && itemstack.getItem() == ChiselsAndBits.instance.items.itemMirrorprint && itemstack.hasTagCompound() )
			{
				itemstack.stackSize++;
			}
		}

		return aitemstack;
	}

}
