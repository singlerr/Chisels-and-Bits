package mod.chiselsandbits.client.model.baked.face;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.core.Direction;

import static net.minecraft.core.Direction.*;
import static net.minecraft.core.Direction.EAST;

public class FormatInfo
{

    private static final int[][] VERTEX_ORDER_MAP = new int[6][4];
    static {
        VERTEX_ORDER_MAP[DOWN.get3DDataValue()] = new int[] { 0, 1, 2, 3 };
        VERTEX_ORDER_MAP[UP.get3DDataValue()] = new int[] { 2, 3, 0, 1 };
        VERTEX_ORDER_MAP[NORTH.get3DDataValue()] = new int[] { 3, 0, 1, 2 };
        VERTEX_ORDER_MAP[SOUTH.get3DDataValue()] = new int[] { 0, 1, 2, 3 };
        VERTEX_ORDER_MAP[WEST.get3DDataValue()] = new int[] { 3, 0, 1, 2 };
        VERTEX_ORDER_MAP[EAST.get3DDataValue()] = new int[] { 1, 2, 3, 0 };
    }

    final int totalSize;
    final int faceSize;

    final int[] offsets;
    final int[] indexLengths;
    final int[] finalLengths;

    public FormatInfo(
      final VertexFormat format )
    {
        int total = 0;
        indexLengths = new int[format.getElements().size()];
        finalLengths = new int[format.getElements().size()];
        offsets = new int[format.getElements().size()];

        for ( int x = 0; x < indexLengths.length; ++x )
        {
            finalLengths[x] = format.getElements().get( x ).getCount();
            indexLengths[x] = finalLengths[x];

            switch (format.getElements().get(x).getUsage()) {
                case GENERIC, PADDING -> indexLengths[x] = 0;
                case COLOR -> indexLengths[x] = 4;
                case NORMAL, POSITION -> indexLengths[x] = 3;
                case UV -> indexLengths[x] = 2;
            }

            offsets[x] = total;
            total += indexLengths[x];
        }

        this.totalSize = total;
        this.faceSize = total * 4;
    }

    public int[] pack(
      float[][][] unpackedData, final Direction orientation)
    {
        int[] out = new int[this.faceSize];

        final int[] orderedFaceIndexes = VERTEX_ORDER_MAP[orientation.get3DDataValue()];

        int offset = 0;
        for ( int faceIndex = 0; faceIndex < 4; ++faceIndex )
        {
            final int orderedFaceIndex = orderedFaceIndexes[faceIndex];
            float[][] unpackedFaceData = unpackedData[orderedFaceIndex];
            for ( int x = 0; x < indexLengths.length; ++x )
            {
                float[] run = unpackedFaceData[x];
                for ( int z = 0; z < indexLengths[x]; z++ )
                {
                    if ( run.length > z )
                        out[offset++] = Float.floatToRawIntBits( run[z] );
                    else
                        out[offset++] = 0;
                }
            }
        }

        return out;
    }

    public float[] unpack(
      int[] raw,
      int vertex,
      int index )
    {
        int size = indexLengths[index];
        float[] out = new float[size];
        int start = vertex * this.totalSize + offsets[index];

        for ( int x = 0; x < size; x++ )
        {
            out[x] = Float.intBitsToFloat( raw[start + x] );
        }

        return out;
    }
}
