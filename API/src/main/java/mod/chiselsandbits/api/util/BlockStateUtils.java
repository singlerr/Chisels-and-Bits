package mod.chiselsandbits.api.util;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistry;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class BlockStateUtils
{

    private BlockStateUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockStateUtils. This is a utility class");
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static final BlockState getRandomSupportedDefaultState(final Random random) {
        final IPlatformRegistry<Block> blocks = IPlatformRegistryManager.getInstance().getBlockRegistry();

        Block candidate = blocks.getValues().stream().skip(
          random.nextInt(blocks.getValues().size())
        ).findFirst().get();

        while (!IEligibilityManager.getInstance().canBeChiseled(candidate))
        {
            candidate = blocks.getValues().stream().skip(
              random.nextInt(blocks.getValues().size())
            ).findFirst().get();
        }

        return candidate.defaultBlockState();
    }
}
