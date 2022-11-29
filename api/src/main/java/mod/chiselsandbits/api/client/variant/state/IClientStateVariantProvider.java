package mod.chiselsandbits.api.client.variant.state;

import com.communi.suggestu.scena.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.api.variant.state.IStateVariant;

/**
 * A state variant provider for client side interactions.
 */
public interface IClientStateVariantProvider {

    /**
     * Retrieves the block model data for the given state variant.
     *
     * @param variant The state variant.
     * @return The block model data.
     */
    IBlockModelData getBlockModelData(IStateVariant variant);
}
