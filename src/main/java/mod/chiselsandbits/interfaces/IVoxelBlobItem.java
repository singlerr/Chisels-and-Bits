package mod.chiselsandbits.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.Rotation;

public interface IVoxelBlobItem
{

	void rotate(
			final ItemStack is,
			final Axis axis,
			final Rotation rotation );
}
