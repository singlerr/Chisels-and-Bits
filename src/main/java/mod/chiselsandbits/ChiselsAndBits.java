
package mod.chiselsandbits;

import java.util.HashMap;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.MaterialType;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.crafting.ChiselCrafting;
import mod.chiselsandbits.crafting.StackableCrafting;
import mod.chiselsandbits.gui.ModGuiRouter;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.items.ItemNegativePrint;
import mod.chiselsandbits.items.ItemPositivePrint;
import mod.chiselsandbits.items.ItemWrench;
import mod.chiselsandbits.network.NetworkRouter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;


@Mod( name = ChiselsAndBits.MODNAME, modid = ChiselsAndBits.MODID, version = ChiselsAndBits.VERSION, dependencies = ChiselsAndBits.DEPENDENCIES, guiFactory = "mod.chiselsandbits.gui.ModConfigGuiFactory" )
public class ChiselsAndBits
{
	public static final String MODNAME = "Chisels & Bits";
	public static final String MODID = "chiselsandbits";
	public static final String VERSION = "1.5";
	
	public static final String DEPENDENCIES = "required-after:Forge@[" // require forge.
			+ net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion + ",)"; // buildVersion";

	public static final String unlocalizedPrefix = "mod." + MODID + ".";

	// create creative tab...
	public static CreativeTab creativeTab = new CreativeTab();
	public static ChiselsAndBits instance;
	
	public static final MaterialType[] validMaterials = new MaterialType[] { new MaterialType( "wood", Material.wood ), new MaterialType( "rock", Material.rock ), new MaterialType( "iron", Material.iron ), new MaterialType( "cloth", Material.cloth ), new MaterialType( "ice", Material.ice ), new MaterialType( "packedIce", Material.packedIce ), new MaterialType( "clay", Material.clay ), new MaterialType( "glass", Material.glass ) };

	final HashMap<Material, BlockChiseled> conversions = new HashMap<Material, BlockChiseled>();

	public IBlockState getChiseledDefaultState()
	{
		for ( final BlockChiseled bc : conversions.values() )
		{
			return bc.getDefaultState();
		}
		return null;
	}

	public BlockChiseled getConversion(
			final Material material )
	{
		return conversions.get( material );
	}

	public ItemChisel itemChiselStone;
	public ItemChisel itemChiselIron;
	public ItemChisel itemChiselGold;
	public ItemChisel itemChiselDiamond;

	public ItemChiseledBit itemBlockBit;
	public ItemPositivePrint itemPositiveprint;
	public ItemNegativePrint itemNegativeprint;
	public ItemBitBag itemBitBag;
	public ItemWrench itemWrench;

	public ModConfig config;

	public ChiselsAndBits()
	{
		instance = this;
	}

	private void registerItem(
			final Item item,
			final String name )
	{
		GameRegistry.registerItem( item.setUnlocalizedName( unlocalizedPrefix + name ), name );
	}

	private void registerBlock(
			final Block block,
			final Class<? extends ItemBlock> itemBlock,
			final String name )
	{
		GameRegistry.registerBlock( block.setUnlocalizedName( unlocalizedPrefix + name ), itemBlock == null ? ItemBlock.class : itemBlock, name );
	}

	@EventHandler
	public void preinit(
			final FMLPreInitializationEvent event )
	{
		// load config...
		config = new ModConfig( event.getSuggestedConfigurationFile() );

		initVersionChecker();

		// loader must be added here to prevent missing models, the rest of the model/textures must be configured later.
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.preinit( this );
		}

		// register item...
		if ( config.enableStoneChisel )
		{
			registerItem( itemChiselStone = new ItemChisel( ToolMaterial.STONE ), "chisel_stone" );
		}

		if ( config.enableIronChisel )
		{
			registerItem( itemChiselIron = new ItemChisel( ToolMaterial.IRON ), "chisel_iron" );
		}

		if ( config.enableGoldChisel )
		{
			registerItem( itemChiselGold = new ItemChisel( ToolMaterial.GOLD ), "chisel_gold" );
		}

		if ( config.enableDiamondChisel )
		{
			registerItem( itemChiselDiamond = new ItemChisel( ToolMaterial.EMERALD ), "chisel_diamond" );
		}

		if ( config.enablePositivePrint )
		{
			registerItem( itemPositiveprint = new ItemPositivePrint(), "positiveprint" );
		}

		if ( config.enableNegativePrint )
		{
			registerItem( itemNegativeprint = new ItemNegativePrint(), "negativeprint" );
		}

		if ( config.enableBitBag )
		{
			registerItem( itemBitBag = new ItemBitBag(), "bit_bag" );
		}

		if ( config.enableWoodenWrench )
		{
			registerItem( itemWrench = new ItemWrench(), "wrench_wood" );
		}

		if ( config.enableChisledBits )
		{
			registerItem( itemBlockBit = new ItemChiseledBit(), "block_bit" );
		}

		// register tile entities.
		GameRegistry.registerTileEntity( TileEntityBlockChiseled.class, "mod.chiselsandbits.TileEntityChiseled" );

