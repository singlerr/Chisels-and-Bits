
package mod.chiselsandbits.render.ItemBlockBit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import mod.chiselsandbits.ClientSide;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;

@SuppressWarnings( "deprecation" )
public class BitItemBaked implements IFlexibleBakedModel
{
	VertexFormat frm;
	IBakedModel originalModel;

	List<BakedQuad> generic;

	public static final float pixelsPerBlock = 16.0f;

	public BitItemBaked(
			final int BlockRef )
	{
		final FaceBakery faceBakery = new FaceBakery();
		final IBlockState state = Block.getStateById( BlockRef );

		if ( state != null )
		{
			originalModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( state );
		}

		generic = new ArrayList<BakedQuad>();
		final TextureAtlasSprite texture = ClientSide.findTexture( BlockRef, originalModel );

		final Vector3f to = new Vector3f( 6.0f, 6.0f, 6.0f );
		final Vector3f from = new Vector3f( 10.0f, 10.0f, 10.0f );

		final BlockPartRotation bpr = null;
		final ModelRotation mr = ModelRotation.X0_Y0;

		for ( final EnumFacing myFace : EnumFacing.VALUES )
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

			generic.add( faceBakery.makeBakedQuad( toB, fromB, bpf, texture, myFace, mr, bpr, false, true ) );
		}

	}

	private float[] getFaceUvs(
			final EnumFacing face )
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
				afloat = new float[] { from_x, pixelsPerBlock - to_y, to_x, pixelsPerBlock - from_y };
				break;
			case WEST:
			case EAST:
				afloat = new float[] { from_z, pixelsPerBlock - to_y, to_z, pixelsPerBlock - from_y };
				break;
			default:
				throw new NullPointerException();
		}

		return afloat;
	}

	@Override
	public List<BakedQuad> getFaceQuads(
			final EnumFacing p_177551_1_ )
	{
		return Collections.emptyList();
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return generic;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture()
	{
		return originalModel == null ? null : originalModel.getTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public VertexFormat getFormat()
	{
		return null;
	}

}
