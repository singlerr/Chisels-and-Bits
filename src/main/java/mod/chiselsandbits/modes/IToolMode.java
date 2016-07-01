package mod.chiselsandbits.modes;

import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.item.ItemStack;

public interface IToolMode
{

	void setMode(
			ItemStack ei );

	LocalStrings getName();

	String name();

	boolean isDisabled();

	int ordinal();

}
