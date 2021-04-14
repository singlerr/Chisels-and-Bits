package mod.chiselsandbits.client.model.baked.face;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;

public interface IFaceBuilder
{
    void setFace(
      Direction myFace,
      int tintIndex );

    void put(
      int element,
      float... args );

    void begin();

    BakedQuad create(
      TextureAtlasSprite sprite );

    VertexFormat getFormat();
}
