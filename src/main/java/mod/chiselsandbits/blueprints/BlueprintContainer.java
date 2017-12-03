package mod.chiselsandbits.blueprints;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlueprintContainer extends Container
{
	final EntityPlayer thePlayer;
	protected int bluePrintSlot;

	public BlueprintContainer(
			final EntityPlayer player,
			final World world,
			final int x,
			final int y,
			final int z )
	{
		thePlayer = player;
		bluePrintSlot = player.inventory.currentItem;
	}

	@Override
	public boolean canInteractWith(
			final EntityPlayer playerIn )
	{
		return playerIn == thePlayer && hasBlueprintInHand( thePlayer );
	}

	private boolean hasBlueprintInHand(
			final EntityPlayer player )
	{
		final ItemStack inHand = player.getHeldItem( EnumHand.MAIN_HAND );

		if ( inHand != null && inHand.getItem() instanceof ItemBlueprint )
		{
			return true;
		}

		return false;
	}

	@SideOnly( Side.CLIENT )
	public static Object getGuiClass()
	{
		return BlueprintGui.class;
	}

}
