package mod.chiselsandbits.crafting;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.registry.ModRecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ChiselCrafting extends SpecialRecipe
{

	public ChiselCrafting(
			ResourceLocation name )
	{
		super( name );
	}

	/**
	 * Find the bag and pattern...
	 *
	 * @param inv
	 * @return
	 */
	private ChiselCraftingRequirements getCraftingReqs(
			final CraftingInventory inv,
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

			if ( is.getItem() == ModItems.ITEM_POSITIVE_PRINT_WRITTEN.get() && pattern == null )
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
			else if ( !ModUtil.isEmpty( is ) )
			{
				return null;
			}
		}

		if ( pattern == null || pattern.hasTag() == false )
		{
			return null;
		}

		final ChiselCraftingRequirements r = new ChiselCraftingRequirements( inv, pattern, copy );
		if ( r.isValid() )
		{
			return r;
		}

		return null;
	}

	@Override
	public boolean matches(
			final CraftingInventory inv,
			final World worldIn )
	{
		return getCraftingReqs( inv, true ) != null;
	}

	@Override
	public ItemStack getCraftingResult(
			final CraftingInventory inv )
	{
		final ChiselCraftingRequirements req = getCraftingReqs( inv, true );

		if ( req != null )
		{
			return ModItems.ITEM_POSITIVE_PRINT_WRITTEN.get().getPatternedItem( req.pattern, true );
		}

		return ModUtil.getEmptyStack();
	}

	@Override
	public boolean canFit(
			final int width,
			final int height )
	{
		return width * height > 3;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		// no inputs, means no output.
		return ModUtil.getEmptyStack();
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(
			final CraftingInventory inv )
	{
		final NonNullList<ItemStack> out = NonNullList.withSize( inv.getSizeInventory(), ItemStack.EMPTY );

		// just getting this will alter the stacks..
		final ChiselCraftingRequirements r = getCraftingReqs( inv, false );

		if ( inv.getSizeInventory() != r.pile.length )
		{
			throw new RuntimeException( "Inventory Changed Size!" );
		}

		for ( int x = 0; x < r.pile.length; x++ )
		{

			if ( r.pile[x] != null && ModUtil.getStackSize( r.pile[x] ) > 0 )
			{
				out.set( x, r.pile[x] );
			}
		}

		return out;
	}

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.CHISEL_CRAFTING.get();
    }
}
