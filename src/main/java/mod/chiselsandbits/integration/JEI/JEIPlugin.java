package mod.chiselsandbits.integration.JEI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IItemBlacklist;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.integration.Integration;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

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
		final ArrayList<ItemStack> chiseles = new ArrayList<ItemStack>();
		addList( chiseles, itemToItemstack( ChiselsAndBits.instance.items.itemChiselDiamond ) );
		addList( chiseles, itemToItemstack( ChiselsAndBits.instance.items.itemChiselGold ) );
		addList( chiseles, itemToItemstack( ChiselsAndBits.instance.items.itemChiselIron ) );
		addList( chiseles, itemToItemstack( ChiselsAndBits.instance.items.itemChiselStone ) );

		final ArrayList<ItemStack> blocks = new ArrayList<ItemStack>();
		for ( final Block blk : ChiselsAndBits.instance.blocks.getConversions().values() )
		{
			addList( blocks, blockToItemstack( blk ) );
		}

		addDescription( registry, chiseles, LocalStrings.LongHelpChisel );
		addDescription( registry, blocks, LocalStrings.LongHelpChiseledBlock );

		addDescription( registry, stackCollection( ChiselsAndBits.instance.items.itemBitBag ), LocalStrings.LongHelpBitBag );
		addDescription( registry, stackCollection( ChiselsAndBits.instance.items.itemBlockBit ), LocalStrings.LongHelpBit );
		addDescription( registry, stackCollection( ChiselsAndBits.instance.items.itemMirrorprint ), LocalStrings.LongHelpMirrorPrint );
		addDescription( registry, stackCollection( ChiselsAndBits.instance.items.itemNegativeprint ), LocalStrings.LongHelpNegativePrint );
		addDescription( registry, stackCollection( ChiselsAndBits.instance.items.itemPositiveprint ), LocalStrings.LongHelpPositivePrint );
		addDescription( registry, stackCollection( ChiselsAndBits.instance.items.itemWrench ), LocalStrings.LongHelpWrench );
	}

	private void addDescription(
			final IModRegistry registry,
			final List<ItemStack> iscol,
			final LocalStrings local )
	{
		if ( iscol != null && iscol.size() > 0 )
		{
			registry.addDescription( iscol, local.toString() );
		}
	}

	private void addList(
			final ArrayList<ItemStack> items,
			final ItemStack itemStack )
	{
		if ( itemStack != null )
		{
			items.add( itemStack );
		}
	}

	private List<ItemStack> stackCollection(
			final Item it )
	{
		if ( it == null )
		{
			return null;
		}

		return Collections.singletonList( itemToItemstack( it ) );
	}

	private ItemStack blockToItemstack(
			final Block blk )
	{
		if ( blk == null )
		{
			return null;
		}

		return new ItemStack( blk, 1, OreDictionary.WILDCARD_VALUE );
	}

	private ItemStack itemToItemstack(
			final Item it )
	{
		if ( it == null )
		{
			return null;
		}

		return new ItemStack( it, 1, OreDictionary.WILDCARD_VALUE );
	}

	@Override
	public void onRecipeRegistryAvailable(
			final IRecipeRegistry recipeRegistry )
	{

	}

}
