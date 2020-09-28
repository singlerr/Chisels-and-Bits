package mod.chiselsandbits.render.chiseledblock;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import mod.chiselsandbits.client.culling.ICullTest;
import mod.chiselsandbits.client.culling.MCCullTest;
import net.minecraft.client.renderer.RenderType;

import java.security.InvalidParameterException;

public enum ChiselRenderType
{
	SOLID( RenderType.getSolid(), VoxelType.SOLID ),
	SOLID_FLUID( RenderType.getSolid(), VoxelType.FLUID ),
	CUTOUT( RenderType.getCutout(), null ),
	CUTOUT_MIPPED( RenderType.getCutoutMipped(), null ),
	TRANSLUCENT( RenderType.getTranslucent(), null );

    public final RenderType layer;
	public final VoxelType type;

	private ChiselRenderType(
			final RenderType layer,
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

	public static ChiselRenderType fromLayer(
			final RenderType layerInfo,
			final boolean isFluid )
	{
        if (ChiselRenderType.CUTOUT.layer.equals(layerInfo))
        {
            return CUTOUT;
        }
        else if (ChiselRenderType.CUTOUT_MIPPED.layer.equals(layerInfo))
        {
            return CUTOUT_MIPPED;
        }
        else if (ChiselRenderType.SOLID.layer.equals(layerInfo))
        {
            return isFluid ? SOLID_FLUID : SOLID;
        }
        else if (ChiselRenderType.TRANSLUCENT.layer.equals(layerInfo))
        {
            return TRANSLUCENT;
        }

		throw new InvalidParameterException();
	}

	public ICullTest getTest()
	{
		return new MCCullTest();
	}

}
