package mod.chiselsandbits.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;

public interface IVoxelBlobItem
{

	void rotate(
			final ItemStack is,
			final Direction.Axis axis,
			final Rotation rotation );
}
