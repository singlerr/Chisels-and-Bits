package mod.chiselsandbits.client.model.baked.face.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ModelQuadReader extends BaseModelReader
{

    private int minX = 16;
    private int minY = 16;
    private int minZ = 16;
    private int maxX = 0;
    private int maxY = 0;
    private int maxZ = 0;

    private int u;
    private int v;

    private final int[][]            pos_uv = new int[4][5];
    private final TextureAtlasSprite sprite;
    private final String             texture;

    private final Direction face;
    private final Direction cull;

    public ModelQuadReader(
      final String textureName,
      final TextureAtlasSprite texture,
      final Direction face,
      final Direction cull )
    {
        sprite = texture;
        this.texture = textureName;
        this.face = face;
        this.cull = cull;
    }

    float[] pos;
    float[] uv;
    int index = 0;

    @Override
    public void put(
      final int vertNum,
      final int element,
      @NotNull final float... data )
    {
        final VertexFormat format = getVertexFormat();
        final VertexFormatElement ele = format.getElements().get(element);

        if ( ele.getUsage() == VertexFormatElement.Usage.UV && ele.getIndex() != 1 )
        {
            uv = Arrays.copyOf( data, data.length );
        }

        else if ( ele.getUsage() == VertexFormatElement.Usage.POSITION )
        {
            pos = Arrays.copyOf( data, data.length );
        }

        if ( element == format.getElements().size() - 1 )
        {
            pos_uv[index][0] = Math.round( pos[0] * 16 );
            pos_uv[index][1] = Math.round( pos[1] * 16 );
            pos_uv[index][2] = Math.round( pos[2] * 16 );
            pos_uv[index][3] = Math.round( ( uv[0] - sprite.getU0() ) / ( sprite.getU1() - sprite.getU0() ) * 16 );
            pos_uv[index][4] = Math.round( ( uv[1] - sprite.getV0() ) / ( sprite.getV1() - sprite.getV0() ) * 16 );

            minX = Math.min( minX, pos_uv[index][0] );
            minY = Math.min( minY, pos_uv[index][1] );
            minZ = Math.min( minZ, pos_uv[index][2] );
            maxX = Math.max( maxX, pos_uv[index][0] );
            maxY = Math.max( maxY, pos_uv[index][1] );
            maxZ = Math.max( maxZ, pos_uv[index][2] );

            index++;
        }
    }

    public String toString(
      Direction faceQuad )
    {
        int U1 = 0, V1 = 16, U2 = 16, V2 = 0;

        for ( int idx = 0; idx < 4; idx++ )
        {
            if ( matches( minX, minY, minZ, pos_uv[idx] ) )
            {
                U1 = pos_uv[idx][3];
                V2 = pos_uv[idx][4];
            }
            else if ( matches( maxX, maxY, maxZ, pos_uv[idx] ) )
            {
                U2 = pos_uv[idx][3];
                V1 = pos_uv[idx][4];
            }
        }

        if ( faceQuad.get2DDataValue() > 1 )
        {
            final int tempU = U1;
            U1 = U2;
            U2 = tempU;
        }
        else if ( faceQuad == Direction.UP )
        {
            final int tempV = V1;
            V1 = V2;
            V2 = tempV;
        }

        if ( cull == null )
        {
            return String.format("{ \"from\": [%d,%d,%d], \"to\": [%d,%d,%d], \"faces\": { \"%s\":  { \"uv\": [%d,%d,%d,%d], \"texture\": \"%s\" } } },\n",
              minX,
              minY,
              minZ,
              maxX,
              maxY,
              maxZ,
              face.getSerializedName(),
              U1,
              V1,
              U2,
              V2,
              texture);
        }
        else
        {
            return String.format("{ \"from\": [%d,%d,%d], \"to\": [%d,%d,%d], \"faces\": { \"%s\":  { \"uv\": [%d,%d,%d,%d], \"texture\": \"%s\", \"cullface\": \"%s\" } } },\n",
              minX,
              minY,
              minZ,
              maxX,
              maxY,
              maxZ,
              face.getSerializedName(),
              U1,
              V1,
              U2,
              V2,
              texture,
              cull.getSerializedName());
        }
    }

    private boolean matches(
      final int x,
      final int y,
      final int z,
      final int[] v )
    {
        return v[0] == x && v[1] == y && v[2] == z;
    }
}