package mod.chiselsandbits.crafting;

import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipes
{

	final ModConfig config;

	public ModRecipes(
			ModConfig modConfig )
	{
		config = modConfig;
	}

	@SubscribeEvent
	void registerRecipes(
			RegistryEvent.Register<IRecipe> e )
	{
		IForgeRegistry<IRecipe> r = e.getRegistry();
		String MODID = ChiselsAndBits.MODID;

		// add special recipes...
		if ( ChiselsAndBits.getItems().itemBitBag != null )
		{
			r.register( new BagDyeing( new ResourceLocation( MODID, "bitbagdyeing" ) ) );
		}

		if ( config.enablePositivePrintCrafting )
		{
			r.register( new ChiselCrafting( new ResourceLocation( MODID, "positiveprintcrafting" ) ) );
		}

		if ( config.enableChiselCrafting )
		{
			r.register( new ChiselBlockCrafting( new ResourceLocation( MODID, "chiselcrafting" ) ) );
		}

		if ( config.enableStackableCrafting )
		{
			r.register( new StackableCrafting( new ResourceLocation( MODID, "stackablecrafting" ) ) );
		}

		if ( config.enableNegativePrintInversionCrafting )
		{
			r.register( new NegativeInversionCrafting( new ResourceLocation( MODID, "negativepatterncrafting" ) ) );
		}

		if ( config.enableMirrorPrint )
		{
			r.register( new MirrorTransferCrafting( new ResourceLocation( MODID, "mirrorpatterncrafting" ) ) );
		}

		if ( config.enableBitSaw )
		{
			r.register( new BitSawCrafting( new ResourceLocation( MODID, "bitsawcrafting" ) ) );
		}
	}

}
