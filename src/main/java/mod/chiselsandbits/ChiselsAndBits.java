package mod.chiselsandbits;

import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.crafting.ChiselCrafting;
import mod.chiselsandbits.crafting.NegativeInversionCrafting;
import mod.chiselsandbits.crafting.StackableCrafting;
import mod.chiselsandbits.events.EventBreakSpeed;
import mod.chiselsandbits.events.EventPlayerInteract;
import mod.chiselsandbits.gui.ModGuiRouter;
import mod.chiselsandbits.integration.Integration;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;

@Mod(
		name = ChiselsAndBits.MODNAME,
		modid = ChiselsAndBits.MODID,
		version = ChiselsAndBits.VERSION,
		acceptedMinecraftVersions = "[1.8.8,1.8.9]",
		dependencies = ChiselsAndBits.DEPENDENCIES,
		guiFactory = "mod.chiselsandbits.gui.ModConfigGuiFactory" )
public class ChiselsAndBits
{
	public static final String MODNAME = "Chisels & Bits";
	public static final String MODID = "chiselsandbits";
	public static final String VERSION = "mc1.8.8-v1.7.4";

	public static final String DEPENDENCIES = "required-after:Forge@[" // forge.
			+ net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion + ",)"; // buildVersion";

	// create creative tab...
	public static ChiselsAndBits instance;

	public ModConfig config;
	public ModItems items;
	public ModBlocks blocks;

	public ChiselsAndBits()
	{
		instance = this;
	}

	@EventHandler
	public void preinit(
			final FMLPreInitializationEvent event )
	{
		// load config...
		config = new ModConfig( event.getSuggestedConfigurationFile() );
		items = new ModItems( config );
		blocks = new ModBlocks( config, event.getSide() );

		Integration.instance.preinit();

		// loader must be added here to prevent missing models, the rest of the
		// model/textures must be configured later.
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.preinit( this );
		}
	}

	@EventHandler
	public void init(
			final FMLInitializationEvent event )
	{
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.init( this );
		}

		Integration.instance.init();

		registerWithBus( new EventBreakSpeed() );
		registerWithBus( new EventPlayerInteract() );

		// add recipes to game...
		items.addRecipes();

		// add special recipes...
		if ( config.enablePositivePrintCrafting )
		{
			GameRegistry.addRecipe( new ChiselCrafting() );
			RecipeSorter.register( MODID + ":chiselcrafting", ChiselCrafting.class, Category.UNKNOWN, "after:minecraft:shapeless" );
		}

		if ( config.enableStackableCrafting )
		{
			GameRegistry.addRecipe( new StackableCrafting() );
			RecipeSorter.register( MODID + ":stackablecrafting", StackableCrafting.class, Category.UNKNOWN, "after:minecraft:shapeless" );
		}

		if ( config.enableNegativePrintInversionCrafting )
		{
			GameRegistry.addRecipe( new NegativeInversionCrafting() );
			RecipeSorter.register( MODID + ":negativepatterncrafting", NegativeInversionCrafting.class, Category.UNKNOWN, "after:minecraft:shapeless" );
		}
	}

	@EventHandler
	public void postinit(
			final FMLPostInitializationEvent event )
	{
		if ( event.getSide() == Side.CLIENT )
		{
			ClientSide.instance.postinit( this );
		}

		Integration.instance.postinit();

		NetworkRouter.instance = new NetworkRouter();
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new ModGuiRouter() );
	}

	public static void registerWithBus(
			final Object obj )
	{
		MinecraftForge.EVENT_BUS.register( obj );
	}

}
