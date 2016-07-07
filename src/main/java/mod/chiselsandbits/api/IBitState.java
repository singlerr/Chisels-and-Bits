package mod.chiselsandbits.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public interface IBitState
{

	boolean isEmpty();

	boolean isFilled();

	IBlockState getBlockState();

	int getStateID();

	IStringSerializable getExtra();

}
