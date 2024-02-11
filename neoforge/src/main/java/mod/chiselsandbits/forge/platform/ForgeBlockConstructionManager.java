package mod.chiselsandbits.forge.platform;

import mod.chiselsandbits.api.block.IBlockConstructionManager;
import mod.chiselsandbits.forge.block.ForgeChiseledBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ForgeBlockConstructionManager implements IBlockConstructionManager {
    private static final ForgeBlockConstructionManager INSTANCE = new ForgeBlockConstructionManager();

    public static ForgeBlockConstructionManager getInstance() {
        return INSTANCE;
    }

    private ForgeBlockConstructionManager() {
    }

    @Override
    public Block createChiseledBlock(BlockBehaviour.Properties properties) {
        return new ForgeChiseledBlock(properties);
    }
}
