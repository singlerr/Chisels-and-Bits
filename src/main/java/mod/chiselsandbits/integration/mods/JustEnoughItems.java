package mod.chiselsandbits.integration.mods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

@mezz.jei.api.JEIPlugin
public class JustEnoughItems implements IModPlugin
{

	@Override
	public void register(
			final IModRegistry registry )
	{
		if ( !ChiselsAndBits.getConfig().ShowBitsInJEI )
		{
			registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist( ModUtil.makeStack( ChiselsAndBits.getItems().itemBlockBit, 1, OreDictionary.WILDCARD_VALUE ) );
		}

		for ( final Block blk : ChiselsAndBits.getBlocks().getConversions().values() )
		{
			registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist( new ItemStack( blk, 1, OreDictionary.WILDCARD_VALUE ) );
		}

		final ArrayList<ItemStack> chiseles = new ArrayList<ItemStack>();
		addList( chiseles, itemToItemstack( ChiselsAndBits.getItems().itemChiselDiamond ) );
		addList( chiseles, itemToItemstack( ChiselsAndBits.getItems().itemChiselGold ) );
		addList( chiseles, itemToItemstack( ChiselsAndBits.getItems().itemChiselIron ) );
		addList( chiseles, itemToItemstack( ChiselsAndBits.getItems().itemChiselStone ) );

		final ArrayList<ItemStack> blocks = new ArrayList<ItemStack>();
		for ( final Block blk : ChiselsAndBits.getBlocks().getConversions().values() )
		{
			addList( blocks, blockToItemstack( blk ) );
		}

		addDescription( registry, chiseles, LocalStrings.LongHelpChisel );
		addDescription( registry, blocks, LocalStrings.LongHelpChiseledBlock );

		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemBitBag ), LocalStrings.LongHelpBitBag );
		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemBlockBit ), LocalStrings.LongHelpBit );
		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemMirrorprint ), LocalStrings.LongHelpMirrorPrint );
		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemNegativeprint ), LocalStrings.LongHelpNegativePrint );
		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemPositiveprint ), LocalStrings.LongHelpPositivePrint );
		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemBitSawDiamond ), LocalStrings.LongHelpBitSaw );
		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemTapeMeasure ), LocalStrings.LongHelpTapeMeasure );
		addDescription( registry, stackCollection( ChiselsAndBits.getItems().itemWrench ), LocalStrings.LongHelpWrench );
		addDescription( registry, stackCollection( ChiselsAndBits.getBlocks().blockBitTank ), LocalStrings.LongHelpBitTank );
	}

	private void addDescription(
			final IModRegistry registry,
			final List<ItemStack> iscol,
			final LocalStrings local )
	{
		if ( iscol != null && iscol.size() > 0 )
		{
			registry.addIngredientInfo( iscol, ItemStack.class, local.toString() );
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

	private List<ItemStack> stackCollection(
			final Block it )
	{
		if ( it == null )
		{
			return null;
		}

		return Collections.singletonList( blockToItemstack( it ) );
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

}
