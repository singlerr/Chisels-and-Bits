package mod.chiselsandbits.render.helpers;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class ModelQuadShare extends BaseModelReader
{

	public static class ShareFaces
	{
		public int x, y, z; // varints.
		public int u, v; // 5 bits per
	};

	int[][] pos_uv = new int[4][5];
	TextureAtlasSprite sprite;
	String texture;
	BlockPos offset;

	public int col;
	EnumFacing face;

	ArrayList<ShareFaces> faces = new ArrayList<ShareFaces>();

	public ModelQuadShare(
			final String textureName,
			final BlockPos offset,
			final TextureAtlasSprite texture,
			final EnumFacing face,
			final EnumFacing cull,
			final ItemStack stack )
	{
		sprite = texture;
		this.texture = textureName;
		this.face = face;
		this.offset = offset;
		colorLookup = stack;
	}

	float[] pos;
	float[] uv;
	float[] color;
	int index = 0;

	ItemStack colorLookup;
	int tint = -1;

	@Override
	public void setQuadTint(
			final int tint )
	{
		this.tint = tint;
	}

	@Override
	public void put(
			final int element,
			final float... data )
	{
		final VertexFormat format = getVertexFormat();
		final VertexFormatElement ele = format.getElement( element );

		if ( ele.getUsage() == EnumUsage.UV && ele.getIndex() != 1 )
		{
			uv = Arrays.copyOf( data, data.length );
		}

		else if ( ele.getUsage() == EnumUsage.POSITION )
		{
			pos = Arrays.copyOf( data, data.length );
		}
		else if ( ele.getUsage() == EnumUsage.COLOR )
		{
			color = Arrays.copyOf( data, data.length );
		}

		if ( element == format.getElementCount() - 1 )
		{
			int fillColor = -1;

			if ( tint != -1 && colorLookup != null )
			{
				fillColor = colorLookup.getItem().getColorFromItemStack( colorLookup, tint );
			}

			pos_uv[index][0] = 16 * offset.getX() + Math.round( pos[0] * 16 );
			pos_uv[index][1] = 16 * offset.getY() + Math.round( pos[1] * 16 );
			pos_uv[index][2] = 16 * offset.getZ() + Math.round( pos[2] * 16 );
			pos_uv[index][3] = Math.round( ( uv[0] - sprite.getMinU() ) / ( sprite.getMaxU() - sprite.getMinU() ) * 16 );
			pos_uv[index][4] = 16 - Math.round( ( uv[1] - sprite.getMinV() ) / ( sprite.getMaxV() - sprite.getMinV() ) * 16 );
			col = fillColor != -1 ? fillColor : color == null ? 0xffffffff : MathHelper.clamp_int( (int) ( color[0] * 0xff ), 0, 0xff ) << 16 |
					MathHelper.clamp_int( (int) ( color[1] * 0xff ), 0, 0xff ) << 8 |
					MathHelper.clamp_int( (int) ( color[2] * 0xff ), 0, 0xff ) |
					MathHelper.clamp_int( (int) ( color[3] * 0xff ), 0, 0xff ) << 24;

			index++;
		}
	}

	public void getFaces(
			final ArrayList<ShareFaces> faces )
	{
		for ( int x = 0; x < index; x++ )
		{
			final ShareFaces f = new ShareFaces();

			f.x = pos_uv[x][0];
			f.y = pos_uv[x][1];
			f.z = pos_uv[x][2];
			f.u = pos_uv[x][3];
			f.v = pos_uv[x][4];

			faces.add( f );
		}
	}

}