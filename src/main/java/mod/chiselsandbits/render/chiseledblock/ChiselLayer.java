package mod.chiselsandbits.render.chiseledblock;

import java.security.InvalidParameterException;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import mod.chiselsandbits.client.culling.ICullTest;
import mod.chiselsandbits.client.culling.MCCullTest;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.BlockRenderLayer;

public enum ChiselLayer
{
	SOLID( RenderType.getSolid(), VoxelType.SOLID ),
	SOLID_FLUID( RenderType.getSolid(), VoxelType.FLUID ),
	CUTOUT( RenderType.getCutout(), null ),
	CUTOUT_MIPPED( RenderType.getCutoutMipped(), null ),
	TRANSLUCENT( RenderType.getTranslucent(), null );

	public final RenderType layer;
	public final VoxelType type;

	private ChiselLayer(
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

	public static ChiselLayer fromLayer(
			final RenderType layerInfo,
			final boolean isFluid )
	{
        if (ChiselLayer.CUTOUT.layer.equals(layerInfo))
        {
            return CUTOUT;
        }
        else if (ChiselLayer.CUTOUT_MIPPED.layer.equals(layerInfo))
        {
            return CUTOUT_MIPPED;
        }
        else if (ChiselLayer.SOLID.layer.equals(layerInfo))
        {
            return isFluid ? SOLID_FLUID : SOLID;
        }
        else if (ChiselLayer.TRANSLUCENT.layer.equals(layerInfo))
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
