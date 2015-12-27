package mod.chiselsandbits.integration.JEI;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mod.chiselsandbits.integration.Integration;
import net.minecraft.item.ItemStack;

@mezz.jei.api.JEIPlugin
public class JEIPlugin implements IModPlugin
{

	@Override
	public boolean isModLoaded()
	{
		return true;
	}

	@Override
	public void onJeiHelpersAvailable(
			final IJeiHelpers jeiHelpers )
	{
		final IItemBlacklist bl = jeiHelpers.getItemBlacklist();

		for ( final ItemStack is : Integration.jei.getBlacklisted() )
		{
			bl.addItemToBlacklist( is );
		}
	}

	@Override
	public void onItemRegistryAvailable(
			final IItemRegistry itemRegistry )
	{

	}

	@Override
	public void register(
			final IModRegistry registry )
	{

	}

	@Override
	public void onRecipeRegistryAvailable(
			final IRecipeRegistry recipeRegistry )
	{

	}

}
