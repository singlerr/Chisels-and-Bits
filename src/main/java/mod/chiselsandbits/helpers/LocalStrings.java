package mod.chiselsandbits.helpers;

import net.minecraft.util.StatCollector;

public enum LocalStrings
{

	ChiselModeSingle( "chiselmode.single" ),
	ChiselModeSnap2( "chiselmode.snap2" ),
	ChiselModeSnap4( "chiselmode.snap4" ),
	ChiselModeSnap8( "chiselmode.snap8" ),
	ChiselModeLine( "chiselmode.line" ),
	ChiselModePlane( "chiselmode.plane" ),
	ChiselModeConnectedPlane( "chiselmode.connected_plane" ),
	ChiselModeCubeSmall( "chiselmode.cube_small" ),
	ChiselModeCubeLarge( "chiselmode.cube_large" ),
	ChiselModeCubeHuge( "chiselmode.cube_huge" ),
	ChiselModeDrawnRegion( "chiselmode.drawn_region" ),

	ShiftDetails( "help.shiftdetails" ),
	Empty( "help.empty" ),

	HelpChiseledBlock( "help.chiseled_block" ),
	HelpBitBag( "help.bit_bag" ),
	HelpWrench( "help.wrench" ),
	HelpBit( "help.bit" ),
	HelpPositivePrint( "help.positiveprint" ),
	HelpNegativePrint( "help.negativeprint" ),
	HelpChisel( "help.chisel" );

	private final String string;

	private LocalStrings(
			final String postFix )
	{
		string = "mod.chiselsandbits." + postFix;
	}

	@Override
	public String toString()
	{
		return string;
	}

	public String getLocal()
	{
		return StatCollector.translateToLocal( string );
	}

}
