package mod.chiselsandbits.api.blockinformation;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.variant.state.IStateVariant;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface IBlockInformationFactory {

    static IBlockInformationFactory getInstance() {
        return IChiselsAndBitsAPI.getInstance().getBlockInformationFactory();
    }

    IBlockInformation create(BlockState blockState, Optional<IStateVariant> variant);
}
