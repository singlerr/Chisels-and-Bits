package mod.chiselsandbits.client.model.baked.face;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class ChiselsAndBitsBakedQuad extends BakedQuad
{

    public static final ConcurrentHashMap<VertexFormat, FormatInfo> formatData = new ConcurrentHashMap<>();

    private final int[] processedVertexData;

    private static int[] packData(
      float[][][] unpackedData)
    {
        FormatInfo fi = formatData.get(DefaultVertexFormats.BLOCK);

        if (fi == null)
        {
            fi = new FormatInfo(DefaultVertexFormats.BLOCK);
            formatData.put(DefaultVertexFormats.BLOCK, fi);
        }

        return fi.pack(unpackedData);
    }

    @Override
    public void pipe(
      final IVertexConsumer consumer)
    {
        final int[] eMap = LightUtil.mapFormats(consumer.getVertexFormat(), DefaultVertexFormats.BLOCK);

        consumer.setTexture(sprite);
        consumer.setQuadTint(getTintIndex());
        consumer.setQuadOrientation(getFace());
        consumer.setApplyDiffuseLighting(true);

        for (int v = 0; v < 4; v++)
        {
            for (int e = 0; e < consumer.getVertexFormat().getElements().size(); e++)
            {
                if (eMap[e] != consumer.getVertexFormat().getElements().size())
                {
                    consumer.put(e, getRawPart(v, eMap[e]));
                }
                else
                {
                    consumer.put(e);
                }
            }
        }
    }

    private float[] getRawPart(
      int v,
      int i)
    {
        return formatData.get(DefaultVertexFormats.BLOCK).unpack(vertexData, v, i);
    }

    @NotNull
    @Override
    public int[] getVertexData()
    {
        return this.processedVertexData;
    }

    private int[] buildProcessedVertexData()
    {
        int[] packed = new int[DefaultVertexFormats.BLOCK.getIntegerSize() * 4];

        for (int v = 0; v < 4; v++)
        {
            for (int e = 0; e < DefaultVertexFormats.BLOCK.getElements().size(); e++)
            {
                LightUtil.pack(getRawPart(v, e), packed, DefaultVertexFormats.BLOCK, v, e);
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
        super(packData(unpackedData), tint, orientation, sprite, true);
        processedVertexData = buildProcessedVertexData();
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
          @NotNull final float... data)
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
