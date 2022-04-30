package mod.chiselsandbits.client.model.baked.face.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import mod.chiselsandbits.platforms.core.client.models.vertices.IVertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public abstract class BaseModelReader implements IVertexConsumer
{
    @NotNull
    @Override
    public VertexFormat getVertexFormat()
    {
        return DefaultVertexFormat.BLOCK;
    }

    @Override
    public void setQuadTint(
      final int tint)
    {
    }

    @Override
    public void setQuadOrientation(
      @NotNull final Direction orientation)
    {
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
}