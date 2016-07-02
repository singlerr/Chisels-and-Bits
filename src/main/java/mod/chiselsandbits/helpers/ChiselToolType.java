package mod.chiselsandbits.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.PositivePatternMode;
import mod.chiselsandbits.modes.TapeMeasureModes;
import net.minecraft.item.ItemStack;

public enum ChiselToolType
{
	CHISEL( true, true ),
	BIT( true, false ),

	POSITIVEPATTERN( true, true ),
	TAPEMEASURE( true, true ),
	NEGATIVEPATTERN( false, false ),
	MIRRORPATTERN( false, false );

	final private boolean hasMenu;
	final private boolean hasItemSettings;

	private ChiselToolType(
			final boolean menu,
			final boolean itemSettings )
	{
		hasMenu = menu;
		hasItemSettings = itemSettings;
	}

	public IToolMode getMode(
			final ItemStack ei )
	{
		if ( this == CHISEL )
		{
			return ChiselMode.getMode( ei );
		}

		if ( this == POSITIVEPATTERN )
		{
			return PositivePatternMode.getMode( ei );
		}

		if ( this == ChiselToolType.TAPEMEASURE )
		{
			return TapeMeasureModes.getMode( ei );
		}

		throw new NullPointerException();
	}

	public boolean hasMenu()
	{
		return hasMenu;
	}

	@SuppressWarnings( "unchecked" )
	public List<IToolMode> getAvailableModes()
	{
		if ( isBitOrChisel() )
		{
			final List<IToolMode> modes = new ArrayList<IToolMode>();
			final EnumSet<ChiselMode> used = EnumSet.noneOf( ChiselMode.class );
			final ChiselMode[] orderedModes = { ChiselMode.SINGLE, ChiselMode.LINE, ChiselMode.PLANE, ChiselMode.CONNECTED_PLANE, ChiselMode.DRAWN_REGION };

			for ( final ChiselMode mode : orderedModes )
			{
				if ( !mode.isDisabled )
				{
					modes.add( mode );
					used.add( mode );
				}
			}

			for ( final ChiselMode mode : ChiselMode.values() )
			{
				if ( !mode.isDisabled && !used.contains( mode ) )
				{
					modes.add( mode );
				}
			}

			return modes;
		}
		else if ( this == POSITIVEPATTERN )
		{
			final PositivePatternMode[] modes = PositivePatternMode.values();
			final ArrayList<IToolMode> t = new ArrayList<IToolMode>( modes.length );

			for ( final PositivePatternMode b : modes )
			{
				t.add( b );
			}

			return t;
		}
		else if ( this == TAPEMEASURE )
		{
			final TapeMeasureModes[] modes = TapeMeasureModes.values();
			final ArrayList<IToolMode> t = new ArrayList<IToolMode>( modes.length );

			for ( final TapeMeasureModes b : modes )
			{
				t.add( b );
			}

			return t;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	public boolean isBitOrChisel()
	{
		return this == BIT || this == ChiselToolType.CHISEL;
	}

	public boolean hasPerToolSettings()
	{
		return hasItemSettings;
	}

	public boolean requiresPerToolSettings()
	{
		return this == ChiselToolType.POSITIVEPATTERN || this == ChiselToolType.TAPEMEASURE;
	}
}
