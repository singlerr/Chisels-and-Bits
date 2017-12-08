package mod.chiselsandbits.blueprints;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class BlueprintPosition
{

	public final BlockPos center;
	public final BlockPos min;
	public final BlockPos max;

	public final BlockPos bitOffset;
	public final EnumFacing axisX;
	public final EnumFacing axisY;
	public final EnumFacing axisZ;
	public final BlockPos afterOffset;

	public BlueprintPosition(
			BlockPos center,
			BlockPos min,
			BlockPos max,
			BlockPos bitOffset,
			EnumFacing axisX,
			EnumFacing axisY,
			EnumFacing axisZ,
			BlockPos afterOffset )
	{
		this.center = center;
		this.min = min;
		this.max = max;
		this.bitOffset = bitOffset;
		this.axisX = axisX;
		this.axisY = axisY;
		this.axisZ = axisZ;
		this.afterOffset = afterOffset;
	}

}
