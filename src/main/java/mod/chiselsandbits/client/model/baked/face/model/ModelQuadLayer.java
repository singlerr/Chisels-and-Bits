package mod.chiselsandbits.client.model.baked.face.model;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ModelQuadLayer
{

    private float[]            uvs;
    private TextureAtlasSprite sprite;
    private int light;
    private int color;
    private int tint;

    public float[] getUvs()
    {
        return uvs;
    }

    public void setUvs(final float[] uvs)
    {
        this.uvs = uvs;
    }

    public TextureAtlasSprite getSprite()
    {
        return sprite;
    }

    public void setSprite(final TextureAtlasSprite sprite)
    {
        this.sprite = sprite;
    }

    public int getLight()
    {
        return light;
    }

    public void setLight(final int light)
    {
        this.light = light;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(final int color)
    {
        this.color = color;
    }

    public int getTint()
    {
        return tint;
    }

    public void setTint(final int tint)
    {
        this.tint = tint;
    }

    public static class ModelQuadLayerBuilder
    {
        public final ModelQuadLayer cache = new ModelQuadLayer();
        public final ModelLightMapReader lv;
        public ModelUVReader uvr;

        public ModelQuadLayerBuilder(
          final TextureAtlasSprite sprite,
          final int uCoord,
          final int vCoord )
        {
            cache.sprite = sprite;
            lv = new ModelLightMapReader();
            uvr = new ModelUVReader( sprite, uCoord, vCoord );
        }

        public ModelQuadLayer build(
          final BlockState state,
          final int color,
          final int lightValue )
        {
            cache.light = Math.max( lightValue, lv.getLv() );
            cache.uvs = uvr.getQuadUVs();
            cache.color = cache.tint != -1 ? color : 0xffffffff;

            if ( 0x00 <= cache.tint && cache.tint <= 0xff )
            {
                cache.color = 0xffffffff;
                cache.tint = ( IBlockStateIdManager.getInstance().getIdFrom(state) << 8 ) | cache.tint;
            }
            else
            {
                cache.tint = -1;
            }

            return cache;
        }
    };

}