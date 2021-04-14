package mod.chiselsandbits.client.model.baked.bit;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.model.baked.base.BaseBakedBlockModel;
import mod.chiselsandbits.client.model.baked.face.FaceManager;
import mod.chiselsandbits.client.model.baked.face.model.ModelQuadLayer;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BitBlockBakedModel extends BaseBakedBlockModel
{
    public static final float PIXELS_PER_BLOCK = 16.0f;

    private static final float BIT_BEGIN = 6.0f;
    private static final float BIT_END = 10.0f;

    final List<BakedQuad> generic = new ArrayList<BakedQuad>( 6 );

    public BitBlockBakedModel(
      final BlockState blockState )
    {
        final FaceBakery faceBakery = new FaceBakery();

        final Vector3f to = new Vector3f( BIT_BEGIN, BIT_BEGIN, BIT_BEGIN );
        final Vector3f from = new Vector3f( BIT_END, BIT_END, BIT_END );

        final ModelRotation mr = ModelRotation.X0_Y0;

        for ( final Direction myFace : Direction.values() )
        {
            for ( final RenderType layer : RenderType.getBlockRenderTypes() )
            {
                final ModelQuadLayer[] layers = FaceManager.getInstance().getCachedFace( blockState, myFace, layer );

                if ( layers == null || layers.length == 0 )
                {
                    continue;
                }

                for ( final ModelQuadLayer quadLayer : layers )
                {
                    final BlockFaceUV uv = new BlockFaceUV( getFaceUvs( myFace ), 0 );
                    final BlockPartFace bpf = new BlockPartFace( myFace, 0, "", uv );

                    Vector3f toB, fromB;

                    switch ( myFace )
                    {
                        case UP:
                            toB = new Vector3f( to.getX(), from.getY(), to.getZ() );
                            fromB = new Vector3f( from.getX(), from.getY(), from.getZ() );
                            break;
                        case EAST:
                            toB = new Vector3f( from.getX(), to.getY(), to.getZ() );
                            fromB = new Vector3f( from.getX(), from.getY(), from.getZ() );
                            break;
                        case NORTH:
                            toB = new Vector3f( to.getX(), to.getY(), to.getZ() );
                            fromB = new Vector3f( from.getX(), from.getY(), to.getZ() );
                            break;
                        case SOUTH:
                            toB = new Vector3f( to.getX(), to.getY(), from.getZ() );
                            fromB = new Vector3f( from.getX(), from.getY(), from.getZ() );
                            break;
                        case DOWN:
                            toB = new Vector3f( to.getX(), to.getY(), to.getZ() );
                            fromB = new Vector3f( from.getX(), to.getY(), from.getZ() );
                            break;
                        case WEST:
                            toB = new Vector3f( to.getX(), to.getY(), to.getZ() );
                            fromB = new Vector3f( to.getX(), from.getY(), from.getZ() );
                            break;
                        default:
                            throw new NullPointerException();
                    }

                    generic.add( faceBakery.bakeQuad( toB, fromB, bpf, quadLayer.getSprite(), myFace, mr, null, false, new ResourceLocation(Constants.MOD_ID, "bit")));
                }
            }
        }
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

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand)
    {
        if ( side != null )
        {
            return Collections.emptyList();
        }

        return generic;
    }

    @Override
    public boolean isSideLit()
    {
        return true;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
    }

}
