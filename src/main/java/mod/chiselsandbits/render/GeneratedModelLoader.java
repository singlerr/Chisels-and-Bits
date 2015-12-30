package mod.chiselsandbits.render;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.render.bit.BitItemSmartModel;
import mod.chiselsandbits.render.chiseledblock.ChisledBlockSmartModel;
import mod.chiselsandbits.render.patterns.MirrorPrintSmartModel;
import mod.chiselsandbits.render.patterns.NegativePrintSmartModel;
import mod.chiselsandbits.render.patterns.PositivePrintSmartModel;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GeneratedModelLoader implements ICustomModelLoader
{

	static class ModelGenerator
	{

		final IFlexibleBakedModel model;

		public ModelGenerator(
				final Class<? extends IFlexibleBakedModel> clz ) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
		{
			model = clz.newInstance();
		}

	};

	HashMap<ResourceLocation, ModelGenerator> models = new HashMap<ResourceLocation, ModelGenerator>();

	ArrayList<ModelResourceLocation> res = new ArrayList<ModelResourceLocation>();

	public GeneratedModelLoader()
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
		add( new ResourceLocation( ChiselsAndBits.MODID, "models/item/mirrorprint_written_preview" ), MirrorPrintSmartModel.class );

		ChiselsAndBits.registerWithBus( this );
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

		res.add( new ModelResourceLocation( modelLocation, "multipart" ) );
		res.add( new ModelResourceLocation( second, "multipart" ) );

		ModelGenerator mg;
		try
		{
			mg = new ModelGenerator( clz );
		}
		catch ( final Throwable e )
		{
			throw new RuntimeException( e );
		}

		models.put( modelLocation, mg );
		models.put( second, mg );

		models.put( new ModelResourceLocation( modelLocation, null ), mg );
		models.put( new ModelResourceLocation( second, null ), mg );

		models.put( new ModelResourceLocation( modelLocation, "inventory" ), mg );
		models.put( new ModelResourceLocation( second, "inventory" ), mg );

		models.put( new ModelResourceLocation( modelLocation, "multipart" ), mg );
		models.put( new ModelResourceLocation( second, "multipart" ), mg );
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
		return new SmartModelContainer( getModel( modelLocation ) );
	}

	private IFlexibleBakedModel getModel(
			final ResourceLocation modelLocation )
	{
		try
		{
			return models.get( modelLocation ).model;
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( "The Model: " + modelLocation.toString() + " was not available was requested." );
		}
	}

}
