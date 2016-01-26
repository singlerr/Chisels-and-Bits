package mod.chiselsandbits.render.helpers;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ModelParserCache
{

	public float[] uvs = new float[6];
	public TextureAtlasSprite sprite;
	public int light;
	public int color;
	public int tint;

	public static class ModelParserCacheBuilder
	{
		public final ModelParserCache cache = new ModelParserCache();
		public final ModelLightMapReader lv = new ModelLightMapReader( 0 );
		public ModelUVReader uvr;

		public ModelParserCacheBuilder(
				final TextureAtlasSprite sprite,
				final int uCoord,
				final int vCoord )
		{
			cache.sprite = sprite;
			uvr = new ModelUVReader( sprite, uCoord, vCoord );
		}

		public ModelParserCache build(
				final int color )
		{
			cache.light = lv.lv;
			cache.uvs = uvr.quadUVs;
			cache.color = color;
			return cache;
		}
	};

}
