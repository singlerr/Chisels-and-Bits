
package mod.chiselsandbits;

import mod.chiselsandbits.helpers.LocalStrings;


public enum ChiselMode
{
	SINGLE( LocalStrings.ChiselModeSingle ),
	LINE( LocalStrings.ChiselModeLine ),
	PLANE( LocalStrings.ChiselModePlane ),
	CONNECTED_PLANE( LocalStrings.ChiselModeConnectedPlane ),
	CUBE_SMALL( LocalStrings.ChiselModeCubeSmall ),
	CUBE_LARGE( LocalStrings.ChiselModeCubeLarge ),
	CUBE_HUGE( LocalStrings.ChiselModeCubeHuge );

	public final LocalStrings string;

	public boolean isDisabled = false;

	public Object binding;

	private ChiselMode(
			final LocalStrings str )
	{
		string = str;
	}

}
