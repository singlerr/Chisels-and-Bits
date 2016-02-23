package mod.chiselsandbits.render.chiseledblock;

import java.security.InvalidParameterException;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import net.minecraft.util.EnumWorldBlockLayer;

public enum ChiselLayer
{
	SOLID( EnumWorldBlockLayer.SOLID, VoxelType.SOLID ),
	SOLID_FLUID( EnumWorldBlockLayer.SOLID, VoxelType.FLUID ),
	CUTOUT( EnumWorldBlockLayer.CUTOUT, null ),
	CUTOUT_MIPPED( EnumWorldBlockLayer.CUTOUT_MIPPED, null ),
	TRANSLUCENT( EnumWorldBlockLayer.TRANSLUCENT, null );

	public final EnumWorldBlockLayer layer;
	public final VoxelType type;

	private ChiselLayer(
			final EnumWorldBlockLayer layer,
			final VoxelType type )
	{
		this.layer = layer;
		this.type = type;
	}

	public boolean filter(
			final VoxelBlob vb )
	{
		if ( vb == null )
		{
			return false;
		}

		if ( vb.filter( layer ) )
		{
			if ( type != null )
			{
				return vb.filterFluids( type == VoxelType.FLUID );
			}

			return true;
		}
		return false;
	}

	public static ChiselLayer fromLayer(
			final EnumWorldBlockLayer layerInfo,
			final boolean isFluid )
	{
		switch ( layerInfo )
		{
			case CUTOUT:
				return CUTOUT;
			case CUTOUT_MIPPED:
				return CUTOUT_MIPPED;
			case SOLID:
				return isFluid ? SOLID_FLUID : SOLID;
			case TRANSLUCENT:
				return TRANSLUCENT;
		}

		throw new InvalidParameterException();
	}

}
