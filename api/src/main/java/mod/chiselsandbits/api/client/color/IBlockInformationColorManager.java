package mod.chiselsandbits.api.client.color;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.blockinformation.BlockInformation;

import java.util.Optional;

public interface IBlockInformationColorManager
{
    static IBlockInformationColorManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getBlockInformationColorManager();
    }

    Optional<Integer> getColor(BlockInformation blockInformation);
}
