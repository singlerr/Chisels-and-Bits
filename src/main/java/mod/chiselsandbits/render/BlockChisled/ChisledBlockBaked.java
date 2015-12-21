
package mod.chiselsandbits.render.BlockChisled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.SimpleQuadReaderBase;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.VisibleFace;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobState;
import mod.chiselsandbits.items.BitColors;
import mod.chiselsandbits.render.BaseBakedModel;
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
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad.Builder;

@SuppressWarnings( "deprecation" )
public class ChisledBlockBaked extends BaseBakedModel
{
	IBakedModel originalModel;

	@SuppressWarnings( "unchecked" )
	final List<BakedQuad>[] face = new List[6];
	List<BakedQuad> generic;
	EnumWorldBlockLayer myLayer;
	VertexFormat format;

	public int sides = 0;

	public static final float pixelsPerBlock = 16.0f;
	private static final ChisledBlockBaked emptyPlaceHolder = new ChisledBlockBaked();

	public boolean isEmpty()
	{
		boolean trulyEmpty = generic.isEmpty();

		for ( final List<BakedQuad> l : face )
		{
			trulyEmpty = trulyEmpty && l.isEmpty();
		}

		return trulyEmpty;
	}

	public ChisledBlockBaked getEmptyModel()
	{
		return emptyPlaceHolder;
	}

	private ChisledBlockBaked()
	{
		initEmpty();
	}

