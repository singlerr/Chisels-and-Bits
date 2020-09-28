package mod.chiselsandbits.crafting;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.registry.ModItems;
import mod.chiselsandbits.registry.ModRecipeSerializers;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class BagDyeing extends SpecialRecipe
{

	public BagDyeing(
			ResourceLocation name )
	{
		super( name );
	}

	private static class dyed_output
	{
		ItemStack bag;
		DyeColor  color;

		public dyed_output(
				ItemStack bag,
          DyeColor dye )
		{
			this.bag = bag;
			this.color = dye;
		}

	};

	@Override
	public ItemStack getCraftingResult(
			CraftingInventory inv )
	{
		dyed_output output = getOutput( inv );

		if ( output != null )
		{
			return ModItems.ITEM_BIT_BAG_DEFAULT.get().dyeBag( output.bag, output.color );
		}

		return ModUtil.getEmptyStack();
	}

	private dyed_output getOutput(
      CraftingInventory inv )
	{
		ItemStack bag = null;
		ItemStack dye = null;

		for ( int x = 0; x < inv.getSizeInventory(); ++x )
		{
			ItemStack is = inv.getStackInSlot( x );
			if ( is != null && !ModUtil.isEmpty( is ) )
			{
				if ( is.getItem() == Items.WATER_BUCKET || getDye( is ) != null )
				{
					if ( dye == null )
						dye = is;
					else
						return null;
				}
				else if ( is.getItem() instanceof ItemBitBag )
				{
					if ( bag == null )
						bag = is;
					else
						return null;
				}
				else
					return null;
			}
		}

		if ( bag != null && dye != null )
		{
			return new dyed_output( bag, getDye( dye ) );
		}

		return null;
	}

	private DyeColor getDye(
			ItemStack is )
	{
		if (is.getItem() instanceof DyeItem) {
		    final DyeItem item = (DyeItem) is.getItem();
		    return item.getDyeColor();
        }

		return null;
	}

    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        return getOutput( inv ) != null;
    }

    @Override
    public boolean canFit(final int width, final int height)
    {
        return width * height >= 2;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.BAG_DYEING.get();
    }
}
