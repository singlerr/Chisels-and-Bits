
package mod.chiselsandbits.chiseledblock.data;

import net.minecraftforge.common.property.IUnlistedProperty;


public final class UnlistedVoxelBlob implements IUnlistedProperty<VoxelBlobState>
{
	@Override
	public String getName()
	{
		return "v";
	}

	@Override
	public boolean isValid(
			final VoxelBlobState value )
	{
		return true;
	}

	@Override
	public Class<VoxelBlobState> getType()
	{
		return VoxelBlobState.class;
	}

	@Override
	public String valueToString(
			final VoxelBlobState value )
	{
		return value.toString();
	}
}