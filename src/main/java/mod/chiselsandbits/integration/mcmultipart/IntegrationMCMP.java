package mod.chiselsandbits.integration.mcmultipart;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.integration.IntegrationBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;

public class IntegrationMCMP extends IntegrationBase
{

	public boolean canUseMCMP = Loader.isModLoaded( "mcmultipart" );
	public Object relay;

	@Override
	public void preinit()
	{
		if ( canUseMCMP )
		{
			relay = new MCMPRelay();
			( (MCMPRelay) relay ).initModIfPossible();
		}
	}

	@Override
	public void init()
	{

	}

	@Override
	public void postinit()
	{

	}

	public TileEntityBlockChiseled getChiseledTileEntity(
			final TileEntity te,
			final boolean create )
	{
		if ( canUseMCMP )
		{
			return ( (MCMPRelay) relay ).getPartIfPossible( te, create );
		}

		return null;
	}

	public void removeChisledBlock(
			final TileEntity te )
	{
		if ( canUseMCMP )
		{
			( (MCMPRelay) relay ).removePartIfPossible( te );
		}
	}

	public boolean isMultiPartTileEntity(
			final TileEntity target )
	{
		if ( canUseMCMP )
		{
			return ( (MCMPRelay) relay ).isMultiPart( target );
		}

		return false;
	}

	public void convertTo(
			final TileEntity current,
			final TileEntityBlockChiseled newTileEntity )
	{
		if ( canUseMCMP )
		{
			( (MCMPRelay) relay ).convertIfPossible( current, newTileEntity );
		}
	}
}
