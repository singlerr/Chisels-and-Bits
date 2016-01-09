package mod.chiselsandbits.integration.mcmultipart;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.integration.IntegrationBase;
import net.minecraft.tileentity.TileEntity;

public class MCMultipartProxy extends IntegrationBase
{

	private static class MCMultiPartNullRelay implements IMCMultiPart
	{

		@Override
		public boolean isMultiPart(
				final TileEntity target )
		{
			return false;
		}

		@Override
		public void convertIfPossible(
				final TileEntity current,
				final TileEntityBlockChiseled newTileEntity )
		{
		}

		@Override
		public void removePartIfPossible(
				final TileEntity te )
		{

		}

		@Override
		public TileEntityBlockChiseled getPartIfPossible(
				final TileEntity te,
				final boolean create )
		{
			return null;
		}

		@Override
		public void triggerPartChange(
				final TileEntity te )
		{
		}

		@Override
		public void addFiler(
				final TileEntity te,
				final VoxelBlob vb )
		{
		}

	};

	public static final MCMultipartProxy proxyMCMultiPart = new MCMultipartProxy();
	protected IMCMultiPart relay = new MCMultiPartNullRelay();

	protected void setRelay(
			final IMCMultiPart mcMultiPart )
	{
		relay = mcMultiPart;
	}

	public TileEntityBlockChiseled getChiseledTileEntity(
			final TileEntity te,
			final boolean create )
	{
		return relay.getPartIfPossible( te, create );
	}

	public void removeChisledBlock(
			final TileEntity te )
	{
		relay.removePartIfPossible( te );
	}

	public boolean isMultiPartTileEntity(
			final TileEntity target )
	{
		return relay.isMultiPart( target );
	}

	public void convertTo(
			final TileEntity current,
			final TileEntityBlockChiseled newTileEntity )
	{
		relay.convertIfPossible( current, newTileEntity );
	}

	public void triggerPartChange(
			final TileEntity te )
	{
		relay.triggerPartChange( te );
	}

	public void addFiller(
			final TileEntity te,
			final VoxelBlob vb )
	{
		relay.addFiler( te, vb );
	}

}
