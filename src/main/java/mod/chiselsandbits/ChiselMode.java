
package mod.chiselsandbits;

import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

public enum ChiselMode
{
	SINGLE( LocalStrings.ChiselModeSingle ),
	LINE( LocalStrings.ChiselModeLine ),
	PLANE( LocalStrings.ChiselModePlane ),
	CONNECTED_PLANE( LocalStrings.ChiselModeConnectedPlane ),
	CUBE_SMALL( LocalStrings.ChiselModeCubeSmall ),
	CUBE_LARGE( LocalStrings.ChiselModeCubeLarge ),
	CUBE_HUGE( LocalStrings.ChiselModeCubeHuge ),
	DRAWN_REGION( LocalStrings.ChiselModeDrawnRegion );

	public final LocalStrings string;

	public boolean isDisabled = false;

	public Object binding;

	private ChiselMode(
			final LocalStrings str )
	{
		string = str;
	}

	public static ChiselMode getMode(
			final ItemStack is )
	{
		if ( is != null )
		{
			try
			{
				final NBTTagCompound nbt = is.getTagCompound();
				if ( nbt != null && nbt.hasKey( "mode" ) )
				{
					return valueOf( nbt.getString( "mode" ) );
				}
			}
			catch ( final Exception e )
			{
				// well whatever just use the default..
			}
		}

		return SINGLE;
	}

	public void setMode(
			final ItemStack is )
	{
		if ( is != null )
		{
			is.setTagInfo( "mode", new NBTTagString( name() ) );
		}
	}

	public static ChiselMode getMode(
			final int offset )
	{
		return values()[offset % values().length];
	}

}
