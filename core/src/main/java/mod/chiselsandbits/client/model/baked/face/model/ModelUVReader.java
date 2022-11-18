package mod.chiselsandbits.client.model.baked.face.model;

import mod.chiselsandbits.utils.ModelUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelUVReader extends BaseModelReader {

    private final float minU;
    private final float maxUMinusMin;

    private final float minV;
    private final float maxVMinusMin;

    private final float[] quadUVs = new float[]{0, 0, 0, 1, 1, 0, 1, 1};

    private record PositionUVAndCorner(float[] position, float[] uv, int corner) {
    }

    private final List<PositionUVAndCorner> positionAndUVS = new ArrayList<>();

    private final int uCoord;
    private final int vCoord;
    private final Direction face;

    public ModelUVReader(
            final TextureAtlasSprite texture,
            final int uFaceCoord,
            final int vFaceCoord,
            final Direction face) {
        minU = texture.getU0();
        maxUMinusMin = texture.getU1() - minU;

        minV = texture.getV0();
        maxVMinusMin = texture.getV1() - minV;

        uCoord = uFaceCoord;
        vCoord = vFaceCoord;
        this.face = face;
    }

    private float[] pos;
    private float[] uv;
    private int corners;

    @Override
    public void put(
            final int vertexIndex,
            final int element,
            final float @NotNull ... data) {
        final VertexFormat format = getVertexFormat();
        final VertexFormatElement ele = format.getElements().get(element);

        if (ele.getUsage() == VertexFormatElement.Usage.UV && ele.getIndex() == 0) {
            uv = Arrays.copyOf(data, data.length);
        } else if (ele.getUsage() == VertexFormatElement.Usage.POSITION) {
            pos = Arrays.copyOf(data, data.length);
        }

        if (element == format.getElements().size() - 1) {
            if (ModelUtil.isZero(pos[uCoord]) && ModelUtil.isZero(pos[vCoord])) {
                corners = corners | 0x1;
                quadUVs[0] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[1] = (uv[1] - minV) / maxVMinusMin;
                positionAndUVS.add(new PositionUVAndCorner(pos, new float[]{quadUVs[0], quadUVs[1]}, 0x1));
            } else if (ModelUtil.isZero(pos[uCoord]) && ModelUtil.isOne(pos[vCoord])) {
                corners = corners | 0x2;
                quadUVs[4] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[5] = (uv[1] - minV) / maxVMinusMin;
                positionAndUVS.add(new PositionUVAndCorner(pos, new float[]{quadUVs[4], quadUVs[5]}, 0x2));
            } else if (ModelUtil.isOne(pos[uCoord]) && ModelUtil.isZero(pos[vCoord])) {
                corners = corners | 0x4;
                quadUVs[2] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[3] = (uv[1] - minV) / maxVMinusMin;
                positionAndUVS.add(new PositionUVAndCorner(pos, new float[]{quadUVs[2], quadUVs[3]}, 0x4));
            } else if (ModelUtil.isOne(pos[uCoord]) && ModelUtil.isOne(pos[vCoord])) {
                corners = corners | 0x8;
                quadUVs[6] = (uv[0] - minU) / maxUMinusMin;
                quadUVs[7] = (uv[1] - minV) / maxVMinusMin;
                positionAndUVS.add(new PositionUVAndCorner(pos, new float[]{quadUVs[6], quadUVs[7]}, 0x8));
            }
        }
    }

    public float[] getQuadUVs() {
        return quadUVs;
    }

    @Override
    public void onComplete() {
        /*
        (0,0) ------------------ (1,0)
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
        (0,1) ------------------ (1,1)

        1) Lower left
        2) Upper left
        3) Lower right
        4) Upper right
        */

        //target - [0.002948761, 0.99705124, 0.002948761, 0.002948761, 0.99705124, 0.99705124, 0.99705124, 0.002948761]
        //down - [0.002948761, 0.99705124, 0.99705124, 0.99705124, 0.002948761, 0.002948761, 0.99705124, 0.002948761]
        //up - [0.002948761, 0.002948761, 0.99705124, 0.002948761, 0.002948761, 0.99705124, 0.99705124, 0.99705124]
        //north - [0.99705124, 0.99705124, 0.002948761, 0.99705124, 0.99705124, 0.002948761, 0.002948761, 0.002948761]
        //south - [0.002948761, 0.99705124, 0.99705124, 0.99705124, 0.002948761, 0.002948761, 0.99705124, 0.002948761]
        //west - [0.002948761, 0.99705124, 0.002948761, 0.002948761, 0.99705124, 0.99705124, 0.99705124, 0.002948761]
        //east - [0.99705124, 0.99705124, 0.99705124, 0.002948761, 0.002948761, 0.99705124, 0.002948761, 0.002948761]

        final int[][] selectorIndexes = new int[][]{
                new int[]{0, 1, 4, 5, 2, 3, 6, 7},
                new int[]{4, 5, 0, 1, 6, 7, 2, 3},
                new int[]{2, 3, 6, 7, 0, 1, 4, 5},
                new int[]{0, 1, 4, 5, 2, 3, 6, 7},
                new int[]{0, 1, 2, 3, 4, 5, 6, 7},
                new int[]{4, 5, 6, 7, 0, 1, 2, 3},
        };

        final float[] data = Arrays.copyOf(quadUVs, 8);
        for (int i = 0; i < 8; i++) {
            final int index = selectorIndexes[face.get3DDataValue()][i];
            //quadUVs[i] = data[index];
        }
    }
}