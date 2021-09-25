package mod.chiselsandbits.client.model.baked.simple;

import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class SimpleGeneratedModel implements IBakedModel
{

    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] face = new List[6];

    private final TextureAtlasSprite texture;

    public SimpleGeneratedModel(
      final TextureAtlasSprite texture )
    {
        // create lists...
        face[0] = new ArrayList<>();
        face[1] = new ArrayList<>();
        face[2] = new ArrayList<>();
        face[3] = new ArrayList<>();
        face[4] = new ArrayList<>();
        face[5] = new ArrayList<>();

        this.texture = texture;

        final float[] afloat = new float[] { 0, 0, 16, 16 };
        final BlockFaceUV uv = new BlockFaceUV( afloat, 0 );
        final FaceBakery faceBakery = new FaceBakery();

        final Vector3f to = new Vector3f( 0.0f, 0.0f, 0.0f );
        final Vector3f from = new Vector3f( 16.0f, 16.0f, 16.0f );

        final ModelRotation mr = ModelRotation.X0_Y0;

        for ( final Direction side : Direction.values() )
        {
            final BlockPartFace bpf = new BlockPartFace( side, 1, "", uv );

            Vector3f toB, fromB;

            switch ( side )
            {
                case UP:
                    toB = new Vector3f( to.x(), from.y(), to.z() );
                    fromB = new Vector3f( from.x(), from.y(), from.z() );
                    break;
                case EAST:
                    toB = new Vector3f( from.x(), to.y(), to.z() );
                    fromB = new Vector3f( from.x(), from.y(), from.z() );
                    break;
                case NORTH:
                    toB = new Vector3f( to.x(), to.y(), to.z() );
                    fromB = new Vector3f( from.x(), from.y(), to.z() );
                    break;
                case SOUTH:
                    toB = new Vector3f( to.x(), to.y(), from.z() );
                    fromB = new Vector3f( from.x(), from.y(), from.z() );
                    break;
                case DOWN:
                    toB = new Vector3f( to.x(), to.y(), to.z() );
                    fromB = new Vector3f( from.x(), to.y(), from.z() );
                    break;
                case WEST:
                    toB = new Vector3f( to.x(), to.y(), to.z() );
                    fromB = new Vector3f( to.x(), from.y(), from.z() );
                    break;
                default:
                    throw new NullPointerException();
            }

            final BakedQuad g = faceBakery.bakeQuad( toB, fromB, bpf, texture, side, mr, null, false, new ResourceLocation(Constants.MOD_ID, "simple"));
            face[side.ordinal()].add( finishFace( g, side) );
        }
    }

    private BakedQuad finishFace(
      final BakedQuad g,
      final Direction myFace)
    {
        final int[] vertData = g.getVertices();
        final int wrapAt = vertData.length / 4;

        final BakedQuadBuilder b = new BakedQuadBuilder( g.sprite );
        b.setQuadOrientation( myFace );
        b.setQuadTint( 1 );

        for ( int vertNum = 0; vertNum < 4; vertNum++ )
        {
            for ( int elementIndex = 0; elementIndex < DefaultVertexFormats.BLOCK.getElements().size(); elementIndex++ )
            {
                final VertexFormatElement element = DefaultVertexFormats.BLOCK.getElements().get(elementIndex);
                switch ( element.getUsage() )
                {
                    case POSITION:
                        b.put( elementIndex, Float.intBitsToFloat( vertData[wrapAt * vertNum] ), Float.intBitsToFloat( vertData[1 + wrapAt * vertNum] ), Float.intBitsToFloat( vertData[2 + wrapAt * vertNum] ) );
                        break;

                    case COLOR:
                        final float light = LightUtil.diffuseLight( myFace );
                        b.put( elementIndex, light, light, light, 1f );
                        break;

                    case NORMAL:
                        b.put( elementIndex, myFace.getStepX(), myFace.getStepY(), myFace.getStepZ() );
                        break;

                    case UV:

                        if ( element.getIndex() == 1 )
                        {
                            b.put( elementIndex, 0, 0 );
                        }
                        else
                        {
                            final float u = Float.intBitsToFloat( vertData[4 + wrapAt * vertNum] );
                            final float v = Float.intBitsToFloat( vertData[5 + wrapAt * vertNum] );
                            b.put( elementIndex, u, v );
                        }

                        break;

                    default:
                        b.put( elementIndex );
                        break;
                }
            }
        }

        return b.build();
    }

    public List<BakedQuad>[] getFace()
    {
        return face;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      final BlockState state,
      final Direction side,
      @NotNull final Random rand )
    {
        if ( side == null )
        {
            return Collections.emptyList();
        }

        return face[side.ordinal()];
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        if ( side == null )
        {
            return Collections.emptyList();
        }

        return face[side.ordinal()];
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return true;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean usesBlockLight()
    {
        return false;
    }

    @NotNull
    @Override
    public ItemCameraTransforms getTransforms()
    {
        return ItemCameraTransforms.NO_TRANSFORMS;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return texture;
    }

    @Override
    public boolean isCustomRenderer()
    {
        return false;
    }

    @NotNull
    @Override
    public ItemOverrideList getOverrides()
    {
        return ItemOverrideList.EMPTY;
    }
}
