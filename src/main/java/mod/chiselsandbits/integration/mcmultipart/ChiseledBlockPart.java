package mod.chiselsandbits.integration.mcmultipart;

import mcmultipart.api.multipart.IMultipartTile;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import net.minecraft.tileentity.TileEntity;

public class ChiseledBlockPart extends TileEntityBlockChiseled implements IMultipartTile
{

	public ChiseledBlockPart()
	{
		// required for loading.
	}

	@Override
	protected boolean supportsSwapping()
	{
		return false;
	}

	public ChiseledBlockPart(
			TileEntity tileEntity )
	{
		if ( tileEntity != null )
			copyFrom( (TileEntityBlockChiseled) tileEntity );
	}

	@Override
	public TileEntity getTileEntity()
	{
		return this;
	}

}
