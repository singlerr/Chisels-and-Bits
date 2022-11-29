package mod.chiselsandbits.client.variant.state;

import com.communi.suggestu.scena.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.client.variant.state.IClientStateVariantManager;
import mod.chiselsandbits.api.client.variant.state.IClientStateVariantProvider;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class ClientStateVariantManager implements IClientStateVariantManager {
    private static final ClientStateVariantManager INSTANCE = new ClientStateVariantManager();

    public static ClientStateVariantManager getInstance() {
        return INSTANCE;
    }

    private final Map<Supplier<Block>, IClientStateVariantProvider> preBakeProviders = new ConcurrentHashMap<>();
    private final Map<Block, IClientStateVariantProvider> providers = new ConcurrentHashMap<>();

    private ClientStateVariantManager() {
    }


    @Override
    public IClientStateVariantManager registerStateVariantProvider(Supplier<Block> block, IClientStateVariantProvider provider) {
        preBakeProviders.put(block, provider);
        return this;
    }

    private void bakeProviders() {
        if (!providers.isEmpty())
            return;

        preBakeProviders.forEach((block, provider) -> providers.put(block.get(), provider));
        preBakeProviders.clear();
    }

    @Override
    public IBlockModelData getBlockModelData(IBlockInformation blockInformation) {
        bakeProviders();
        if (blockInformation.getVariant().isEmpty() || !providers.containsKey(blockInformation.getBlockState().getBlock()))
            return IBlockModelData.empty();

        return providers.get(blockInformation.getBlockState().getBlock()).getBlockModelData(blockInformation.getVariant().get());
    }
}
