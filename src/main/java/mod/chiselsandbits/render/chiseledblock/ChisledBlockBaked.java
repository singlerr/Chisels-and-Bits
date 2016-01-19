package mod.chiselsandbits.render.chiseledblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import mod.chiselsandbits.chiseledblock.data.BitColors;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.VisibleFace;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.render.BaseBakedBlockModel;
import mod.chiselsandbits.render.helpers.ModelUVReader;
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
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;

import org.lwjgl.util.vector.Vector3f;

public class ChisledBlockBaked extends BaseBakedBlockModel
{
	public static final float PIXELS_PER_BLOCK = 16.0f;
	static boolean hasFaceMap = false;
	static int[][] faceVertMap = new int[6][4];

	private static final EnumFacing[] X_Faces = new EnumFacing[] { EnumFacing.EAST, EnumFacing.WEST };
	private static final EnumFacing[] Y_Faces = new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };
	private static final EnumFacing[] Z_Faces = new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.NORTH };

	public static final VertexFormat CNB = new VertexFormat();

	static
	{
		for ( final VertexFormatElement element : DefaultVertexFormats.ITEM.getElements() )
		{
			CNB.addElement( element );
		}

		// add lightmap ;)
		CNB.addElement( DefaultVertexFormats.TEX_2S );
	}

	@SuppressWarnings( "unchecked" )
	final List<BakedQuad>[] face = new List[6];
	List<BakedQuad> generic;
	EnumWorldBlockLayer myLayer;
	VertexFormat format;
	TextureAtlasSprite sprite;

	private ChisledBlockBaked()
	{
		initEmpty();
	}

	public ChisledBlockBaked(
			final int blockReference,
			final EnumWorldBlockLayer layer,
			final VoxelBlobStateReference data,
			final ModelRenderState mrs,
			final VertexFormat format )
	{
		myLayer = layer;
		this.format = format;
		final IBlockState state = Block.getStateById( blockReference );
		initEmpty();

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
				// create lists...
				face[0] = new ArrayList<BakedQuad>();
				face[1] = new ArrayList<BakedQuad>();
				face[2] = new ArrayList<BakedQuad>();
				face[3] = new ArrayList<BakedQuad>();
				face[4] = new ArrayList<BakedQuad>();
				face[5] = new ArrayList<BakedQuad>();
				generic = new ArrayList<BakedQuad>();

				generateFaces( vb, mrs, data.weight );
			}
		}
	}

	public static ChisledBlockBaked breakingParticleModel(
			final EnumWorldBlockLayer layer,
			final Integer blockStateID )
	{
		final ChisledBlockBaked out = new ChisledBlockBaked();

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
		boolean trulyEmpty = generic.isEmpty();

		for ( final List<BakedQuad> l : face )
		{
			trulyEmpty = trulyEmpty && l.isEmpty();
		}

		return trulyEmpty;
	}

	private void initEmpty()
	{
		face[0] = Collections.emptyList();
		face[1] = Collections.emptyList();
		face[2] = Collections.emptyList();
		face[3] = Collections.emptyList();
		face[4] = Collections.emptyList();
		face[5] = Collections.emptyList();
		generic = Collections.emptyList();
	}

	@SuppressWarnings( "deprecation" )
	private void generateFaces(
			final VoxelBlob blob,
			final ModelRenderState mrs,
			final long weight )
	{
		final FaceBakery faceBakery = new FaceBakery();

		final BlockPartRotation bpr = null;
		final ModelRotation mr = ModelRotation.X0_Y0;

		final ArrayList<ArrayList<FaceRegion>> rset = new ArrayList<ArrayList<FaceRegion>>();
		final HashMap<Integer, float[]> sourceUVCache = new HashMap<Integer, float[]>();
		final VisibleFace visFace = new VisibleFace();

		processXFaces( blob, visFace, mrs, rset );
		processYFaces( blob, visFace, mrs, rset );
		processZFaces( blob, visFace, mrs, rset );

		final float[] defUVs = new float[] { 0, 0, 1, 1 };

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

				final IBlockState state = Block.getStateById( region.blockStateID );
				final IBakedModel model = ModelUtil.solveModel( region.blockStateID, weight, Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( state ) );
				final TextureAtlasSprite texture = ModelUtil.findTexture( region.blockStateID, model, myFace, myLayer );

				sprite = texture;

				final int lightValue = Math.max( state.getBlock().getLightValue(), ModelUtil.findLightValue( region.blockStateID, model ) );

				final BlockFaceUV uv = new BlockFaceUV( defUVs, 0 );
				final BlockPartFace bpf = new BlockPartFace( myFace, -1, "", uv );

				final float[] uvs = getFaceUvs( myFace, from, to, getSourceUVs( sourceUVCache, region.blockStateID, weight, texture, myFace ) );
				final BakedQuad g = faceBakery.makeBakedQuad( to, from, bpf, texture, myFace, mr, bpr, true, true );

				final HackedUnpackedBakedQuad.Builder b = new HackedUnpackedBakedQuad.Builder( format );
				b.setQuadColored();
				b.setQuadOrientation( myFace );
				b.setQuadTint( 0 );

				final int[] vertData = g.getVertexData();
				final int wrapAt = vertData.length / 4;

				final int color = BitColors.getColorFor( state, myLayer.ordinal() );
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
								b.put( elementIndex, Float.intBitsToFloat( vertData[0 + wrapAt * vertNum] ), Float.intBitsToFloat( vertData[1 + wrapAt * vertNum] ), Float.intBitsToFloat( vertData[2 + wrapAt * vertNum] ) );
								break;

							case COLOR:
								final int cb = getShadeColor( vertData, wrapAt * vertNum, region, blob, color );
								b.put( elementIndex, byteToFloat( cb ), byteToFloat( cb >> 8 ), byteToFloat( cb >> 16 ), byteToFloat( cb >> 24 ) );
								break;

							case NORMAL:
								b.put( elementIndex, myFace.getFrontOffsetX(), myFace.getFrontOffsetY(), myFace.getFrontOffsetZ() );
								break;

							case UV:
								if ( element.getIndex() == 1 )
								{
									final float v = maxLightmap * Math.max( 0, Math.min( 15, lightValue ) );
									b.put( elementIndex, v, v );
								}
								else
								{
									b.put( elementIndex, texture.getInterpolatedU( uvs[faceVertMap[myFace.getIndex()][vertNum] * 2 + 0] ), texture.getInterpolatedV( uvs[faceVertMap[myFace.getIndex()][vertNum] * 2 + 1] ) );
								}
								break;

							default:
								b.put( elementIndex );
								break;
						}
					}
				}

				if ( region.isEdge )
				{
					face[myFace.ordinal()].add( b.build() );
				}
				else
				{
					generic.add( b.build() );
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
		final int i = MathHelper.clamp_int( (int) ( ( format == CNB ? getFaceBrightness( face ) * f : f ) * 255.0F ), 0, 255 );

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

	private float[] getSourceUVs(
			final HashMap<Integer, float[]> sourceUVCache,
			final int id,
			final long weight,
			final TextureAtlasSprite texture,
			final EnumFacing myFace )
	{
		float[] quadUVs = sourceUVCache.get( id << 4 | myFace.getIndex() );

		if ( quadUVs == null )
		{
			final IBakedModel model = ModelUtil.solveModel( id, weight, Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( Block.getStateById( id ) ) );
			if ( model != null )
			{
				if ( model != null )
				{
					final List<BakedQuad> quads = model.getFaceQuads( myFace );

					// top/bottom
					int uCoord = 0;
					int vCoord = 2;

					switch ( myFace )
					{
						case NORTH:
						case SOUTH:
							uCoord = 0;
							vCoord = 1;
							break;
						case EAST:
						case WEST:
							uCoord = 1;
							vCoord = 2;
							break;
						default:
					}

					final ModelUVReader uvr = new ModelUVReader( texture, uCoord, vCoord );

					// process all the quads on the face.
					for ( final BakedQuad src : quads )
					{
						if ( src.getFace() == myFace )
						{
							src.pipe( uvr );
						}
					}

					// was the auto-discover fully successful?
					if ( uvr.corners == 0xf )
					{
						quadUVs = uvr.quadUVs;
					}
				}
			}

			// default.
			if ( quadUVs == null )
			{
				quadUVs = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 };
			}

			sourceUVCache.put( id << 4 | myFace.getIndex(), quadUVs );
		}

		return quadUVs;
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
		return face[requestedFace.ordinal()];
			}

	@Override
	public List<BakedQuad> getGeneralQuads()
	{
		return generic;
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
		int count = generic.size();

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
