package mod.chiselsandbits.render.chiseledblock;

import java.util.Arrays;

import mod.chiselsandbits.chiseledblock.data.VoxelBlobStateReference;
import net.minecraft.util.EnumFacing;

public class ModelRenderState
{

	public final VoxelBlobStateReference[] sides;

	public ModelRenderState(
			final VoxelBlobStateReference[] sides )
	{
		if ( sides == null )
		{
			this.sides = new VoxelBlobStateReference[EnumFacing.VALUES.length];
		}
		else
		{
			this.sides = Arrays.copyOf( sides, sides.length );
		}
	}

}
