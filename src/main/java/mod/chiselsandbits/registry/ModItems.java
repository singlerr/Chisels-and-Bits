package mod.chiselsandbits.registry;

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
    final public ItemMirrorPrint   itemMirrorPrint;
    final public ItemMirrorPrint   itemMirrorPrintWritten;
    final public ItemPositivePrint itemPositivePrint;
    final public ItemPositivePrint itemPositivePrintWritten;
	final public ItemNegativePrint itemNegativePrint;
	final public ItemNegativePrint itemNegativePrintWritten;

	final public ItemBitBag      itemBitBagDefault;
	final public ItemBitBag      itemBitBagDyed;
	final public ItemWrench      itemWrench;
	final public ItemBitSaw      itemBitSawDiamond;
	final public ItemTapeMeasure itemTapeMeasure;

	public ModItems()
	{
		// register items...
		itemChiselStone = registerItem((prop) -> new ItemChisel(ItemTier.STONE, prop), "chisel_stone" );
		itemChiselIron = registerItem((prop) -> new ItemChisel( ItemTier.IRON, prop ), "chisel_iron" );
		itemChiselGold = registerItem((prop) -> new ItemChisel( ItemTier.GOLD, prop), "chisel_gold" );
		itemChiselDiamond = registerItem((prop) -> new ItemChisel( ItemTier.DIAMOND, prop ), "chisel_diamond" );
		itemPositivePrint = registerItem(ItemPositivePrint::new, "positiveprint" );
        itemPositivePrintWritten = registerItem(ItemPositivePrint::new, "positiveprint_written" );
		itemNegativePrint = registerItem(ItemNegativePrint::new, "negativeprint" );
        itemNegativePrintWritten = registerItem(ItemNegativePrint::new, "negativeprint_written");
        itemMirrorPrint = registerItem(ItemMirrorPrint::new, "mirrorprint" );
        itemMirrorPrintWritten = registerItem(ItemMirrorPrint::new, "mirrorprint_written" );
		itemBitBagDefault = registerItem(ItemBitBag::new, "bit_bag" );
        itemBitBagDyed = registerItem(ItemBitBag::new, "bit_bag_dyed" );
		itemWrench = registerItem(ItemWrench::new, "wrench_wood" );
		itemBitSawDiamond = registerItem(ItemBitSaw::new, "bitsaw_diamond" );
		itemBlockBit = registerItem(ItemChiseledBit::new, "block_bit" );
		itemTapeMeasure = registerItem(ItemTapeMeasure::new, "tape_measure" );
		registerItem(ItemApiDebug::new, "debug" );
    }

}
