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
			boolean clientside )
	{
		return new ChisledBlockPart();
	}

	@Override
	public ItemStack createStack(
			IMicroMaterial material,
			int arg1,
			int arg2 )
	{
		ChisledBlockPart part = (ChisledBlockPart) material;
		return part.getTile().getItemStack( part.getBlock(), null );
	}

	@Override
	public String getLocalizedName(
			IMicroMaterial material,
			int arg1 )
	{
		ChisledBlockPart part = (ChisledBlockPart) material;
		return part.getBlock().getUnlocalizedName();
	}

	@Override
	public MicroblockPlacement getPlacement(
			World arg0,
			BlockPos arg1,
			IMicroMaterial arg2,
			int arg3,
			MovingObjectPosition arg4,
			EntityPlayer arg5 )
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
