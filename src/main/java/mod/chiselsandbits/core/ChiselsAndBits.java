package mod.chiselsandbits.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.client.CreativeClipboardTab;
import mod.chiselsandbits.client.UndoTracker;
import mod.chiselsandbits.commands.SetBit;
import mod.chiselsandbits.config.Configuration;
import mod.chiselsandbits.core.api.ChiselAndBitsAPI;
import mod.chiselsandbits.core.api.IMCHandler;
import mod.chiselsandbits.crafting.ModRecipes;
import mod.chiselsandbits.events.EventPlayerInteract;
import mod.chiselsandbits.events.VaporizeWater;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.network.NetworkChannel;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLModIdMappingEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(ChiselsAndBits.MODID)
public class ChiselsAndBits
{
	public static final @Nonnull String MODNAME = "Chisels & Bits";
	public static final @Nonnull String MODID = "chiselsandbits";

	private static ChiselsAndBits    instance;
	private        Configuration     config;
	private        ModItems          items;
	private        ModBlocks         blocks;
	private final  Integration       integration = new Integration();
	private final  IChiselAndBitsAPI api = new ChiselAndBitsAPI();
	private final  NetworkChannel    networkChannel = new NetworkChannel(MODID);
	private        boolean           loadClientAssets = false;

	List<ICacheClearable> cacheClearables = new ArrayList<ICacheClearable>();

	public ChiselsAndBits()
	{
	    instance = this;
        config = new Configuration(ModLoadingContext.get().getActiveContainer());
	}

	public static ChiselsAndBits getInstance()
	{
		return instance;
	}

	public static ModBlocks getBlocks()
	{
		return instance.blocks;
	}

	public static ModItems getItems()
	{
		return instance.items;
	}

	public static Configuration getConfig()
	{
		return instance.config;
	}

	public static IChiselAndBitsAPI getApi()
	{
		return instance.api;
	}

	public static NetworkChannel getNetworkChannel() {
	    return instance.networkChannel;
    }


	private void handleIMCEvent(
			final InterModProcessEvent event )
	{
		final IMCHandler imcHandler = new IMCHandler();
		imcHandler.handleIMCEvent();
	}

	public void commonSetup(
			final FMLCommonSetupEvent event )
	{

		registerWithBus( new ModRecipes( getConfig() ) );

		integration.preinit( event );

		// merge most of the extra materials into the normal set.
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.SPONGE, Material.CLAY );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.ANVIL, Material.IRON );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.PLANTS, Material.GRASS );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.GOURD, Material.PLANTS );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.CACTUS, Material.PLANTS );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.CORAL, Material.ROCK );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.WEB, Material.PLANTS );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.VINE, Material.PLANTS );
		ChiselsAndBits.getApi().addEquivilantMaterial( Material.TNT, Material.ROCK );

		// loader must be added here to prevent missing models, the rest of the
		// model/textures must be configured later.
		if ( event.getSide() == Side.CLIENT )
		{
			loadClientAssets = true;

			// load this after items are created...
			CreativeClipboardTab.load( new File( configFile.getParent(), MODID + "_clipboard.cfg" ) );

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

		integration.init();

		registerWithBus( new EventPlayerInteract() );
		registerWithBus( new VaporizeWater() );
	}

	@EventHandler
	public void serverStart(
			final FMLServerStartingEvent e )
	{

		if ( getConfig().enableSetBitCommand )
		{
			e.registerServerCommand( new SetBit() );
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

		integration.postinit();

		NetworkRouter.instance = new NetworkRouter();
		NetworkRegistry.INSTANCE.registerGuiHandler( this, new ModGuiRouter() );
	}

	boolean idsHaveBeenMapped = false;

	@EventHandler
	public void idsMapped(
			final FMLModIdMappingEvent event )
	{
		idsHaveBeenMapped = true;
		BlockBitInfo.recalculateFluidBlocks();
		clearCache();
	}

	public void clearCache()
	{
		if ( idsHaveBeenMapped )
		{
			for ( final ICacheClearable clearable : cacheClearables )
			{
				clearable.clearCache();
			}

			addClearable( UndoTracker.getInstance() );
			VoxelBlob.clearCache();
		}
	}

	public static void registerWithBus(
			final Object obj )
	{
		MinecraftForge.EVENT_BUS.register( obj );
	}

	public void addClearable(
			final ICacheClearable cache )
	{
		if ( !cacheClearables.contains( cache ) )
		{
			cacheClearables.add( cache );
		}
	}

	public boolean loadClientAssets()
	{
		return loadClientAssets;
	}



}