		// register blocks...
		for ( final MaterialType mat : validMaterials )
		{
			final BlockChiseled blk = new BlockChiseled( mat.type, "chiseled_" + mat.name );
			conversions.put( mat.type, blk );
			registerBlock( blk, ItemBlockChiseled.class, blk.name );
		}
	}

	private void initVersionChecker()
	{
		final NBTTagCompound compound = new NBTTagCompound();
		compound.setString( "curseProjectName", "231095-chisels-bits" );
		compound.setString( "curseFilenameParser", "chiselsandbits-[].jar" );
		FMLInterModComms.sendRuntimeMessage( MODID, "VersionChecker", "addCurseCheck", compound );
	}

	@EventHandler
	public void init(
			final FMLInitializationEvent event )
	{
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.init( this );
		}

		MinecraftForge.EVENT_BUS.register( this );
		net.minecraftforge.fml.common.FMLCommonHandler.instance().bus().register( this );

		// add recipes to game...

		// tools..
		ShapedOreRecipe( itemChiselDiamond, "TS", 'T', "gemDiamond", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselGold, "TS", 'T', "ingotGold", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselIron, "TS", 'T', "ingotIron", 'S', "stickWood" );
		ShapedOreRecipe( itemChiselStone, "TS", 'T', "cobblestone", 'S', "stickWood" );
		ShapedOreRecipe( itemWrench, " W ", "WS ", "  S", 'W', "plankWood", 'S', "stickWood" );

		// create prints...
		ShapelessOreRecipe( itemPositiveprint, Items.water_bucket, Items.paper, "gemLapis" );
		ShapelessOreRecipe( itemNegativeprint, Items.water_bucket, Items.paper, "dustRedstone" );

		// clean patterns...
		ShapelessOreRecipe( itemPositiveprint, new ItemStack( itemPositiveprint, 1, OreDictionary.WILDCARD_VALUE ) );
		ShapelessOreRecipe( itemNegativeprint, new ItemStack( itemNegativeprint, 1, OreDictionary.WILDCARD_VALUE ) );

		// make a bit bag..
		if ( itemBlockBit != null )
		{
			ShapedOreRecipe( itemBitBag, "WWW", "WbW", "WWW", 'W', new ItemStack( Blocks.wool, 1, OreDictionary.WILDCARD_VALUE ), 'b', new ItemStack( itemBlockBit, 1, OreDictionary.WILDCARD_VALUE ) );
		}

		// aseemble prints from bits
		if ( config.enablePositivePrintCrafting )
		{
			GameRegistry.addRecipe( new ChiselCrafting() );
			net.minecraftforge.oredict.RecipeSorter.register( MODID + ":chiselcrafting", ChiselCrafting.class, Category.UNKNOWN, "after:minecraft:shapeless" );
		}

		if ( config.enableStackableCrafting )
		{
			GameRegistry.addRecipe( new StackableCrafting() );
			net.minecraftforge.oredict.RecipeSorter.register( MODID + ":stackablecrafting", StackableCrafting.class, Category.UNKNOWN, "after:minecraft:shapeless" );
		}
	}

	private void ShapedOreRecipe(
			final Item result,
			final Object... recipe )
	{
		if ( result == null )
		{
			return;
		}
		GameRegistry.addRecipe( new ShapedOreRecipe( result, recipe ) );
	}

	private void ShapelessOreRecipe(
			final Item result,
			final Object... recipe )
	{
		if ( result == null )
		{
			return;
		}
		GameRegistry.addRecipe( new ShapelessOreRecipe( result, recipe ) );
	}

	@EventHandler
	public void postinit(
			final FMLPostInitializationEvent event )
	{
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.postinit( this );
		}

		NetworkRouter.instance = new NetworkRouter();
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new ModGuiRouter() );
	}

	@SubscribeEvent
	/**
	 * this prevents some unwanted left click behavior..
	 *
	 * @param event
	 */
	public void interaction(
			final PlayerInteractEvent event )
	{
		if ( event.action == Action.LEFT_CLICK_BLOCK && event.entityPlayer != null )
		{
			final ItemStack is = event.entityPlayer.inventory.getCurrentItem();
			if ( is != null && ( is.getItem() instanceof ItemChisel || is.getItem() instanceof ItemChiseledBit ) )
			{
				event.setCanceled( true );
			}
		}
	}

	@SubscribeEvent
	/**
	 * this makes the chisel into an instant "miner", this makes the breaks as fast as creative, which are converted into microbreaks.
	 *
	 * @param event
	 */
	public void breakSpeed(
			final PlayerEvent.BreakSpeed event )
	{
		if ( event.entityPlayer != null )
		{
			final ItemStack is = event.entityPlayer.inventory.getCurrentItem();
			if ( is != null && ( is.getItem() instanceof ItemChisel || is.getItem() instanceof ItemChiseledBit ) )
			{
				event.newSpeed = 9999;
			}
		}
	}

}
