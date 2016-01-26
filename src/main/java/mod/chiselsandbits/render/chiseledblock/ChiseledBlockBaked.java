package mod.chiselsandbits.render.chiseledblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.VisibleFace;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.render.BaseBakedBlockModel;
import mod.chiselsandbits.render.helpers.ModelParserCache;
import mod.chiselsandbits.render.helpers.ModelUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.BlockPartRotation;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;

public class ChiseledBlockBaked extends BaseBakedBlockModel
{
	public static final float PIXELS_PER_BLOCK = 16.0f;
	static boolean hasFaceMap = false;
	static int[][] faceVertMap = new int[6][4];

	private static final EnumFacing[] X_Faces = new EnumFacing[] { EnumFacing.EAST, EnumFacing.WEST };
	private static final EnumFacing[] Y_Faces = new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };
	private static final EnumFacing[] Z_Faces = new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.NORTH };

	EnumWorldBlockLayer myLayer;
	VertexFormat format;
	TextureAtlasSprite sprite;

	BakedQuad[] up;
	BakedQuad[] down;
	BakedQuad[] north;
	BakedQuad[] south;
	BakedQuad[] east;
	BakedQuad[] west;
	BakedQuad[] generic;

	public List<BakedQuad> getList(
			final EnumFacing side )
	{
		if ( side != null )
		{
			switch ( side )
			{
				case DOWN:
					return asList( down );
				case EAST:
					return asList( east );
				case NORTH:
					return asList( north );
				case SOUTH:
					return asList( south );
				case UP:
					return asList( up );
				case WEST:
					return asList( west );
				default:
			}
		}

		return asList( generic );
	}

	private List<BakedQuad> asList(
			final BakedQuad[] array )
	{
		if ( array == null )
		{
			return Collections.emptyList();
		}

		return Arrays.asList( array );
	}

	private ChiseledBlockBaked()
	{
	}

	public ChiseledBlockBaked(
			final int blockReference,
			final EnumWorldBlockLayer layer,
			final VoxelBlobStateReference data,
			final ModelRenderState mrs,
			final VertexFormat format )
	{
		myLayer = layer;
		this.format = format;
		final IBlockState state = Block.getStateById( blockReference );

		IBakedModel originalModel = null;

		if ( state != null )
		{
			originalModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( state );
		}

		if ( originalModel != null && data != null )
		{
			final VoxelBlob vb = data.getVoxelBlob();
			if ( vb != null && vb.filter( layer ) )
			{
				final ChiseledModelBuilder builder = new ChiseledModelBuilder();
				generateFaces( builder, vb, mrs, data.weight );

				up = builder.getSide( EnumFacing.UP );
				down = builder.getSide( EnumFacing.DOWN );
				east = builder.getSide( EnumFacing.EAST );
				west = builder.getSide( EnumFacing.WEST );
				north = builder.getSide( EnumFacing.NORTH );
				south = builder.getSide( EnumFacing.SOUTH );
				generic = builder.getSide( null );
			}
		}
	}

	public static ChiseledBlockBaked breakingParticleModel(
			final EnumWorldBlockLayer layer,
			final Integer blockStateID )
	{
		final ChiseledBlockBaked out = new ChiseledBlockBaked();

		final IBakedModel model = ModelUtil.solveModel( blockStateID, 0, Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( Block.getStateById( blockStateID ) ) );
		if ( model != null )
		{
			out.sprite = ModelUtil.findTexture( blockStateID, model, EnumFacing.UP, layer );
			out.myLayer = layer;
		}

		return out;
	}

	public boolean isEmpty()
	{
		boolean trulyEmpty = getList( null ).isEmpty();

		for ( final EnumFacing e : EnumFacing.VALUES )
		{
			trulyEmpty = trulyEmpty && getList( e ).isEmpty();
		}

		return trulyEmpty;
	}

	private void generateFaces(
			final ChiseledModelBuilder builder,
			final VoxelBlob blob,
			final ModelRenderState mrs,
			final long weight )
	{
		final FaceBakery faceBakery = new FaceBakery();

		final BlockPartRotation bpr = null;
		final ModelRotation mr = ModelRotation.X0_Y0;

		final ArrayList<ArrayList<FaceRegion>> rset = new ArrayList<ArrayList<FaceRegion>>();
		final VisibleFace visFace = new VisibleFace();

		processXFaces( blob, visFace, mrs, rset );
		processYFaces( blob, visFace, mrs, rset );
		processZFaces( blob, visFace, mrs, rset );

		final float[] defUVs = new float[] { 0, 0, 1, 1 };

		final IFaceBuilder faceBuilder = format == ChiselsAndBitsBakedQuad.VERTEX_FORMAT ? new ChiselsAndBitsBakedQuad.Builder() : new UnpackedQuadBuilderWrapper();
		for ( final ArrayList<FaceRegion> src : rset )
		{
			mergeFaces( src );

			for ( final FaceRegion region : src )
			{
				final EnumFacing myFace = region.face;

				// keep integers up until the last moment... ( note I tested
				// snapping the floats after this stage, it made no
				// difference. )
				final Vector3f to = offsetVec( region.max, myFace, 1 );
				final Vector3f from = offsetVec( region.min, myFace, -1 );

				final BlockFaceUV uv = new BlockFaceUV( defUVs, 0 );
				final BlockPartFace bpf = new BlockPartFace( myFace, -1, "", uv );

				final ModelParserCache[] mpc = ModelUtil.getCachedFace( region.blockStateID, weight, myFace, myLayer );

				for ( final ModelParserCache pc : mpc )
				{
					final float[] uvs = getFaceUvs( myFace, from, to, pc.uvs );
					final BakedQuad g = faceBakery.makeBakedQuad( to, from, bpf, pc.sprite, myFace, mr, bpr, true, true );

					faceBuilder.begin( format );
					faceBuilder.setFace( myFace, pc.tint );

					final int[] vertData = g.getVertexData();
					final int wrapAt = vertData.length / 4;

					calcVertFaceMap();

					final float maxLightmap = 32.0f / 0xffff;

					// build it.
					for ( int vertNum = 0; vertNum < 4; vertNum++ )
					{
						for ( int elementIndex = 0; elementIndex < format.getElementCount(); elementIndex++ )
						{
							final VertexFormatElement element = format.getElement( elementIndex );
							switch ( element.getUsage() )
							{
								case POSITION:
									faceBuilder.put( elementIndex, Float.intBitsToFloat( vertData[0 + wrapAt * vertNum] ), Float.intBitsToFloat( vertData[1 + wrapAt * vertNum] ), Float.intBitsToFloat( vertData[2 + wrapAt * vertNum] ) );
									break;

								case COLOR:
									final int cb = getShadeColor( vertData, wrapAt * vertNum, region, blob, pc.tint == -1 ? 0xffffff : pc.color );
									faceBuilder.put( elementIndex, byteToFloat( cb ), byteToFloat( cb >> 8 ), byteToFloat( cb >> 16 ), byteToFloat( cb >> 24 ) );
									break;

								case NORMAL:
									faceBuilder.put( elementIndex, myFace.getFrontOffsetX(), myFace.getFrontOffsetY(), myFace.getFrontOffsetZ() );
									break;

								case UV:
									if ( element.getIndex() == 1 )
									{
										final float v = maxLightmap * Math.max( 0, Math.min( 15, pc.light ) );
										faceBuilder.put( elementIndex, v, v );
									}
									else
									{
										faceBuilder.put( elementIndex, pc.sprite.getInterpolatedU( uvs[faceVertMap[myFace.getIndex()][vertNum] * 2 + 0] ), pc.sprite.getInterpolatedV( uvs[faceVertMap[myFace.getIndex()][vertNum] * 2 + 1] ) );
									}
									break;

								default:
									faceBuilder.put( elementIndex );
									break;
							}
						}
					}

					if ( region.isEdge )
					{
						builder.getList( myFace ).add( faceBuilder.create() );
					}
					else
					{
						builder.getList( null ).add( faceBuilder.create() );
					}
				}
			}
		}
	}

	private float byteToFloat(
			final int i )
	{
		return ( i & 0xff ) / 255.0f;
	}

	private void mergeFaces(
			final ArrayList<FaceRegion> src )
	{
		boolean restart;

		do
		{
			restart = false;

			final int size = src.size();
			final int sizeMinusOne = size - 1;

			restart: for ( int x = 0; x < sizeMinusOne; ++x )
			{
				final FaceRegion faceA = src.get( x );

				for ( int y = x + 1; y < size; ++y )
				{
					final FaceRegion faceB = src.get( y );

					if ( faceA.extend( faceB ) )
					{
						src.set( y, src.get( sizeMinusOne ) );
						src.remove( sizeMinusOne );

						restart = true;
						break restart;
					}
				}
			}
		}
		while ( restart );
	}

	private void processXFaces(
			final VoxelBlob blob,
			final VisibleFace visFace,
			final ModelRenderState mrs,
			final ArrayList<ArrayList<FaceRegion>> rset )
	{
		ArrayList<FaceRegion> regions = null;

		for ( final EnumFacing myFace : X_Faces )
		{
			final VoxelBlobStateReference nextToState = mrs != null && myLayer != EnumWorldBlockLayer.SOLID ? mrs.sides[myFace.ordinal()] : null;
			final VoxelBlob nextTo = nextToState == null ? null : nextToState.getVoxelBlob();
			for ( int x = 0; x < blob.detail; x++ )
			{
				if ( regions == null )
				{
					regions = new ArrayList<FaceRegion>( 16 );
				}

				for ( int z = 0; z < blob.detail; z++ )
				{
					FaceRegion currentFace = null;

					for ( int y = 0; y < blob.detail; y++ )
					{
						final FaceRegion region = getRegion( blob, myFace, x, y, z, visFace, nextTo );

						if ( region == null )
						{
							currentFace = null;
							continue;
						}

						if ( currentFace != null )
						{
							if ( currentFace.extend( region ) )
							{
								continue;
							}
						}

						currentFace = region;
						regions.add( region );
					}
				}

				if ( !regions.isEmpty() )
				{
					rset.add( regions );
					regions = null;
				}
			}
		}
	}

	private void processYFaces(
			final VoxelBlob blob,
			final VisibleFace visFace,
			final ModelRenderState mrs,
			final ArrayList<ArrayList<FaceRegion>> rset )
	{
		ArrayList<FaceRegion> regions = null;

		for ( final EnumFacing myFace : Y_Faces )
		{
			final VoxelBlobStateReference nextToState = mrs != null && myLayer != EnumWorldBlockLayer.SOLID ? mrs.sides[myFace.ordinal()] : null;
			final VoxelBlob nextTo = nextToState == null ? null : nextToState.getVoxelBlob();
			for ( int y = 0; y < blob.detail; y++ )
			{
				if ( regions == null )
				{
					regions = new ArrayList<FaceRegion>( 16 );
				}

				for ( int z = 0; z < blob.detail; z++ )
				{
					FaceRegion currentFace = null;

					for ( int x = 0; x < blob.detail; x++ )
					{
						final FaceRegion region = getRegion( blob, myFace, x, y, z, visFace, nextTo );

						if ( region == null )
						{
							currentFace = null;
							continue;
						}

						if ( currentFace != null )
						{
							if ( currentFace.extend( region ) )
							{
								continue;
							}
						}

						currentFace = region;
						regions.add( region );
					}
				}

				if ( !regions.isEmpty() )
				{
					rset.add( regions );
					regions = null;
				}
			}
		}
	}

	private void processZFaces(
			final VoxelBlob blob,
			final VisibleFace visFace,
			final ModelRenderState mrs,
			final ArrayList<ArrayList<FaceRegion>> rset )
	{
		ArrayList<FaceRegion> regions = null;

		for ( final EnumFacing myFace : Z_Faces )
		{
			final VoxelBlobStateReference nextToState = mrs != null && myLayer != EnumWorldBlockLayer.SOLID ? mrs.sides[myFace.ordinal()] : null;
			final VoxelBlob nextTo = nextToState == null ? null : nextToState.getVoxelBlob();
			for ( int z = 0; z < blob.detail; z++ )
			{
				if ( regions == null )
				{
					regions = new ArrayList<FaceRegion>( 16 );
				}

				for ( int y = 0; y < blob.detail; y++ )
				{
					FaceRegion currentFace = null;

					for ( int x = 0; x < blob.detail; x++ )
					{
						final FaceRegion region = getRegion( blob, myFace, x, y, z, visFace, nextTo );

						if ( region == null )
						{
							currentFace = null;
							continue;
						}

						if ( currentFace != null )
						{
							if ( currentFace.extend( region ) )
							{
								continue;
							}
						}

						currentFace = region;
						regions.add( region );
					}
				}

				if ( !regions.isEmpty() )
				{
					rset.add( regions );
					regions = null;
				}
			}
		}
	}

	private int getShadeColor(
			final int[] vertData,
			final int offset,
			final FaceRegion region,
			final VoxelBlob blob,
			final int color )
	{
		return getShadeColor( region.face, 1.0f, color );
	}

	private FaceRegion getRegion(
			final VoxelBlob blob,
			final EnumFacing myFace,
			final int x,
			final int y,
			final int z,
			final VisibleFace visFace,
			final VoxelBlob nextTo )
	{
		blob.visibleFace( myFace, x, y, z, visFace, nextTo );

		if ( visFace.visibleFace )
		{
			final Vec3i off = myFace.getDirectionVec();
			final Vec3i center = new Vec3i( x * 2 + 1 + off.getX(), y * 2 + 1 + off.getY(), z * 2 + 1 + off.getZ() );

			return new FaceRegion( myFace, center, visFace.state, visFace.isEdge );
		}

		return null;
	}

	// merge face brightness with custom multiplier
	private int getShadeColor(
			final EnumFacing face,
			final float f,
			final int color )
	{
		final int i = MathHelper.clamp_int( (int) ( ( format == ChiselsAndBitsBakedQuad.VERTEX_FORMAT ? getFaceBrightness( face ) * f : f ) * 255.0F ), 0, 255 );

		final int r = ( color >> 16 & 0xff ) * i / 255;
		final int g = ( color >> 8 & 0xff ) * i / 255;
		final int b = ( color & 0xff ) * i / 255;

		return -16777216 | b << 16 | g << 8 | r;
	}

	// based on MC's FaceBakery...
	private float getFaceBrightness(
			final EnumFacing face )
	{
		switch ( face )
		{
			case DOWN:
				return 0.5F;
			case UP:
				return 1.0F;
			case NORTH:
			case SOUTH:
				return 0.8F;
			case WEST:
			case EAST:
				return 0.6F;
			default:
				return 1.0F;
		}
	}

	static void calcVertFaceMap()
	{
		if ( hasFaceMap )
		{
			return;
		}

		hasFaceMap = true;
		final Vector3f to = new Vector3f( 0, 0, 0 );
		final Vector3f from = new Vector3f( 16, 16, 16 );

		for ( final EnumFacing myFace : EnumFacing.VALUES )
		{
			final FaceBakery faceBakery = new FaceBakery();

			final BlockPartRotation bpr = null;
			final ModelRotation mr = ModelRotation.X0_Y0;

			final float[] defUVs = new float[] { 0, 0, 1, 1 };
			final BlockFaceUV uv = new BlockFaceUV( defUVs, 0 );
			final BlockPartFace bpf = new BlockPartFace( myFace, 0, "", uv );

			final TextureAtlasSprite texture = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
			final BakedQuad q = faceBakery.makeBakedQuad( to, from, bpf, texture, myFace, mr, bpr, true, true );

			final int[] vertData = q.getVertexData();

			int a = 0;
			int b = 2;

			switch ( myFace )
			{
				case NORTH:
				case SOUTH:
					a = 0;
					b = 1;
					break;
				case EAST:
				case WEST:
					a = 1;
					b = 2;
					break;
				default:
			}

			final int p = vertData.length / 4;
			for ( int vertNum = 0; vertNum < 4; vertNum++ )
			{
				if ( ModelUtil.isZero( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && ModelUtil.isZero( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
				{
					faceVertMap[myFace.getIndex()][vertNum] = 0;
				}
				else if ( ModelUtil.isZero( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && ModelUtil.isOne( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
				{
					faceVertMap[myFace.getIndex()][vertNum] = 3;
				}
				else if ( ModelUtil.isOne( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && ModelUtil.isZero( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
				{
					faceVertMap[myFace.getIndex()][vertNum] = 1;
				}
				else
				{
					faceVertMap[myFace.getIndex()][vertNum] = 2;
				}
			}
		}
	}

	private float[] getFaceUvs(
			final EnumFacing face,
			final Vector3f to16,
			final Vector3f from16,
			final float[] quadsUV )
	{
		float fromA = 0;
		float fromB = 0;
		float toA = 0;
		float toB = 0;

		switch ( face )
		{
			case UP:
				fromA = from16.x / 16.0f;
				fromB = from16.z / 16.0f;
				toA = to16.x / 16.0f;
				toB = to16.z / 16.0f;
				break;
			case DOWN:
				fromA = from16.x / 16.0f;
				fromB = from16.z / 16.0f;
				toA = to16.x / 16.0f;
				toB = to16.z / 16.0f;
				break;
			case SOUTH:
				fromA = from16.x / 16.0f;
				fromB = from16.y / 16.0f;
				toA = to16.x / 16.0f;
				toB = to16.y / 16.0f;
				break;
			case NORTH:
				fromA = from16.x / 16.0f;
				fromB = from16.y / 16.0f;
				toA = to16.x / 16.0f;
				toB = to16.y / 16.0f;
				break;
			case EAST:
				fromA = from16.y / 16.0f;
				fromB = from16.z / 16.0f;
				toA = to16.y / 16.0f;
				toB = to16.z / 16.0f;
				break;
			case WEST:
				fromA = from16.y / 16.0f;
				fromB = from16.z / 16.0f;
				toA = to16.y / 16.0f;
				toB = to16.z / 16.0f;
				break;
			default:
		}

		return new float[] {
				16.0f * u( quadsUV, fromA, fromB ), // 0
				16.0f * v( quadsUV, fromA, fromB ), // 1

				16.0f * u( quadsUV, toA, fromB ), // 2
				16.0f * v( quadsUV, toA, fromB ), // 3

				16.0f * u( quadsUV, toA, toB ), // 2
				16.0f * v( quadsUV, toA, toB ), // 3

				16.0f * u( quadsUV, fromA, toB ), // 0
				16.0f * v( quadsUV, fromA, toB ), // 1
		};
	}

	float u(
			final float[] src,
			final float inU,
			final float inV )
	{
		final float u1 = src[0] * inU + ( 1.0f - inU ) * src[2];
		final float u2 = src[4] * inU + ( 1.0f - inU ) * src[6];
		return u1 * inV + ( 1.0f - inV ) * u2;
	}

	float v(
			final float[] src,
			final float inU,
			final float inV )
	{
		final float v1 = src[1] * inU + ( 1.0f - inU ) * src[3];
		final float v2 = src[5] * inU + ( 1.0f - inU ) * src[7];
		return v1 * inV + ( 1.0f - inV ) * v2;
	}

	static private Vector3f offsetVec(
			final Vec3i to,
			final EnumFacing f,
			final int d )
	{

		int leftX = 0;
		final int leftY = 0;
		int leftZ = 0;

		final int upX = 0;
		int upY = 0;
		int upZ = 0;

		switch ( f )
		{
			case DOWN:
				leftX = 1;
				upZ = 1;
				break;
			case EAST:
				leftZ = 1;
				upY = 1;
				break;
			case NORTH:
				leftX = 1;
				upY = 1;
				break;
			case SOUTH:
				leftX = 1;
				upY = 1;
				break;
			case UP:
				leftX = 1;
				upZ = 1;
				break;
			case WEST:
				leftZ = 1;
				upY = 1;
				break;
			default:
				break;
		}

		final int x = to.getX() + leftX * d + upX * d;
		final int y = to.getY() + leftY * d + upY * d;
		final int z = to.getZ() + leftZ * d + upZ * d;

		return new Vector3f( x * 0.5f, y * 0.5f, z * 0.5f );
	}

	@Override
	public List<BakedQuad> getFaceQuads(
			final EnumFacing requestedFace )
	{
		return getList( requestedFace );
	}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return getList( null );
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return sprite != null ? sprite : ClientSide.instance.getMissingIcon();
	}

	@Override
	public VertexFormat getFormat()
	{
		return format;
	}

	public int faceCount()
	{
		int count = getList( null ).size();

		for ( final EnumFacing f : EnumFacing.VALUES )
		{
			count += getFaceQuads( f ).size();
		}

		return count;
	}

	public boolean isAboveLimit()
	{
		return faceCount() >= ChiselsAndBits.getConfig().dynamicModelFaceCount;
	}

}