	public ChisledBlockBaked(
			final int BlockRef,
			final EnumWorldBlockLayer layer,
			final VoxelBlobState data,
			final ModelRenderState mrs,
			final VertexFormat format )
	{
		myLayer = layer;
		this.format = format;
		final IBlockState state = Block.getStateById( BlockRef );
		initEmpty();

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

	final static VertexFormat CNB = new VertexFormat();

	static
	{
		CNB.addElement( DefaultVertexFormats.POSITION_3F );
		CNB.addElement( DefaultVertexFormats.COLOR_4UB );
		CNB.addElement( DefaultVertexFormats.TEX_2F );
		CNB.addElement( DefaultVertexFormats.TEX_2S );
		CNB.addElement( DefaultVertexFormats.NORMAL_3B );
	}

	private final static EnumFacing[] X_Faces = new EnumFacing[] { EnumFacing.EAST, EnumFacing.WEST };
	private final static EnumFacing[] Y_Faces = new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };
	private final static EnumFacing[] Z_Faces = new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.NORTH };

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
		sides = blob.getSideFlags( 0, VoxelBlob.dim_minus_one, VoxelBlob.dim2 );

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
				final IBakedModel model = ClientSide.instance.solveModel( region.blockStateID, weight, Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( state ) );
				final TextureAtlasSprite texture = ClientSide.findTexture( region.blockStateID, model, myFace, myLayer );

				final int lightValue = Math.max( state.getBlock().getLightValue(), ClientSide.findLightValue( region.blockStateID, model ) );

				final BlockFaceUV uv = new BlockFaceUV( defUVs, 0 );
				final BlockPartFace bpf = new BlockPartFace( myFace, -1, "", uv );

				final float[] uvs = getFaceUvs( myFace, from, to, getSourceUVs( sourceUVCache, region.blockStateID, weight, texture, myFace ) );
				final BakedQuad g = faceBakery.makeBakedQuad( to, from, bpf, texture, myFace, mr, bpr, true, true );

				final UnpackedBakedQuad.Builder b = new Builder( format );
				b.setQuadColored();
				b.setQuadOrientation( myFace );
				b.setQuadTint( 0 );

				final int[] vertData = g.getVertexData();
				final int wrapAt = vertData.length / 4;

				final int color = BitColors.getColorFor( state, myLayer.ordinal() );
				calcVertFaceMap();

				final float maxLightmap = 32.0f / 0xffff;

				// build un
				final VertexFormat format = b.getVertexFormat();
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
		boolean restart = false;

		do
		{
			restart = false;

			final int size = src.size();
			final int size_minus_one = size - 1;
			restart: for ( int x = 0; x < size_minus_one; ++x )
			{
				final FaceRegion A = src.get( x );

				for ( int y = x + 1; y < size; ++y )
				{
					final FaceRegion B = src.get( y );

					if ( A.extend( B ) )
					{
						src.set( y, src.get( size_minus_one ) );
						src.remove( size_minus_one );

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
			final VoxelBlobState nextToState = mrs != null && myLayer == EnumWorldBlockLayer.TRANSLUCENT ? mrs.sides[myFace.ordinal()] : null;
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

					// row complete!
					currentFace = null;
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
			final VoxelBlobState nextToState = mrs != null && myLayer == EnumWorldBlockLayer.TRANSLUCENT ? mrs.sides[myFace.ordinal()] : null;
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

					// row complete!
					currentFace = null;
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
			final VoxelBlobState nextToState = mrs != null && myLayer == EnumWorldBlockLayer.TRANSLUCENT ? mrs.sides[myFace.ordinal()] : null;
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

					// row complete!
					currentFace = null;
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
			float f,
			final int color )
	{
		if ( format == CNB )
		{
			f *= getFaceBrightness( face );
		}

		final int i = MathHelper.clamp_int( (int) ( f * 255.0F ), 0, 255 );

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

	static boolean hasFaceMap = false;
	static int faceVertMap[][] = new int[6][4];

	static private boolean isOne(
			final float v )
	{
		return Math.abs( v ) < 0.01;
	}

	static private boolean isZero(
			final float v )
	{
		return Math.abs( v - 1.0f ) < 0.01;
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
				if ( isZero( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && isZero( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
				{
					faceVertMap[myFace.getIndex()][vertNum] = 0;
				}
				else if ( isZero( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && isOne( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
				{
					faceVertMap[myFace.getIndex()][vertNum] = 3;
				}
				else if ( isOne( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && isZero( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
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

	private static class UVReader extends SimpleQuadReaderBase
	{

		final float minU;
		final float maxUMinusMin;

		final float minV;
		final float maxVMinusMin;

		final float[] quadUVs = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 };

		int uCoord, vCoord;

		public UVReader(
				final TextureAtlasSprite texture,
				final int uFaceCoord,
				final int vFaceCoord )
		{
			minU = texture.getMinU();
			maxUMinusMin = texture.getMaxU() - minU;

			minV = texture.getMinV();
			maxVMinusMin = texture.getMaxV() - minV;

			uCoord = uFaceCoord;
			vCoord = vFaceCoord;
		}

		private float pos[];
		private float uv[];
		public int corners;

		@Override
		public void put(
				final int element,
				final float... data )
		{
			final VertexFormat format = getVertexFormat();
			final VertexFormatElement ele = format.getElement( element );

			if ( ele.getUsage() == EnumUsage.UV && ele.getIndex() != 1 )
			{
				uv = Arrays.copyOf( data, data.length );
			}

			else if ( ele.getUsage() == EnumUsage.POSITION )
			{
				pos = Arrays.copyOf( data, data.length );
			}

			if ( element == format.getElementCount() - 1 )
			{
				if ( isZero( pos[uCoord] ) && isZero( pos[vCoord] ) )
				{
					corners = corners | 0x1;
					quadUVs[0] = ( uv[0] - minU ) / maxUMinusMin;
					quadUVs[1] = ( uv[1] - minV ) / maxVMinusMin;
				}
				else if ( isZero( pos[uCoord] ) && isOne( pos[vCoord] ) )
				{
					corners = corners | 0x2;
					quadUVs[4] = ( uv[0] - minU ) / maxUMinusMin;
					quadUVs[5] = ( uv[1] - minV ) / maxVMinusMin;
				}
				else if ( isOne( pos[uCoord] ) && isZero( pos[vCoord] ) )
				{
					corners = corners | 0x4;
					quadUVs[2] = ( uv[0] - minU ) / maxUMinusMin;
					quadUVs[3] = ( uv[1] - minV ) / maxVMinusMin;
				}
				else
				{
					corners = corners | 0x8;
					quadUVs[6] = ( uv[0] - minU ) / maxUMinusMin;
					quadUVs[7] = ( uv[1] - minV ) / maxVMinusMin;
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
			final IBakedModel model = ClientSide.instance.solveModel( id, weight, Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( Block.getStateById( id ) ) );
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

					final UVReader uvr = new UVReader( texture, uCoord, vCoord );

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
			final Vector3f to_16,
			final Vector3f from_16,
			final float quadsUV[] )
	{
		float from_a = 0;
		float from_b = 0;
		float to_a = 0;
		float to_b = 0;

		switch ( face )
		{
			case UP:
				from_a = from_16.x / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			case DOWN:
				from_a = from_16.x / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			case SOUTH:
				from_a = from_16.x / 16.0f;
				from_b = from_16.y / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.y / 16.0f;
				break;
			case NORTH:
				from_a = from_16.x / 16.0f;
				from_b = from_16.y / 16.0f;
				to_a = to_16.x / 16.0f;
				to_b = to_16.y / 16.0f;
				break;
			case EAST:
				from_a = from_16.y / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.y / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			case WEST:
				from_a = from_16.y / 16.0f;
				from_b = from_16.z / 16.0f;
				to_a = to_16.y / 16.0f;
				to_b = to_16.z / 16.0f;
				break;
			default:
		}

		final float[] afloat = new float[] { // :P
				16.0f * u( quadsUV, from_a, from_b ), // 0
				16.0f * v( quadsUV, from_a, from_b ), // 1

				16.0f * u( quadsUV, to_a, from_b ), // 2
				16.0f * v( quadsUV, to_a, from_b ), // 3

				16.0f * u( quadsUV, to_a, to_b ), // 2
				16.0f * v( quadsUV, to_a, to_b ), // 3

				16.0f * u( quadsUV, from_a, to_b ), // 0
				16.0f * v( quadsUV, from_a, to_b ), // 1
		};

		return afloat;

	}

	float u(
			final float[] src,
			final float U,
			final float V )
	{
		final float u1 = src[0] * U + ( 1.0f - U ) * src[2];
		final float u2 = src[4] * U + ( 1.0f - U ) * src[6];
		return u1 * V + ( 1.0f - V ) * u2;
	}

	float v(
			final float[] src,
			final float U,
			final float V )
	{
		final float v1 = src[1] * U + ( 1.0f - U ) * src[3];
		final float v2 = src[5] * U + ( 1.0f - U ) * src[7];
		return v1 * V + ( 1.0f - V ) * v2;
	}

	static private Vector3f offsetVec(
			final Vec3i to,
			final EnumFacing f,
			final int d )
	{

		int left_x = 0;
		final int left_y = 0;
		int left_z = 0;

		final int up_x = 0;
		int up_y = 0;
		int up_z = 0;

		switch ( f )
		{
			case DOWN:
				left_x = 1;
				up_z = 1;
				break;
			case EAST:
				left_z = 1;
				up_y = 1;
				break;
			case NORTH:
				left_x = 1;
				up_y = 1;
				break;
			case SOUTH:
				left_x = 1;
				up_y = 1;
				break;
			case UP:
				left_x = 1;
				up_z = 1;
				break;
			case WEST:
				left_z = 1;
				up_y = 1;
				break;
			default:
				break;
		}

		final int x = to.getX() + left_x * d + up_x * d;
		final int y = to.getY() + left_y * d + up_y * d;
		final int z = to.getZ() + left_z * d + up_z * d;

		return new Vector3f( x * 0.5f, y * 0.5f, z * 0.5f );
	}

	@Override
	public List<BakedQuad> getFaceQuads(
			final EnumFacing p_177551_1_ )
	{
		return face[p_177551_1_.ordinal()];
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
	public TextureAtlasSprite getParticleTexture()
	{
		return originalModel == null ? ClientSide.instance.getMissingIcon() : originalModel.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public VertexFormat getFormat()
	{
		return format;
	}

}
