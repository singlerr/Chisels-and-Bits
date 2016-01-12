package mod.chiselsandbits.api;

import net.minecraft.item.ItemStack;

/**
 * Do not implement, acquire from {@link IChiselAndBitsAPI}
 */
public interface IBitBrush
{

	/**
	 * @return true when the brush is air...
	 */
	boolean isAir();

	/**
	 * Get the ItemStack for a bit, returns null for air.
	 *
	 * @param count
	 * @return ItemStack, or null for air.
	 */
	ItemStack getItemStack(
			int count );

}
