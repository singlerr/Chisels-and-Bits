package mod.chiselsandbits.render.helpers;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ModelQuadLayer
{

	public float[] uvs = new float[6];
	public TextureAtlasSprite sprite;
	public int light;
	public int color;
	public int tint;

	public static class ModelQuadLayerBuilder
	{
		public final ModelQuadLayer cache = new ModelQuadLayer();
		public final ModelLightMapReader lv = new ModelLightMapReader( 0 );
		public ModelUVReader uvr;

		public ModelQuadLayerBuilder(
				final TextureAtlasSprite sprite,
				final int uCoord,
				final int vCoord )
		{
			cache.sprite = sprite;
			uvr = new ModelUVReader( sprite, uCoord, vCoord );
		}

		public ModelQuadLayer build(
				final int color )
		{
			cache.light = lv.lv;
			cache.uvs = uvr.quadUVs;
			cache.color = color;
			return cache;
		}
	};

}
