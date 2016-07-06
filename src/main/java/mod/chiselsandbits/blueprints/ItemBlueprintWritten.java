package mod.chiselsandbits.blueprints;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemBlueprintWritten extends Item
{

	public ItemBlueprintWritten()
	{
		setMaxStackSize( 1 );
	}

	@Override
	public boolean onItemUse(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final World worldIn,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if ( !worldIn.isRemote )
		{
			final EntityBlueprint e = new EntityBlueprint( worldIn );
			e.posX = pos.getX() + 0.5;
			e.posY = pos.getY() + 0.5;
			e.posZ = pos.getZ() + 0.5;
			e.item = stack.copy();
			worldIn.spawnEntityInWorld( e );
		}

		stack.stackSize--;
		return true;
	}

}
