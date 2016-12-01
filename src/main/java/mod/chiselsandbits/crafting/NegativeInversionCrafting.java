package mod.chiselsandbits.crafting;

import mod.chiselsandbits.chiseledblock.NBTBlobConverter;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class NegativeInversionCrafting implements IRecipe
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

		for ( int x = 0; x < craftingInv.getSizeInventory(); x++ )
		{
			final ItemStack f = craftingInv.getStackInSlot( x );
			if ( f == null )
			{
				continue;
			}

			if ( f.getItem() == ChiselsAndBits.getItems().itemNegativeprint )
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
					if ( targetB != null )
					{
						return null;
					}

					targetB = f;
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

			final NBTBlobConverter tmp = new NBTBlobConverter();
			tmp.readChisleData( targetA.getTagCompound() );

			final VoxelBlob bestBlob = tmp.getBlob();
			bestBlob.binaryReplacement( ModUtil.getStateId( Blocks.STONE.getDefaultState() ), 0 );

			tmp.setBlob( bestBlob );

			final NBTTagCompound comp = targetA.getTagCompound().copy();
			tmp.writeChisleData( comp, false );

			final ItemStack outputPattern = new ItemStack( targetA.getItem() );
			outputPattern.setTagCompound( comp );

			return outputPattern;
		}

		return null;
	}

	@Override
	public ItemStack getCraftingResult(
			final InventoryCrafting craftingInv )
	{
		return analzyeCraftingInventory( craftingInv, false );
	}

	@Override
	public int getRecipeSize()
	{
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return ModUtil.getEmptyStack(); // nope
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(
			final InventoryCrafting craftingInv )
	{
		final NonNullList<ItemStack> aitemstack = NonNullList.func_191197_a( craftingInv.getSizeInventory(), ItemStack.field_190927_a );

		for ( int i = 0; i < aitemstack.size(); ++i )
		{
			final ItemStack itemstack = craftingInv.getStackInSlot( i );
			if ( itemstack != null && itemstack.getItem() == ChiselsAndBits.getItems().itemNegativeprint && itemstack.hasTagCompound() )
			{
				ModUtil.adjustStackSize( itemstack, 1 );
			}
		}

		return aitemstack;
	}

}
