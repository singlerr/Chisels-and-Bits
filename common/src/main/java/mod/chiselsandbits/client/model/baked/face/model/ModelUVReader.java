package mod.chiselsandbits.client.model.baked.face.model;

import mod.chiselsandbits.utils.ModelUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ModelUVReader extends BaseModelReader
{

    private final float minU;
    private final float maxUMinusMin;

    private final float minV;
    private final float maxVMinusMin;

    private final float[] quadUVs = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 };

    private final int uCoord;
    private final int vCoord;

    public ModelUVReader(
      final TextureAtlasSprite texture,
      final int uFaceCoord,
      final int vFaceCoord )
    {
        minU = texture.getU0();
        maxUMinusMin = texture.getU1() - minU;

        minV = texture.getV0();
        maxVMinusMin = texture.getV1() - minV;

        uCoord = uFaceCoord;
        vCoord = vFaceCoord;
    }

    private float[] pos;
    private float[] uv;
    private int     corners;

    @Override
    public void put(
      final int element,
      @NotNull final float... data )
    {
        final VertexFormat format = getVertexFormat();
        final VertexFormatElement ele = format.getElements().get(element);

        if ( ele.getUsage() == VertexFormatElement.Usage.UV && ele.getIndex() == 0)
        {
            uv = Arrays.copyOf( data, data.length );
        }

        else if ( ele.getUsage() == VertexFormatElement.Usage.POSITION )
        {
            pos = Arrays.copyOf( data, data.length );
        }

        if ( element == format.getElements().size() - 1 )
        {
            if ( ModelUtil.isZero( pos[uCoord] ) && ModelUtil.isZero( pos[vCoord] ) )
            {
                corners = corners | 0x1;
                quadUVs[0] = ( uv[0] - minU ) / maxUMinusMin;
                quadUVs[1] = ( uv[1] - minV ) / maxVMinusMin;
            }
            else if ( ModelUtil.isZero( pos[uCoord] ) && ModelUtil.isOne( pos[vCoord] ) )
            {
                corners = corners | 0x2;
                quadUVs[4] = ( uv[0] - minU ) / maxUMinusMin;
                quadUVs[5] = ( uv[1] - minV ) / maxVMinusMin;
            }
            else if ( ModelUtil.isOne( pos[uCoord] ) && ModelUtil.isZero( pos[vCoord] ) )
            {
                corners = corners | 0x4;
                quadUVs[2] = ( uv[0] - minU ) / maxUMinusMin;
                quadUVs[3] = ( uv[1] - minV ) / maxVMinusMin;
            }
            else if ( ModelUtil.isOne( pos[uCoord] ) && ModelUtil.isOne( pos[vCoord] ) )
            {
                corners = corners | 0x8;
                quadUVs[6] = ( uv[0] - minU ) / maxUMinusMin;
                quadUVs[7] = ( uv[1] - minV ) / maxVMinusMin;
            }
        }
    }

    public float[] getQuadUVs()
    {
        return quadUVs;
    }
}