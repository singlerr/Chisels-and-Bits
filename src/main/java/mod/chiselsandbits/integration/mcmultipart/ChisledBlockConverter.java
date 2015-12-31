package mod.chiselsandbits.integration.mcmultipart;

import java.util.Collection;
import java.util.Collections;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IPartConverter;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ChisledBlockConverter implements IPartConverter
{

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public Collection<Block> getConvertableBlocks()
	{
		return (Collection) ChiselsAndBits.getBlocks().getConversions().values();
	}

	@Override
	public Collection<? extends IMultipart> convertBlock(
			final IBlockAccess world,
			final BlockPos pos )
	{
		final TileEntity te = world.getTileEntity( pos );

		if ( te instanceof TileEntityBlockChiseled )
		{
			final ChisledBlockPart part = new ChisledBlockPart();

			part.bc = (BlockChiseled) world.getBlockState( pos ).getBlock();
			part.inner = new TileEntityBlockChiseled();
			part.inner.copyFrom( (TileEntityBlockChiseled) te );

			return Collections.singletonList( part );
		}

		return Collections.emptyList();
	}

}
