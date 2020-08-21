package mod.chiselsandbits.registry;

import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.debug.ItemApiDebug;
import mod.chiselsandbits.items.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTier;

public class ModItems extends ModRegistry
{

	final public ItemChisel itemChiselStone;
	final public ItemChisel itemChiselIron;
	final public ItemChisel itemChiselGold;
	final public ItemChisel itemChiselDiamond;

	final public ItemChiseledBit   itemBlockBit;
	final public ItemMirrorPrint   itemMirrorprint;
    final public ItemPositivePrint itemPositiveprint;
    final public ItemPositivePrint itemPositiveprintWritten;
	final public ItemNegativePrint itemNegativePrint;
	final public ItemNegativePrint itemNegativePrintWritten;

	final public ItemBitBag      itemBitBagDefault;
	final public ItemBitBag      itemBitBagDyed;
	final public ItemWrench      itemWrench;
	final public ItemBitSaw      itemBitSawDiamond;
	final public ItemTapeMeasure itemTapeMeasure;

	public ModItems(
			final ModConfig config )
	{
		// register items...
		itemChiselStone = registerItem( config.enableStoneChisel, new ItemChisel(ItemTier.STONE, new Item.Properties()), "chisel_stone" );
		itemChiselIron = registerItem( config.enableIronChisel, new ItemChisel( ItemTier.IRON, new Item.Properties() ), "chisel_iron" );
		itemChiselGold = registerItem( config.enableGoldChisel, new ItemChisel( ItemTier.GOLD, new Item.Properties() ), "chisel_gold" );
		itemChiselDiamond = registerItem( config.enableDiamondChisel, new ItemChisel( ItemTier.DIAMOND, new Item.Properties() ), "chisel_diamond" );
		itemPositiveprint = registerItem( config.enablePositivePrint, new ItemPositivePrint(new Item.Properties()), "positiveprint" );
        itemPositiveprintWritten = registerItem( config.enablePositivePrint, new ItemPositivePrint(new Item.Properties()), "positiveprint_written" );
		itemNegativePrint = registerItem( config.enableNegativePrint, new ItemNegativePrint(new Item.Properties()), "negativeprint" );
        itemNegativePrintWritten = registerItem( config.enableNegativePrint, new ItemNegativePrint(new Item.Properties()), "negativeprint_written");
		itemMirrorprint = registerItem( config.enableMirrorPrint, new ItemMirrorPrint(), "mirrorprint" );
		itemBitBagDefault = registerItem( config.enableBitBag, new ItemBitBag(new Item.Properties()), "bit_bag" );
        itemBitBagDyed = registerItem( config.enableBitBag, new ItemBitBag(new Item.Properties()), "bit_bag_dyed" );
		itemWrench = registerItem( config.enableWoodenWrench, new ItemWrench(), "wrench_wood" );
		itemBitSawDiamond = registerItem( config.enableBitSaw, new ItemBitSaw(), "bitsaw_diamond" );
		itemBlockBit = registerItem( config.enableChisledBits, new ItemChiseledBit(new Item.Properties()), "block_bit" );
		itemTapeMeasure = registerItem( config.enableTapeMeasure, new ItemTapeMeasure(), "tape_measure" );
		registerItem( config.enableAPITestingItem, new ItemApiDebug(), "debug" );
    }

}
