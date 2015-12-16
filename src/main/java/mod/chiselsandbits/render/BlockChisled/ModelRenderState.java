package mod.chiselsandbits.render.BlockChisled;

import java.util.Arrays;

import mod.chiselsandbits.chiseledblock.data.VoxelBlobState;
import net.minecraft.util.EnumFacing;

public class ModelRenderState
{

	public final VoxelBlobState[] sides;

	public ModelRenderState(
			final VoxelBlobState[] sides )
	{
		if ( sides == null )
		{
			this.sides = new VoxelBlobState[EnumFacing.VALUES.length];
		}
		else
		{
			this.sides = Arrays.copyOf( sides, sides.length );
		}
	}

}
