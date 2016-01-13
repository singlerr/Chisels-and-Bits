package mod.chiselsandbits.registry;

import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.debug.ItemApiDebug;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.items.ItemMirrorPrint;
import mod.chiselsandbits.items.ItemNegativePrint;
import mod.chiselsandbits.items.ItemPositivePrint;
import mod.chiselsandbits.items.ItemWrench;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ModItems extends ModRegistry
{

	public ItemChisel itemChiselStone;
	public ItemChisel itemChiselIron;
	public ItemChisel itemChiselGold;
	public ItemChisel itemChiselDiamond;

	public ItemChiseledBit itemBlockBit;
	public ItemMirrorPrint itemMirrorprint;
	public ItemPositivePrint itemPositiveprint;
	public ItemNegativePrint itemNegativeprint;

	public ItemBitBag itemBitBag;
	public ItemWrench itemWrench;

	private <T extends Item> T registerItem(
			final boolean enabled,
			final T item,
			final String name )
	{
		if ( enabled )
		{
			item.setCreativeTab( creativeTab );
			GameRegistry.registerItem( item.setUnlocalizedName( unlocalizedPrefix + name ), name );
			return item;
		}

		return null;
	}

	public ModItems(
			final ModConfig config )
	{
		// register items...
		itemChiselStone = registerItem( config.enableStoneChisel, new ItemChisel( ToolMaterial.STONE ), "chisel_stone" );
		itemChiselIron = registerItem( config.enableIronChisel, new ItemChisel( ToolMaterial.IRON ), "chisel_iron" );
		itemChiselGold = registerItem( config.enableGoldChisel, new ItemChisel( ToolMaterial.GOLD ), "chisel_gold" );
		itemChiselDiamond = registerItem( config.enableDiamondChisel, new ItemChisel( ToolMaterial.EMERALD ), "chisel_diamond" );
		itemPositiveprint = registerItem( config.enablePositivePrint, new ItemPositivePrint(), "positiveprint" );
		itemNegativeprint = registerItem( config.enableNegativePrint, new ItemNegativePrint(), "negativeprint" );
		itemMirrorprint = registerItem( config.enableMirrorPrint, new ItemMirrorPrint(), "mirrorprint" );
		itemBitBag = registerItem( config.enableBitBag, new ItemBitBag(), "bit_bag" );
		itemWrench = registerItem( config.enableWoodenWrench, new ItemWrench(), "wrench_wood" );
		itemBlockBit = registerItem( config.enableChisledBits, new ItemChiseledBit(), "block_bit" );
		registerItem( config.enableAPITestingItem, new ItemApiDebug(), "debug" );
	}

	public void addRecipes()
	{
		// tools..
		ShapedOreRecipe( itemChiselDiamond, "TS", 'T', "gemDiamond", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselGold, "TS", 'T', "ingotGold", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselIron, "TS", 'T', "ingotIron", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselStone, "TS", 'T', "cobblestone", 'S', "stickWood" );
		ShapedOreRecipe( itemWrench, " W ", "WS ", "  S", 'W', "plankWood", 'S', "stickWood" );

		// create prints...
		ShapelessOreRecipe( itemPositiveprint, Items.water_bucket, Items.paper, "gemLapis" );
		ShapelessOreRecipe( itemNegativeprint, Items.water_bucket, Items.paper, "dustRedstone" );
		ShapelessOreRecipe( itemMirrorprint, Items.water_bucket, Items.paper, "dustGlowstone" );

		// clean patterns...
		ShapelessOreRecipe( itemPositiveprint, new ItemStack( itemPositiveprint, 1, OreDictionary.WILDCARD_VALUE ) );
		ShapelessOreRecipe( itemNegativeprint, new ItemStack( itemNegativeprint, 1, OreDictionary.WILDCARD_VALUE ) );
		ShapelessOreRecipe( itemMirrorprint, new ItemStack( itemMirrorprint, 1, OreDictionary.WILDCARD_VALUE ) );

		// make a bit bag..
		ShapedOreRecipe( itemBitBag, "WWW", "WbW", "WWW", 'W', new ItemStack( Blocks.wool, 1, OreDictionary.WILDCARD_VALUE ), 'b', new ItemStack( itemBlockBit, 1, OreDictionary.WILDCARD_VALUE ) );
	}

	private void ShapedOreRecipe(
			final Item result,
			final Object... recipe )
	{
		if ( result != null )
		{
			GameRegistry.addRecipe( new ShapedOreRecipe( result, recipe ) );
		}
	}

	private void ShapelessOreRecipe(
			final Item result,
			final Object... recipe )
	{
		if ( result != null )
		{
			GameRegistry.addRecipe( new ShapelessOreRecipe( result, recipe ) );
		}
	}

}
