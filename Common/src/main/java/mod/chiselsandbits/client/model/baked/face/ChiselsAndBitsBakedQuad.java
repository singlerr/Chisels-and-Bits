package mod.chiselsandbits.client.model.baked.face;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.chiselsandbits.platforms.core.client.models.vertices.IVertexConsumer;
import mod.chiselsandbits.utils.LightUtil;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class ChiselsAndBitsBakedQuad extends BakedQuad
{

    public static final ConcurrentHashMap<VertexFormat, FormatInfo> formatData = new ConcurrentHashMap<>();

    private static int[] packData(
      float[][][] unpackedData)
    {
        FormatInfo fi = formatData.get(DefaultVertexFormat.BLOCK);

        if (fi == null)
        {
            fi = new FormatInfo(DefaultVertexFormat.BLOCK);
            formatData.put(DefaultVertexFormat.BLOCK, fi);
        }

        return fi.pack(unpackedData);
    }

    private static float[] getRawPart(
      int[] vertices,
      int v,
      int i)
    {
        return formatData.get(DefaultVertexFormat.BLOCK).unpack(vertices, v, i);
    }

    private static int[] buildProcessedVertexData(int[] vertices)
    {
        int[] packed = new int[DefaultVertexFormat.BLOCK.getIntegerSize() * 4];

        for (int v = 0; v < 4; v++)
        {
            for (int e = 0; e < DefaultVertexFormat.BLOCK.getElements().size(); e++)
            {
                LightUtil.pack(getRawPart(vertices, v, e), packed, DefaultVertexFormat.BLOCK, v, e);
            }
        }

        return packed;
    }

    public ChiselsAndBitsBakedQuad(
      final float[][][] unpackedData,
      final int tint,
      final Direction orientation,
      final TextureAtlasSprite sprite)
    {
        super(buildProcessedVertexData(packData(unpackedData)), tint, orientation, sprite, true);
    }

    public static class Builder implements IVertexConsumer, IFaceBuilder
    {
        private float[][][] unpackedData;
        private int         tint = -1;
        private Direction   orientation;

        private int vertices = 0;
        private int elements = 0;

        private final VertexFormat format;

        public Builder(
          VertexFormat format)
        {
            this.format = format;
        }

        @NotNull
        @Override
        public VertexFormat getVertexFormat()
        {
            return format;
        }

        @Override
        public void setQuadTint(
          final int tint)
        {
            this.tint = tint;
        }

        @Override
        public void setQuadOrientation(
          @NotNull final Direction orientation)
        {
            this.orientation = orientation;
        }

        @Override
        public void put(
          final int element,
          final float... data)
        {
            for (int i = 0; i < 4; i++)
            {
                if (i < data.length)
                {
                    unpackedData[vertices][element][i] = data[i];
                }
                else
                {
                    unpackedData[vertices][element][i] = 0;
                }
            }

            elements++;

            if (elements == getVertexFormat().getElements().size())
            {
                vertices++;
                elements = 0;
            }
        }

        @Override
        public void begin()
        {
            if (format != getVertexFormat())
            {
                throw new RuntimeException("Bad format, can only be CNB.");
            }

            unpackedData = new float[4][getVertexFormat().getElements().size()][4];
            tint = -1;
            orientation = null;

            vertices = 0;
            elements = 0;
        }

        @Override
        public BakedQuad create(
          final TextureAtlasSprite sprite)
        {
            return new ChiselsAndBitsBakedQuad(unpackedData, tint, orientation, sprite);
        }

        @Override
        public void setFace(
          final Direction myFace,
          final int tintIndex)
        {
            setQuadOrientation(myFace);
            setQuadTint(tintIndex);
        }

        @Override
        public void setApplyDiffuseLighting(
          final boolean diffuse)
        {
        }

        @Override
        public void setTexture(
          @NotNull final TextureAtlasSprite texture)
        {
        }

        @Override
        public VertexFormat getFormat()
        {
            return format;
        }
    }
}
