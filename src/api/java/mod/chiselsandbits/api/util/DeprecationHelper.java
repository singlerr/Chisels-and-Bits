package mod.chiselsandbits.api.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.LanguageMap;
import net.minecraftforge.fml.DistExecutor;

@SuppressWarnings( "deprecation" )
public class DeprecationHelper
{

	public static int getLightValue(
			final BlockState state )
	{
		return state.getBlock().getLightValue( state, new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO );
	}

	public static BlockState getStateFromItem(
			final ItemStack bitItemStack )
	{
		if ( bitItemStack != null && bitItemStack.getItem() instanceof BlockItem)
		{
			final BlockItem blkItem = (BlockItem) bitItemStack.getItem();
			return blkItem.getBlock().getDefaultState();
		}

		return null;
	}

	public static String translateToLocal(
			final String string )
	{
	    return DistExecutor.unsafeRunForDist(
          () -> () -> {
              final String translated = LanguageMap.getInstance().func_230503_a_(string);
              if (translated.equals(string))
                  return LanguageHandler.translateKey(string);

              return translated;
              },
          () -> () -> LanguageHandler.translateKey(string)
        );
	}

	public static String translateToLocal(
			final String string,
			final Object... args )
	{
        return String.format(translateToLocal(string), args);
	}

	public static SoundType getSoundType(
			BlockState block )
	{
		return block.getBlock().soundType;
	}

    public static SoundType getSoundType(
      Block block )
    {
        return block.getBlock().soundType;
    }
}
