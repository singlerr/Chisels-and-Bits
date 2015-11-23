
package mod.chiselsandbits.render.BlockChisled;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.VisibleFace;
import mod.chiselsandbits.chiseledblock.data.VoxelBlobState;
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
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ISmartBlockModel;


@SuppressWarnings( "deprecation" )
public class ChisledBlockBaked implements IFlexibleBakedModel
{
	IBakedModel originalModel;

	@SuppressWarnings( "unchecked" )
	final List<BakedQuad>[] face = new List[6];
	List<BakedQuad> generic;

	public static final float pixelsPerBlock = 16.0f;

	public ChisledBlockBaked(
			final int BlockRef,
			final EnumWorldBlockLayer layer,
			final VoxelBlobState data )
	{
		final IBlockState state = Block.getStateById( BlockRef );
		initEmpty();

		if ( state != null )
		{
			originalModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( state );
		}

		if ( originalModel != null && data != null )
		{
			final VoxelBlob vb = new VoxelBlob();
			try
			{
				vb.fromByteArray( data.getByteArray() );
				if ( vb.filter( layer ) )
				{
					// create lists...
					face[0] = new ArrayList<BakedQuad>();
					face[1] = new ArrayList<BakedQuad>();
					face[2] = new ArrayList<BakedQuad>();
					face[3] = new ArrayList<BakedQuad>();
					face[4] = new ArrayList<BakedQuad>();
					face[5] = new ArrayList<BakedQuad>();
					generic = new ArrayList<BakedQuad>();

					generateFaces( vb, data.weight );
				}
			}
			catch ( final IOException e )
			{

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

	private final static EnumFacing[] X_Faces = new EnumFacing[] { EnumFacing.EAST, EnumFacing.WEST };
	private final static EnumFacing[] Y_Faces = new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };
	private final static EnumFacing[] Z_Faces = new EnumFacing[] { EnumFacing.SOUTH, EnumFacing.NORTH };

	private void generateFaces(
			final VoxelBlob blob,
			final long weight )
	{
		final FaceBakery faceBakery = new FaceBakery();

		final BlockPartRotation bpr = null;
		final ModelRotation mr = ModelRotation.X0_Y0;

		final Multimap<Integer, FaceRegion> rset = ArrayListMultimap.create();
		final HashMap<Integer, float[]> sourceUVCache = new HashMap<Integer, float[]>();
		final VisibleFace visFace = new VisibleFace();

		for ( final EnumFacing myFace : X_Faces )
		{
			for ( int x = 0; x < blob.detail; x++ )
			{
				for ( int z = 0; z < blob.detail; z++ )
				{
					FaceRegion currentFace = null;

					for ( int y = 0; y < blob.detail; y++ )
					{
						final FaceRegion region = getRegion( blob, myFace, x, y, z, visFace );

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
						rset.put( getBucket( myFace, x, y, z ), region );
					}

					// row complete!
					currentFace = null;
				}
			}
		}

		for ( final EnumFacing myFace : Y_Faces )
		{
			for ( int y = 0; y < blob.detail; y++ )
			{
				for ( int z = 0; z < blob.detail; z++ )
				{
					FaceRegion currentFace = null;

					for ( int x = 0; x < blob.detail; x++ )
					{
						final FaceRegion region = getRegion( blob, myFace, x, y, z, visFace );

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
						rset.put( getBucket( myFace, x, y, z ), region );
					}

					// row complete!
					currentFace = null;
				}
			}
		}

		for ( final EnumFacing myFace : Z_Faces )
		{
			for ( int z = 0; z < blob.detail; z++ )
			{
				for ( int y = 0; y < blob.detail; y++ )
				{
					FaceRegion currentFace = null;

					for ( int x = 0; x < blob.detail; x++ )
					{
						final FaceRegion region = getRegion( blob, myFace, x, y, z, visFace );

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
						rset.put( getBucket( myFace, x, y, z ), region );
					}

					// row complete!
					currentFace = null;
				}
			}
		}

		final ArrayList<Integer> keys = new ArrayList<Integer>( rset.keySet() );
		final float[] defUVs = new float[] { 0, 0, 1, 1 };

		for ( final int key : keys )
		{
			final Collection<FaceRegion> src = rset.get( key );
			boolean restart = false;

			do
			{
				restart = false;

				restart: for ( final FaceRegion A : src )
				{
					for ( final FaceRegion B : src )
					{
						if ( A != B && A.extend( B ) )
						{
							src.remove( B );
							restart = true;
							break restart;
						}
					}
				}
			}
			while ( restart );

			for ( final FaceRegion region : src )
			{
				final EnumFacing myFace = region.face;

				// keep integers up until the last moment...
				final Vector3f to = offsetVec( region.max, myFace, 1 );
				final Vector3f from = offsetVec( region.min, myFace, -1 );

				final IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( Block.getStateById( region.blockStateID ) );
				final TextureAtlasSprite texture = ClientSide.findTexture( region.blockStateID, model );

				final BlockFaceUV uv = new BlockFaceUV( defUVs, 0 );
				final BlockPartFace bpf = new BlockPartFace( myFace, 0, "", uv );

				final float[] uvs = getFaceUvs( myFace, from, to, getSourceUVs( sourceUVCache, region.blockStateID, weight, texture, myFace ) );
				final BakedQuad q = faceBakery.makeBakedQuad( to, from, bpf, texture, myFace, mr, bpr, true, true );

				final int[] vertData = q.getVertexData();
				int wrapAt = vertData.length / 4;
				
				vertData[0 + 3] = getShadeColor( vertData, 0, region, blob );
				vertData[wrapAt*1 + 3] = getShadeColor( vertData, wrapAt*1, region, blob );
				vertData[wrapAt*2 + 3] = getShadeColor( vertData, wrapAt*2, region, blob );
				vertData[wrapAt*3 + 3] = getShadeColor( vertData, wrapAt*3, region, blob );
				
				calcVertFaceMap();

				for ( int vertNum = 0; vertNum < 4; vertNum++ )
				{
					vertData[vertNum * wrapAt+ 4] = Float.floatToRawIntBits( texture.getInterpolatedU( uvs[faceVertMap[myFace.getIndex()][vertNum] * 2 + 0] ) );
					vertData[vertNum * wrapAt + 5] = Float.floatToRawIntBits( texture.getInterpolatedV( uvs[faceVertMap[myFace.getIndex()][vertNum] * 2 + 1] ) );
				}

				/*
				 * vertData[7 + 4] = Float.floatToRawIntBits( texture.getInterpolatedU( uvs[2] ) );
				 * vertData[7 + 5] = Float.floatToRawIntBits( texture.getInterpolatedV( uvs[3] ) );
				 * vertData[14 + 4] = Float.floatToRawIntBits( texture.getInterpolatedU( uvs[4] ) );
				 * vertData[14 + 5] = Float.floatToRawIntBits( texture.getInterpolatedV( uvs[5] ) );
				 * vertData[21 + 4] = Float.floatToRawIntBits( texture.getInterpolatedU( uvs[6] ) );
				 * vertData[21 + 5] = Float.floatToRawIntBits( texture.getInterpolatedV( uvs[7] ) );
				 */

				if ( region.isEdge )
				{
					face[myFace.ordinal()].add( q );
				}
				else
				{
					generic.add( q );
				}
			}
		}
	}

	private int getShadeColor(
			final int[] vertData,
			final int offset,
			final FaceRegion region,
			final VoxelBlob blob )
	{
		/*
		 * int x = ( int ) ( pixelsPerBlock * Float.intBitsToFloat( vertData[offset] ) );
		 * int y = ( int ) ( pixelsPerBlock * Float.intBitsToFloat( vertData[offset + 1] ) );
		 * int z = ( int ) ( pixelsPerBlock * Float.intBitsToFloat( vertData[offset + 2] ) );
		 * switch ( region.face )
		 * {
		 * case DOWN:
		 * y = region.min.getY() / 2 - 1;
		 * break;
		 * case UP:
		 * y = region.min.getY() / 2;
		 * break;
		 * case EAST:
		 * x = region.min.getX() / 2;
		 * break;
		 * case WEST:
		 * x = region.min.getX() / 2 - 1;
		 * break;
		 * case SOUTH:
		 * z = region.min.getZ() / 2;
		 * break;
		 * case NORTH:
		 * z = region.min.getZ() / 2 - 1;
		 * break;
		 * default:
		 * break;
		 * }
		 * final int sides = ( blob.getSafe( x + 1, y, z ) != 0 ? 1 : 0 ) + ( blob.getSafe( x, y + 1, z ) != 0 ? 1 : 0 )
		 * + ( blob.getSafe( x, y, z + 1 ) != 0 ? 1 : 0 ) + ( blob.getSafe( x - 1, y, z ) != 0 ? 1 : 0 ) + (
		 * blob.getSafe( x, y - 1, z ) != 0 ? 1 : 0 ) + ( blob.getSafe( x, y, z - 1 ) != 0 ? 1 : 0 );
		 * final float multiplier = sides <= 1 ? 1.0f : 0.90f - ( 6 - sides ) * 0.026f;
		 */

		return getShadeColor( region.face, 1.0f );
	}

	private FaceRegion getRegion(
			final VoxelBlob blob,
			final EnumFacing myFace,
			final int x,
			final int y,
			final int z,
			final VisibleFace visFace )
	{
		blob.visibleFace( myFace, x, y, z, visFace );

		if ( visFace.visibleFace )
		{
			final Vec3i off = myFace.getDirectionVec();
			final Vec3i center = new Vec3i( x * 2 + 1 + off.getX(), y * 2 + 1 + off.getY(), z * 2 + 1 + off.getZ() );

			return new FaceRegion( myFace, center, visFace.state, visFace.isEdge );
		}

		return null;
	}

	private int getBucket(
			final EnumFacing face,
			final int x,
			final int y,
			final int z )
	{
		switch ( face )
		{
			case DOWN:
			case UP:
				return y << 5 | face.ordinal();
			case EAST:
			case WEST:
				return x << 5 | face.ordinal();
			case SOUTH:
			case NORTH:
				return z << 5 | face.ordinal();
			default:
				return 0;
		}
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

	// merge face brightness with custom multiplier
	private int getShadeColor(
			final EnumFacing face,
			float f )
	{
		f *= getFaceBrightness( face );
		final int i = MathHelper.clamp_int( ( int ) ( f * 255.0F ), 0, 255 );
		return -16777216 | i << 16 | i << 8 | i;
	}

	static boolean hasFaceMap = false;
	static int faceVertMap[][] = new int[6][4];

	static void calcVertFaceMap()
	{
		//if ( hasFaceMap )return;
		
		hasFaceMap = true;
		Vector3f to = new Vector3f(0,0,0);
		Vector3f from = new Vector3f(16,16,16);
		
		for (EnumFacing myFace : EnumFacing.VALUES )
		{
			final FaceBakery faceBakery = new FaceBakery();

			final BlockPartRotation bpr = null;
			final ModelRotation mr = ModelRotation.X0_Y0;

			final float[] defUVs = new float[] { 0, 0, 1, 1 };
			final BlockFaceUV uv = new BlockFaceUV( defUVs, 0 );
			final BlockPartFace bpf = new BlockPartFace( myFace, 0, "", uv );

			TextureAtlasSprite texture = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
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
					//quadUVs[vertNum*2+0] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
					//quadUVs[vertNum*2+1] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
				}
				else if ( isZero( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && isOne( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
				{
					faceVertMap[myFace.getIndex()][vertNum] = 3;
					//quadUVs[4] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
					//quadUVs[5] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
				}
				else if ( isOne( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && isZero( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
				{
					faceVertMap[myFace.getIndex()][vertNum] = 1;
					//quadUVs[2] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
					//quadUVs[3] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
				}
				else
				{
					faceVertMap[myFace.getIndex()][vertNum] = 2;
					//quadUVs[6] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
					//quadUVs[7] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
				}
			}	
		}
	}
	
	@SuppressWarnings( "unchecked" )
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
			quadUVs = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 };

			IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState( Block.getStateById( id ) );
			if ( model != null )
			{
				if ( model != null && ChiselsAndBits.instance.config.allowBlockAlternatives && model instanceof WeightedBakedModel )
				{
					model = ( ( WeightedBakedModel ) model ).getAlternativeModel( weight );
				}
				
				try
				{
					if ( model instanceof ISmartBlockModel )
					{
						IBakedModel newModel = ( (ISmartBlockModel) model ).handleBlockState(Block.getStateById(id));
						if ( newModel != null ) model = newModel;
					}
				}
				catch( Exception err ) {}
				
				if ( model != null )
				{
					final List<BakedQuad> quads = model.getFaceQuads( myFace );

					if ( quads.size() == 1 )
					{
						final BakedQuad src = quads.get( 0 );
						final int[] vertData = src.getVertexData();

						final float minU = texture.getMinU();
						final float maxUMinusMin = texture.getMaxU() - minU;

						final float minV = texture.getMinV();
						final float maxVMinusMin = texture.getMaxV() - minV;

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
								//faceVertMap[myFace.getIndex()][vertNum] = 0;
								quadUVs[0] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
								quadUVs[1] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
							}
							else if ( isZero( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && isOne( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
							{
								//faceVertMap[myFace.getIndex()][vertNum] = 3;
								quadUVs[4] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
								quadUVs[5] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
							}
							else if ( isOne( Float.intBitsToFloat( vertData[vertNum * p + a] ) ) && isZero( Float.intBitsToFloat( vertData[vertNum * p + b] ) ) )
							{
								//faceVertMap[myFace.getIndex()][vertNum] = 1;
								quadUVs[2] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
								quadUVs[3] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
							}
							else
							{
								//faceVertMap[myFace.getIndex()][vertNum] = 2;
								quadUVs[6] = ( Float.intBitsToFloat( vertData[vertNum * p + 4] ) - minU ) / maxUMinusMin;
								quadUVs[7] = ( Float.intBitsToFloat( vertData[vertNum * p + 5] ) - minV ) / maxVMinusMin;
							}
						}
					}
				}
			}
			
			//quadUVs[2] -= quadUVs[0];
			//quadUVs[3] -= quadUVs[1];
			//quadUVs[4] -= quadUVs[0];
			//quadUVs[5] -= quadUVs[1];

			sourceUVCache.put( id << 4 | myFace.getIndex(), quadUVs );
		}

		return quadUVs;
	}

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

		/*
		from_a = 1.0f - from_a;
		from_b = 1.0f - from_b;
		to_a = 1.0f - to_a;
		to_b = 1.0f - to_b;

		final float[] afloat = new float[] {// :P
		16.0f * ( quadsUV[0] + quadsUV[2] * from_a + quadsUV[4] * from_b ), // 0
		16.0f * ( quadsUV[1] + quadsUV[3] * from_a + quadsUV[5] * from_b ), // 1

		16.0f * ( quadsUV[0] + quadsUV[2] * to_a + quadsUV[4] * from_b ), // 2
		16.0f * ( quadsUV[1] + quadsUV[3] * to_a + quadsUV[5] * from_b ), // 3

		16.0f * ( quadsUV[0] + quadsUV[2] * to_a + quadsUV[4] * to_b ), // 2
		16.0f * ( quadsUV[1] + quadsUV[3] * to_a + quadsUV[5] * to_b ), // 3

		16.0f * ( quadsUV[0] + quadsUV[2] * from_a + quadsUV[4] * to_b ), // 0
		16.0f * ( quadsUV[1] + quadsUV[3] * from_a + quadsUV[5] * to_b ), // 1
		};
		*/
		
		final float[] afloat = new float[] {// :P
		16.0f * u( quadsUV,  from_a, from_b ), // 0
		16.0f * v( quadsUV, from_a , from_b ), // 1

		16.0f * u( quadsUV, to_a , from_b ), // 2
		16.0f * v( quadsUV, to_a , from_b ), // 3

		16.0f * u( quadsUV, to_a , to_b ), // 2
		16.0f * v( quadsUV, to_a , to_b ), // 3

		16.0f * u( quadsUV, from_a , to_b ), // 0
		16.0f * v( quadsUV, from_a, to_b ), // 1
		};

		return afloat;

	}

	float u ( float[] src, float U, float V )
	{
		float u1 = src[0] * U + ( 1.0f - U ) * src[2];
		float u2 = src[4] * U + ( 1.0f - U ) * src[6];
		return u1 * V + ( 1.0f - V ) * u2;
	}

	float v( float[] src, float U, float V )
	{
		float v1 = src[1] * U + ( 1.0f - U ) * src[3];
		float v2 = src[5] * U + ( 1.0f - U ) * src[7];
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
