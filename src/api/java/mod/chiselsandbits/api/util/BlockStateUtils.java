package mod.chiselsandbits.api.util;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class BlockStateUtils
{

    private BlockStateUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockStateUtils. This is a utility class");
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static final BlockState getRandomSupportedDefaultState(final Random random) {
        Block candidate = ForgeRegistries.BLOCKS.getValues().stream().skip(
          random.nextInt(ForgeRegistries.BLOCKS.getValues().size())
        ).findFirst().get();

        while (!IEligibilityManager.getInstance().canBeChiseled(candidate))
        {
            candidate = ForgeRegistries.BLOCKS.getValues().stream().skip(
              random.nextInt(ForgeRegistries.BLOCKS.getValues().size())
            ).findFirst().get();
        }

        return candidate.defaultBlockState();
    }
}
