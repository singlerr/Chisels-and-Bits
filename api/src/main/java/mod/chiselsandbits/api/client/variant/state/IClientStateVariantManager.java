package mod.chiselsandbits.api.client.variant.state;

import com.communi.suggestu.scena.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * The state variant provider for the client.
 * Handles client specific logic related to the state variant.
 */
public interface IClientStateVariantManager {

    static IClientStateVariantManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getClientStateVariantManager();
    }

    /**
     * Registers a new state variant provider.
     *
     * @param provider The provider to register.
     * @return The manager instance.
     */
    IClientStateVariantManager registerStateVariantProvider(Supplier<Block> block, IClientStateVariantProvider provider);

    /**
     * Retrieves the block model data for the state block information.
     *
     * @param blockInformation The state block information.
     * @return The block model data.
     */
    IBlockModelData getBlockModelData(IBlockInformation blockInformation);
}
