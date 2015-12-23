package mod.chiselsandbits.chiseledblock;

import net.minecraftforge.common.property.IExtendedBlockState;

public class TileEntityBlockChiseledTESR extends TileEntityBlockChiseled
{

	public IExtendedBlockState getTileRenderState()
	{
		return getState( true, 0 );
	}

	@Override
	public boolean shouldRenderInPass(
			final int pass )
	{
		return true;
	}

}
