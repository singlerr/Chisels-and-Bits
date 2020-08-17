package mod.chiselsandbits.interfaces;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IItemScrollWheel
{

	void scroll(
			PlayerEntity player,
			ItemStack stack,
			int dwheel );

}
