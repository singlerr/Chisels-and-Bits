package mod.chiselsandbits.client.variant.state;

import com.communi.suggestu.scena.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.client.variant.state.IClientStateVariantManager;
import mod.chiselsandbits.api.client.variant.state.IClientStateVariantProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public void appendHoverText(IBlockInformation blockInformation, Level level, List<Component> tooltip, TooltipFlag flags) {
        bakeProviders();
        if (blockInformation.getVariant().isEmpty() || !providers.containsKey(blockInformation.getBlockState().getBlock()))
            return;

        final List<Component> variantLines = new ArrayList<>();
        blockInformation.getVariant().ifPresent(variant -> {
            providers.get(blockInformation.getBlockState().getBlock()).appendHoverText(variant, level, variantLines, flags);
        });

        if (!variantLines.isEmpty()) {
            tooltip.add(Component.literal(""));
            tooltip.addAll(variantLines);
        }
    }
}
