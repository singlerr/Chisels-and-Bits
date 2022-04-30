package mod.chiselsandbits.api.util;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistry;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.Random;

/**
 * Utility class for handling {@link BlockState}s.
 */
public class BlockStateUtils
{

    private BlockStateUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockStateUtils. This is a utility class");
    }

    /**
     * Gets a random chiselable blockstate from the blocks in the registry.
     *
     * @param random The random to get the random ids from.
     * @return The default random state of a supported chiselable block.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static BlockState getRandomSupportedDefaultState(final Random random) {
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

    /**
     * Gets a random chiselable block information from the blocks in the registry.
     *
     * @param random The random to get the random ids from.
     * @return The default random information of a supported chiselable block.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static BlockInformation getRandomSupportedInformation(final Random random) {
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

        final BlockState blockState = candidate.defaultBlockState();
        return new BlockInformation(blockState, IStateVariantManager.getInstance().getStateVariant(blockState, Optional.empty()));
    }
}
