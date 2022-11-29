package mod.chiselsandbits.client.colors;

import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.client.color.IBlockInformationColorManager;

import java.util.Optional;

public final class BlockInformationColorManager implements IBlockInformationColorManager
{
    private static final BlockInformationColorManager INSTANCE = new BlockInformationColorManager();

    public static BlockInformationColorManager getInstance()
    {
        return INSTANCE;
    }

    private BlockInformationColorManager()
    {
    }
    @Override
    public Optional<Integer> getColor(final IBlockInformation blockInformation)
    {
        return Optional.empty();
    }
}
