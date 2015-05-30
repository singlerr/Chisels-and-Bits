
package mod.chiselsandbits;

public enum ChiselMode
{
	SINGLE( "mod.chiselsandbits.chiselmode.single" ),
	LINE( "mod.chiselsandbits.chiselmode.line" ),
	PLANE( "mod.chiselsandbits.chiselmode.plane" ),
	CONNECTED_PLANE( "mod.chiselsandbits.chiselmode.connected_plane" ),
	CUBE_SMALL( "mod.chiselsandbits.chiselmode.cube_small" ),
	CUBE_LARGE( "mod.chiselsandbits.chiselmode.cube_large" ),
	CUBE_HUGE( "mod.chiselsandbits.chiselmode.cube_huge" );

	public final String unlocalized;

	private ChiselMode(
			final String unlocalized )
	{
		this.unlocalized = unlocalized;
	}

}
