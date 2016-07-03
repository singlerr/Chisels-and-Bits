package mod.chiselsandbits.modes;

import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

public enum PositivePatternMode implements IToolMode
{
	REPLACE( LocalStrings.PositivePatternReplace ),
	ADDITIVE( LocalStrings.PositivePatternAdditive ),
	PLACEMENT( LocalStrings.PositivePatternPlacement ),
	IMPOSE( LocalStrings.PositivePatternImpose );

	public final LocalStrings string;
	public boolean isDisabled = false;

	public Object binding;

	private PositivePatternMode(
			final LocalStrings str )
	{
		string = str;
	}

	public static PositivePatternMode getMode(
			final ItemStack stack )
	{
		if ( stack != null )
		{
			try
			{
				final NBTTagCompound nbt = stack.getTagCompound();
				if ( nbt != null && nbt.hasKey( "mode" ) )
				{
					return valueOf( nbt.getString( "mode" ) );
				}
			}
			catch ( final IllegalArgumentException iae )
			{
				// nope!
			}
			catch ( final Exception e )
			{
				Log.logError( "Unable to determine mode.", e );
			}
		}

		return REPLACE;
	}

	@Override
	public void setMode(
			final ItemStack stack )
	{
		if ( stack != null )
		{
			stack.setTagInfo( "mode", new NBTTagString( name() ) );
		}
	}

	public static PositivePatternMode castMode(
			final IToolMode chiselMode )
	{
		if ( chiselMode instanceof PositivePatternMode )
		{
			return (PositivePatternMode) chiselMode;
		}

		return PositivePatternMode.REPLACE;
	}

	@Override
	public LocalStrings getName()
	{
		return string;
	}

	@Override
	public boolean isDisabled()
	{
		return isDisabled;
	}
}
