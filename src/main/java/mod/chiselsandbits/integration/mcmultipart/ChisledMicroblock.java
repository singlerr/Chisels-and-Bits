package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.microblock.IMicroMaterial;
import mcmultipart.microblock.MicroblockClass;
import mcmultipart.microblock.MicroblockPlacement;
import mcmultipart.microblock.MicroblockPlacementGrid;
import mcmultipart.multipart.IMultipart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ChisledMicroblock extends MicroblockClass
{

	final public static MicroblockClass instance = new ChisledMicroblock();

	@Override
	public IMultipart create(
			final boolean clientside )
	{
		return new ChisledBlockPart();
	}

	@Override
	public ItemStack createStack(
			final IMicroMaterial material,
			final int arg1,
			final int arg2 )
	{
		final ChisledBlockPart part = (ChisledBlockPart) material;
		return part.getTile().getItemStack( part.getBlock(), null );
	}

	@Override
	public String getLocalizedName(
			final IMicroMaterial material,
			final int arg1 )
	{
		final ChisledBlockPart part = (ChisledBlockPart) material;
		return part.getBlock().getUnlocalizedName();
	}

	@Override
	public MicroblockPlacement getPlacement(
			final World arg0,
			final BlockPos arg1,
			final IMicroMaterial arg2,
			final int arg3,
			final MovingObjectPosition arg4,
			final EntityPlayer arg5 )
	{
		return null;
	}

	@Override
	public MicroblockPlacementGrid getPlacementGrid()
	{
		return null;
	}

	@Override
	public String getType()
	{
		return MCMultiPart.block_name;
	}

}
