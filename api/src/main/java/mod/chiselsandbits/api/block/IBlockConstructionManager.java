package mod.chiselsandbits.api.block;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface IBlockConstructionManager {

    static IBlockConstructionManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getBlockConstructionManager();
    }

    Block createChiseledBlock(BlockBehaviour.Properties properties);
}
