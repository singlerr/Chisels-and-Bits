package mod.chiselsandbits.render.bit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;

import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.render.BaseBakedBlockModel;
import mod.chiselsandbits.render.helpers.ModelQuadLayer;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.client.renderer.model.BlockPartRotation;
import net.minecraft.client.renderer.model.FaceBakery;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class BitItemBaked extends BaseBakedBlockModel
{
	public static final float PIXELS_PER_BLOCK = 16.0f;

	private static final float BIT_BEGIN = 6.0f;
	private static final float BIT_END = 10.0f;

	final ArrayList<BakedQuad> generic = new ArrayList<BakedQuad>( 6 );

	public BitItemBaked(
			final int BlockRef )
	{
		final FaceBakery faceBakery = new FaceBakery();

		final Vector3f to = new Vector3f( BIT_BEGIN, BIT_BEGIN, BIT_BEGIN );
		final Vector3f from = new Vector3f( BIT_END, BIT_END, BIT_END );

		final BlockPartRotation bpr = null;
		final ModelRotation mr = ModelRotation.X0_Y0;

		for ( final Direction myFace : Direction.values() )
		{
			for ( final RenderType layer : RenderType.getBlockRenderTypes() )
			{
				final ModelQuadLayer[] layers = ModelUtil.getCachedFace( BlockRef, 0, myFace, layer );

				if ( layers == null || layers.length == 0 )
				{
					continue;
				}

				for ( final ModelQuadLayer clayer : layers )
				{
					final BlockFaceUV uv = new BlockFaceUV( getFaceUvs( myFace ), 0 );
					final BlockPartFace bpf = new BlockPartFace( myFace, 0, "", uv );

					Vector3f toB, fromB;

					switch ( myFace )
					{
						case UP:
							toB = new Vector3f( to.x, from.y, to.z );
							fromB = new Vector3f( from.x, from.y, from.z );
							break;
						case EAST:
							toB = new Vector3f( from.x, to.y, to.z );
							fromB = new Vector3f( from.x, from.y, from.z );
							break;
						case NORTH:
							toB = new Vector3f( to.x, to.y, to.z );
							fromB = new Vector3f( from.x, from.y, to.z );
							break;
						case SOUTH:
							toB = new Vector3f( to.x, to.y, from.z );
							fromB = new Vector3f( from.x, from.y, from.z );
							break;
						case DOWN:
							toB = new Vector3f( to.x, to.y, to.z );
							fromB = new Vector3f( from.x, to.y, from.z );
							break;
						case WEST:
							toB = new Vector3f( to.x, to.y, to.z );
							fromB = new Vector3f( to.x, from.y, from.z );
							break;
						default:
							throw new NullPointerException();
					}

					generic.add( faceBakery.makeBakedQuad( toB, fromB, bpf, clayer.sprite, myFace, mr, bpr, false, true ) );
				}
			}
		}

		generic.trimToSize();
	}

	private float[] getFaceUvs(
			final Direction face )
	{
		float[] afloat;

		final int from_x = 7;
		final int from_y = 7;
		final int from_z = 7;

		final int to_x = 8;
		final int to_y = 8;
		final int to_z = 8;

		switch ( face )
		{
			case DOWN:
			case UP:
				afloat = new float[] { from_x, from_z, to_x, to_z };
				break;
			case NORTH:
			case SOUTH:
				afloat = new float[] { from_x, PIXELS_PER_BLOCK - to_y, to_x, PIXELS_PER_BLOCK - from_y };
				break;
			case WEST:
			case EAST:
				afloat = new float[] { from_z, PIXELS_PER_BLOCK - to_y, to_z, PIXELS_PER_BLOCK - from_y };
				break;
			default:
				throw new NullPointerException();
		}

		return afloat;
	}

	@Override
	public List<BakedQuad> getQuads(
			final BlockState state,
			final Direction side,
			final long rand )
	{
		if ( side != null )
		{
			return Collections.emptyList();
		}

		return generic;
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return ClientSide.instance.getMissingIcon();
	}

}
