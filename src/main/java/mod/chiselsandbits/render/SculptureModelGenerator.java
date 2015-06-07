
package mod.chiselsandbits.render;

import java.util.ArrayList;
import java.util.HashMap;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.render.BlockChisled.ChisledBlockSmartModel;
import mod.chiselsandbits.render.ItemBlockBit.BitItemSmartModel;
import mod.chiselsandbits.render.patterns.NegativePrintSmartModel;
import mod.chiselsandbits.render.patterns.PositivePrintSmartModel;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class SculptureModelGenerator implements ICustomModelLoader
{

	HashMap<ResourceLocation, Class<? extends IFlexibleBakedModel>> models = new HashMap<ResourceLocation, Class<? extends IFlexibleBakedModel>>();

	ArrayList<ModelResourceLocation> res = new ArrayList<ModelResourceLocation>();

	public SculptureModelGenerator()
	{
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_iron" ), ChisledBlockSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_clay" ), ChisledBlockSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_cloth" ), ChisledBlockSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_packedIce" ), ChisledBlockSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_ice" ), ChisledBlockSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_wood" ), ChisledBlockSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_rock" ), ChisledBlockSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "chiseled_glass" ), ChisledBlockSmartModel.class );

		add( new ResourceLocation( ChiselsAndBits.MODID, "models/item/block_chiseled" ), ChisledBlockSmartModel.class );

		add( new ResourceLocation( ChiselsAndBits.MODID, "models/item/block_bit" ), BitItemSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "models/item/positiveprint_written_preview" ), PositivePrintSmartModel.class );
		add( new ResourceLocation( ChiselsAndBits.MODID, "models/item/negativeprint_written_preview" ), NegativePrintSmartModel.class );

		MinecraftForge.EVENT_BUS.register( this );
	}

	private void add(
			final ResourceLocation modelLocation,
			final Class<? extends IFlexibleBakedModel> clz )
	{
		final ResourceLocation second = new ResourceLocation( modelLocation.getResourceDomain(), modelLocation.getResourcePath().substring( 1 + modelLocation.getResourcePath().lastIndexOf( '/' ) ) );

		res.add( new ModelResourceLocation( modelLocation, null ) );
		res.add( new ModelResourceLocation( second, null ) );
		res.add( new ModelResourceLocation( modelLocation, "inventory" ) );
		res.add( new ModelResourceLocation( second, "inventory" ) );

		models.put( modelLocation, clz );
		models.put( second, clz );
		models.put( new ModelResourceLocation( modelLocation, null ), clz );
		models.put( new ModelResourceLocation( second, null ), clz );
		models.put( new ModelResourceLocation( modelLocation, "inventory" ), clz );
		models.put( new ModelResourceLocation( second, "inventory" ), clz );
	}

	@SubscribeEvent
	public void onModelBakeEvent(
			final ModelBakeEvent event )
	{
		for ( final ModelResourceLocation rl : res )
		{
			event.modelRegistry.putObject( rl, getModel( rl ) );
		}
	}

	@Override
	public void onResourceManagerReload(
			final IResourceManager resourceManager )
	{

	}

	@Override
	public boolean accepts(
			final ResourceLocation modelLocation )
	{
		return models.containsKey( modelLocation );
	}

	@Override
	public IModel loadModel(
			final ResourceLocation modelLocation )
	{
		return new SmartModel( getModel( modelLocation ) );
	}

	private IFlexibleBakedModel getModel(
			final ResourceLocation modelLocation )
	{
		try
		{
			return models.get( modelLocation ).newInstance();
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( "The Model: " + e.toString() + " was not available was requested." );
		}
	}

}
