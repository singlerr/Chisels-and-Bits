package mod.chiselsandbits.localization;

public enum LocalStrings implements ILocalizeable
{

	ChiselModeSingle( "chiselmode.single" ),
	ChiselModeSnap2( "chiselmode.snap2" ),
	ChiselModeSnap4( "chiselmode.snap4" ),
	ChiselModeSnap8( "chiselmode.snap8" ),
	ChiselModeLine( "chiselmode.line" ),
	ChiselModePlane( "chiselmode.plane" ),
	ChiselModeConnectedPlane( "chiselmode.connected_plane" ),
	ChiselModeConnectedMaterial( "chiselmode.connected_material" ),
	ChiselModeCubeSmall( "chiselmode.cube_small" ),
	ChiselModeCubeMedium( "chiselmode.cube_medium" ),
	ChiselModeCubeLarge( "chiselmode.cube_large" ),
	ChiselModeSameMaterial( "chiselmode.same_material" ),
	ChiselModeDrawnRegion( "chiselmode.drawn_region" ),

	ShiftDetails( "help.shiftdetails" ),
	Empty( "help.empty" ),
	Filled( "help.filled" ),

	HelpChiseledBlock( "help.chiseled_block" ),
	LongHelpChiseledBlock( "help.chiseled_block.long" ),

	HelpBitSaw( "help.bitsaw" ),
	LongHelpBitSaw( "help.bitsaw.long" ),

	HelpBitBag( "help.bit_bag" ),
	LongHelpBitBag( "help.bit_bag.long" ),

	HelpWrench( "help.wrench" ),
	LongHelpWrench( "help.wrench.long" ),

	HelpBit( "help.bit" ),
	LongHelpBit( "help.bit.long" ),

	HelpBitTank( "help.bittank" ),
	LongHelpBitTank( "help.bittank.long" ),

	ShareStart( "help.share.start" ),
	ShareComplete( "help.share.complete" ),
	ShareClipboard( "help.share.clipboard" ),
	ShareFile( "help.share.file" ),
	ShareNoFile( "help.share.nofile" ),
	ShareInvalidData( "help.share.invaliddata" ),
	ShareNoUrl( "help.share.badurl" ),

	HelpPositivePrint( "help.positiveprint" ),
	LongHelpPositivePrint( "help.positiveprint.long" ),

	HelpNegativePrint( "help.negativeprint" ),
	LongHelpNegativePrint( "help.negativeprint.long" ),

	HelpMirrorPrint( "help.mirrorprint" ),
	LongHelpMirrorPrint( "help.mirrorprint.long" ),

	HelpChisel( "help.chisel" ),
	LongHelpChisel( "help.chisel.long" ),

	noBind( "help.nobind" ),

	leftShift( "help.leftshift" ),
	rightShift( "help.rightshift" ),

	leftAlt( "help.leftalt" ),
	rightAlt( "help.rightalt" ),

	Trash( "help.trash" ),
	TrashItem( "help.trashitem" ),
	ReallyTrash( "help.reallytrash" ),
	ReallyTrashItem( "help.reallytrash_blank" ),
	TrashInvalidItem( "help.trash.invalid" ),

	PositivePatternReplace( "positivepatternmode.replace" ),
	PositivePatternAdditive( "positivepatternmode.additive" ),
	PositivePatternPlacement( "positivepatternmode.placement" ),
	PositivePatternImpose( "positivepatternmode.impose" ),

	HelpTapeMeasure( "help.tape_measure" ),
	LongHelpTapeMeasure( "help.tape_measure.long" ),
	TapeMeasureBit( "tapemeasure.bit" ),
	TapeMeasureBlock( "tapemeasure.block" ),
	TapeMeasureDistance( "tapemeasure.distance" ),

	WrenchNudgeBit( "wrench.nudge_bit" ),
	WrenchNudgeBlock( "wrench.nudge_block" ),
	WrenchRotateBlock( "wrench.rotate_block" ),
	WrenchOnlyGhosts( "wrenge.nudge_block_disabled" ),

	BlueprintBeginPlace( "blueprint.placement" ),
	BlueprintBeginCapture( "blueprint.capture" ),
	BlueprintCannotMove( "blueprint.cannotmove" ),
	BlueprintFinished( "blueprint.finished" ),
	BlueprintBlockOnly( "blueprint.bitplacementonly" ),
	BlueprintNoSuchPath( "blueprint.nosuchpath" ),
	BlueprintFailedToWrite( "blueprint.failedtowrite" ),

	BitPickBit( "bitoption.pick" ),
	BitOptionPlace( "bitoption.place" ),
	BitOptionReplace( "bitoption.replace" ),

	HelpMallet( "help.mallet" );

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

}
