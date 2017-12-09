package mod.chiselsandbits.modes;

import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.localization.LocalStrings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

public enum WrenchModes implements IToolMode
{
	NUDGE_BIT( LocalStrings.WrenchNudgeBit ),
	NUDGE_BLOCK( LocalStrings.WrenchNudgeBlock ),
	ROTATE( LocalStrings.WrenchRotateBlock );

	public final LocalStrings string;
	public boolean isDisabled = false;

	public Object binding;

	private WrenchModes(
			final LocalStrings str )
	{
		string = str;
	}

	public static WrenchModes getMode(
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

		return WrenchModes.ROTATE;
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

	public static WrenchModes castMode(
			final IToolMode mode )
	{
		if ( mode instanceof WrenchModes )
		{
			return (WrenchModes) mode;
		}

		return WrenchModes.ROTATE;
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
