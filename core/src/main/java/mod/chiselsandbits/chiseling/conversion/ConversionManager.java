package mod.chiselsandbits.chiseling.conversion;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.chiseling.conversion.IConversionManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.registrars.ModBlocks;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class ConversionManager implements IConversionManager
{
    private static final ConversionManager INSTANCE = new ConversionManager();

    public static ConversionManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public Optional<Block> getChiseledVariantOf(final Block block)
    {
        if (!IEligibilityManager.getInstance().canBeChiseled(IBlockInformation.create(block)))
            return Optional.empty();

        return Optional.ofNullable(ModBlocks.CHISELED_BLOCK.get());
    }

    private ConversionManager()
    {
    }
}
