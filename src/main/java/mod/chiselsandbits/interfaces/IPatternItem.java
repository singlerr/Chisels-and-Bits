package mod.chiselsandbits.interfaces;

import net.minecraft.item.ItemStack;

public interface IPatternItem
{

	ItemStack getPatternedItem(
			ItemStack stack,
			final boolean wantRealBlocks );

	boolean isWritten(
			ItemStack stack );

}
