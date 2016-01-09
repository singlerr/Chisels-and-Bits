package mod.chiselsandbits.integration.mcmultipart;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import net.minecraft.tileentity.TileEntity;

public interface IMCMultiPart
{

	boolean isMultiPart(
			TileEntity target );

	void convertIfPossible(
			TileEntity current,
			TileEntityBlockChiseled newTileEntity );

	void removePartIfPossible(
			TileEntity te );

	TileEntityBlockChiseled getPartIfPossible(
			TileEntity te,
			boolean create );

	void triggerPartChange(
			TileEntity te );

	void addFiler(
			TileEntity te,
			VoxelBlob vb );

}
