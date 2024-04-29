package mod.chiselsandbits.api.util;

import com.communi.suggestu.scena.core.registries.IPlatformRegistry;
import com.communi.suggestu.scena.core.registries.IPlatformRegistryManager;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.blockinformation.IBlockInformationFactory;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Utility class for handling {@link BlockState}s.
 */
public class BlockInformationUtils
{

    private BlockInformationUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockStateUtils. This is a utility class");
    }

    /**
     * Gets a random chiselable block information from the blocks in the registry.
     *
     * @param random The random to get the random ids from.
     * @return The default random information of a supported chiselable block.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static IBlockInformation getRandomSupportedInformation(final RandomSource random) {
        final IPlatformRegistry<Block> blocks = IPlatformRegistryManager.getInstance().getBlockRegistry();

         Block candidate = blocks.getValues().stream().skip(
          random.nextInt(blocks.getValues().size())
        ).findFirst().get();

        IBlockInformation blockInformation = IBlockInformationFactory.getInstance().create(
            candidate.defaultBlockState(),
            IStateVariantManager.getInstance().getStateVariant(candidate.defaultBlockState(), Optional.empty())
        );

        while (!IEligibilityManager.getInstance().canBeChiseled(blockInformation))
        {
            candidate = blocks.getValues().stream().skip(
                    random.nextInt(blocks.getValues().size())
            ).findFirst().get();

            blockInformation = IBlockInformationFactory.getInstance().create(
                    candidate.defaultBlockState(),
                    IStateVariantManager.getInstance().getStateVariant(candidate.defaultBlockState(), Optional.empty())
            );
        }

        return blockInformation;
    }

    /**
     * Gets a random chiselable block information from the blocks in the registry.
     *
     * @param random The random to get the random ids from.
     * @param count The amount of random block information to get.
     * @return The default random information of a supported chiselable block.
     */
    public static IBlockInformation[] getRandomSupportedInformation(final RandomSource random, int count) {
        final IPlatformRegistry<Block> blocks = IPlatformRegistryManager.getInstance().getBlockRegistry();
        final IBlockInformation[] result = new IBlockInformation[count];

        final IBlockInformation[] blockLookup = blocks.getValues().stream()
                .parallel()
                .map(block -> IBlockInformationFactory.getInstance().create(
                        block.defaultBlockState(),
                        IStateVariantManager.getInstance().getStateVariant(block.defaultBlockState(), Optional.empty())
                ))
                .filter(blockInformation -> IEligibilityManager.getInstance().canBeChiseled(blockInformation))
                .toArray(IBlockInformation[]::new);

        for (int i = 0; i < count; i++) {
            result[i] = blockLookup[random.nextInt(blockLookup.length)];
        }

        return result;
    }
}
