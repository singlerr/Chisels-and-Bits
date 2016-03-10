package mod.chiselsandbits.render.helpers;

import java.util.Arrays;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class ModelQuadShare extends BaseModelReader
{

	int[][] pos_uv = new int[4][6];
	TextureAtlasSprite sprite;
	String texture;

	EnumFacing face;

	public ModelQuadShare(
			final String textureName,
			final TextureAtlasSprite texture,
			final EnumFacing face,
			final EnumFacing cull )
	{
		sprite = texture;
		this.texture = textureName;
		this.face = face;
	}

	float[] pos;
	float[] uv;
	float[] color;
	int index = 0;

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
			pos_uv[index][0] = Math.round( pos[0] * 16 );
			pos_uv[index][1] = Math.round( pos[1] * 16 );
			pos_uv[index][2] = Math.round( pos[2] * 16 );
			pos_uv[index][3] = Math.round( ( uv[0] - sprite.getMinU() ) / ( sprite.getMaxU() - sprite.getMinU() ) * 16 );
			pos_uv[index][4] = Math.round( ( uv[1] - sprite.getMinV() ) / ( sprite.getMaxV() - sprite.getMinV() ) * 16 );
			pos_uv[index][5] = color == null ? 0xffffffff : MathHelper.clamp_int( (int) ( color[0] * 0xff ), 0, 0xff ) << 16 |
					MathHelper.clamp_int( (int) ( color[1] * 0xff ), 0, 0xff ) << 8 |
					MathHelper.clamp_int( (int) ( color[2] * 0xff ), 0, 0xff ) |
					MathHelper.clamp_int( (int) ( color[3] * 0xff ), 0, 0xff ) << 24;

			index++;
		}
	}

	@Override
	public String toString()
	{
		final StringBuilder o = new StringBuilder();

		o.append( "{\"t\":\"" );
		o.append( texture );
		o.append( "\",\"v\":" );

		o.append( '[' );
		for ( int x = 0; x < index; x++ )
		{
			if ( x > 0 )
			{
				o.append( ',' );
			}

			o.append( '"' );
			o.append( asHex( pos_uv[x][0] ) );
			o.append( ' ' );
			o.append( asHex( pos_uv[x][1] ) );
			o.append( ' ' );
			o.append( asHex( pos_uv[x][2] ) );
			o.append( ' ' );
			o.append( asHex( pos_uv[x][3] ) );
			o.append( ' ' );
			o.append( asHex( pos_uv[x][4] ) );
			o.append( ' ' );
			o.append( asHex( pos_uv[x][5] ) );
			o.append( '"' );
		}

		o.append( "]}" );
		return o.toString();
	}

	private String asHex(
			final int i )
	{
		return Integer.toHexString( i );
	}

}