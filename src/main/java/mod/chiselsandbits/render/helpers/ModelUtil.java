package mod.chiselsandbits.render.helpers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ReflectionWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings( "unchecked" )
public class ModelUtil
{
	private final static Random RANDOM = new Random();
	private final static HashMap<Integer, String> blockToTexture[];
	private final static HashMap<Integer, Integer> blockToLight = new HashMap<Integer, Integer>();

	static
	{
		blockToTexture = new HashMap[EnumFacing.VALUES.length * EnumWorldBlockLayer.values().length];

		for ( int x = 0; x < blockToTexture.length; x++ )
		{
			blockToTexture[x] = new HashMap<Integer, String>();
		}
	}

	private ModelUtil()
	{

	}

	private static int findLightValue(
			final int lightValue,
			final List<BakedQuad> faceQuads )
	{
		final ModelLightMapReader lv = new ModelLightMapReader( lightValue );

		for ( final BakedQuad q : faceQuads )
		{
			if ( q instanceof UnpackedBakedQuad )
			{
				final UnpackedBakedQuad ubq = (UnpackedBakedQuad) q;
				ubq.pipe( lv );
			}
		}

		return lv.lv;
	}

	private static TextureAtlasSprite findTexture(
			TextureAtlasSprite texture,
			final List<BakedQuad> faceQuads,
			final EnumFacing side ) throws IllegalArgumentException, IllegalAccessException, NullPointerException
	{
		if ( texture == null )
		{
			final TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
			final Map<String, TextureAtlasSprite> mapRegisteredSprites = ReflectionWrapper.instance.getRegSprite( map );

			if ( mapRegisteredSprites == null )
			{
				throw new RuntimeException( "Unable to lookup textures." );
			}

			for ( final BakedQuad q : faceQuads )
			{
				if ( side != null && q.getFace() != side )
				{
					continue;
				}

				final ModelUVAverager av = new ModelUVAverager();
				q.pipe( av );

				final float U = av.getU();
				final float V = av.getV();

				final Iterator<?> iterator1 = mapRegisteredSprites.values().iterator();
				while ( iterator1.hasNext() )
				{
					final TextureAtlasSprite sprite = (TextureAtlasSprite) iterator1.next();
					if ( sprite.getMinU() <= U && U <= sprite.getMaxU() && sprite.getMinV() <= V && V <= sprite.getMaxV() )
					{
						texture = sprite;
						return texture;
					}
				}
			}
		}

		return texture;
	}

	public static int findLightValue(
			final int BlockRef,
			final IBakedModel originalModel )
	{
		final Integer lvP = blockToLight.get( BlockRef );

		if ( lvP != null )
		{
			return lvP;
		}

		int lv = 0;

		for ( final EnumFacing side : EnumFacing.VALUES )
		{
			lv = findLightValue( lv, originalModel.getFaceQuads( side ) );
		}

		findLightValue( lv, originalModel.getGeneralQuads() );
		blockToLight.put( BlockRef, lv );
		return lv;
	}

	@SuppressWarnings( "rawtypes" )
	public static IBakedModel solveModel(
			final int BlockRef,
			final long weight,
			final IBakedModel originalModel )
	{
		IBakedModel actingModel = originalModel;
		final IBlockState state = Block.getStateById( BlockRef );

		try
		{
			if ( actingModel != null && ChiselsAndBits.getConfig().allowBlockAlternatives && actingModel instanceof WeightedBakedModel )
			{
				actingModel = ( (WeightedBakedModel) actingModel ).getAlternativeModel( weight );
			}
		}
		catch ( final Exception err )
		{
		}

		// first try to get the real model...
		try
		{
			if ( actingModel instanceof ISmartBlockModel )
			{
				if ( state instanceof IExtendedBlockState )
				{
					if ( actingModel instanceof ISmartItemModel )
					{
						final Item it = state.getBlock().getItemDropped( state, RANDOM, 0 );
						final ItemStack stack = new ItemStack( it, 1, state.getBlock().damageDropped( state ) );

						final IBakedModel newModel = ( (ISmartItemModel) actingModel ).handleItemState( stack );
						if ( newModel != null )
						{
							return newModel;
						}
					}

					IExtendedBlockState extendedState = (IExtendedBlockState) state;

					for ( final IUnlistedProperty p : extendedState.getUnlistedNames() )
					{
						extendedState = extendedState.withProperty( p, p.getType().newInstance() );
					}

					final IBakedModel newModel = ( (ISmartBlockModel) actingModel ).handleBlockState( extendedState );
					if ( newModel != null )
					{
						return newModel;
					}
				}
				else
				{
					final IBakedModel newModel = ( (ISmartBlockModel) actingModel ).handleBlockState( state );
					if ( newModel != null )
					{
						return newModel;
					}
				}
			}
		}
		catch ( final Exception err )
		{
			if ( actingModel instanceof ISmartItemModel )
			{
				final Item it = state.getBlock().getItemDropped( state, RANDOM, 0 );
				final ItemStack stack = new ItemStack( it, 1, state.getBlock().damageDropped( state ) );

				final IBakedModel newModel = ( (ISmartItemModel) actingModel ).handleItemState( stack );
				if ( newModel != null )
				{
					return newModel;
				}
			}
		}

		return actingModel;
	}

	public static TextureAtlasSprite findTexture(
			final int BlockRef,
			final IBakedModel model,
			final EnumFacing myFace,
			final EnumWorldBlockLayer layer )
	{
		final int blockToWork = layer.ordinal() * EnumFacing.VALUES.length + myFace.ordinal();

		// didn't work? ok lets try scanning for the texture in the
		if ( blockToTexture[blockToWork].containsKey( BlockRef ) )
		{
			final String textureName = blockToTexture[blockToWork].get( BlockRef );
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite( textureName );
		}

		TextureAtlasSprite texture = null;

		if ( model != null )
		{
			try
			{
				texture = findTexture( texture, model.getFaceQuads( myFace ), myFace );

				if ( texture == null )
				{
					for ( final EnumFacing side : EnumFacing.VALUES )
					{
						texture = findTexture( texture, model.getFaceQuads( side ), side );
					}

					texture = findTexture( texture, model.getGeneralQuads(), null );
				}
			}
			catch ( final Exception errr )
			{
			}
		}

		// who knows if that worked.. now lets try to get a texture...
		if ( texture == null )
		{
			try
			{
				if ( texture == null )
				{
					texture = model.getParticleTexture();
				}
			}
			catch ( final Exception err )
			{
			}
		}

		if ( texture == null )
		{
			texture = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		}

		blockToTexture[blockToWork].put( BlockRef, texture.getIconName() );
		return texture;
	}

	public static boolean isOne(
			final float v )
	{
		return Math.abs( v ) < 0.01;
	}

	public static boolean isZero(
			final float v )
	{
		return Math.abs( v - 1.0f ) < 0.01;
	}

}
