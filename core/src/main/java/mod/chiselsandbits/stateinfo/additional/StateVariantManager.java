package mod.chiselsandbits.stateinfo.additional;

import com.communi.suggestu.scena.core.fluid.FluidInformation;
import com.communi.suggestu.scena.core.registries.IPlatformRegistryManager;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.api.variant.state.IStateVariant;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.api.variant.state.IStateVariantProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class StateVariantManager implements IStateVariantManager
{
    private static final StateVariantManager INSTANCE = new StateVariantManager();

    public static StateVariantManager getInstance()
    {
        return INSTANCE;
    }

    private final Map<Supplier<Block>, IStateVariantProvider> preBakeProviders = new ConcurrentHashMap<>();
    private final Map<Block, IStateVariantProvider> providers = new ConcurrentHashMap<>();

    private StateVariantManager()
    {
    }

    @Override
    public IStateVariantManager registerProvider(Supplier<Block> block, IStateVariantProvider provider) {
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
    public Optional<IStateVariant> getStateVariant(
      final BlockState blockState, final Optional<BlockEntity> blockEntity)
    {
        bakeProviders();
        if (!providers.containsKey(blockState.getBlock()))
            return Optional.empty();

        return Optional.of(providers.get(blockState.getBlock())).flatMap(provider -> provider.getStateVariant(blockState, blockEntity))
                 .map(variant -> new WrappingStateVariant(blockState.getBlock(), variant));
    }

    @Override
    public Optional<IStateVariant> getStateVariant(final FluidState fluidStateIn)
    {
        bakeProviders();
        if (!providers.containsKey(fluidStateIn.createLegacyBlock().getBlock()))
            return Optional.empty();

        return Optional.of(providers.get(fluidStateIn.createLegacyBlock().getBlock())).flatMap(provider -> provider.getStateVariant(fluidStateIn))
          .map(variant -> new WrappingStateVariant(fluidStateIn.createLegacyBlock().getBlock(), variant));
    }

    @Override
    public Optional<IStateVariant> getStateVariant(final BlockState blockState, final ItemStack itemStack)
    {
        bakeProviders();
        if (!providers.containsKey(blockState.getBlock()))
            return Optional.empty();

        return Optional.of(providers.get(blockState.getBlock())).flatMap(provider -> provider.getStateVariant(blockState, itemStack))
          .map(variant -> new WrappingStateVariant(blockState.getBlock(), variant));
    }

    @Override
    public Optional<IStateVariant> getStateVariant(final FluidInformation fluidInformation)
    {
        bakeProviders();
        if (!providers.containsKey(fluidInformation.fluid().defaultFluidState().createLegacyBlock().getBlock()))
            return Optional.empty();

        return Optional.of(providers.get(fluidInformation.fluid().defaultFluidState().createLegacyBlock().getBlock()))
          .flatMap(provider -> provider.getStateVariant(fluidInformation))
          .map(variant -> new WrappingStateVariant(fluidInformation.fluid().defaultFluidState().createLegacyBlock().getBlock(), variant));
    }

    @Override
    public Collection<IBlockInformation> getAllDefaultVariants(final BlockState state)
    {
        bakeProviders();
        if (!providers.containsKey(state.getBlock()))
            return Collections.emptyList();

        return Optional.of(providers.get(state.getBlock())).map(provider -> provider.getAllDefaultVariants(state))
                 .stream()
                 .flatMap(Collection::stream)
                 .map(variant -> new WrappingStateVariant(state.getBlock(), variant))
                 .map(variant -> new BlockInformation(state, Optional.of(variant)))
                 .collect(Collectors.toSet());
    }

    @Override
    public CompoundTag serializeNBT(final IStateVariant variant)
    {
        if (!(variant instanceof WrappingStateVariant wrappingStateVariant))
            throw new IllegalArgumentException("additionalStateInfo must be a WrappingStateVariant");

        bakeProviders();
        final Block block = wrappingStateVariant.sourceBlock();

        final CompoundTag tag = new CompoundTag();
        tag.putString(NbtConstants.BLOCK, IPlatformRegistryManager.getInstance().getBlockRegistry().getKey(block).toString());
        tag.put(NbtConstants.VARIANT, providers.get(block).serializeNBT(wrappingStateVariant.delegate()));
        return tag;
    }

    @Override
    public IStateVariant deserializeNBT(final CompoundTag tag)
    {
        bakeProviders();
        final Optional<Block> block = IPlatformRegistryManager.getInstance().getBlockRegistry().getValue(new ResourceLocation(tag.getString(NbtConstants.BLOCK)));
        if (block.isEmpty())
            throw new IllegalStateException("Missing block from variant!");

        final IStateVariant delegate = providers.get(block.get()).deserializeNBT(tag.getCompound(NbtConstants.VARIANT));
        return new WrappingStateVariant(
          block.get(),
          delegate
        );
    }

    @Override
    public void serializeInto(final FriendlyByteBuf packetBuffer, final IStateVariant variant)
    {
        if (!(variant instanceof WrappingStateVariant wrappingStateVariant))
            throw new IllegalArgumentException("additionalStateInfo must be a WrappingStateVariant");

        bakeProviders();
        packetBuffer.writeResourceLocation(IPlatformRegistryManager.getInstance().getBlockRegistry().getKey(wrappingStateVariant.sourceBlock()));
        providers.get(wrappingStateVariant.sourceBlock()).serializeInto(packetBuffer, wrappingStateVariant.delegate());
    }

    @Override
    public IStateVariant deserializeFrom(final FriendlyByteBuf packetBuffer)
    {
        bakeProviders();
        final Optional<Block> block = IPlatformRegistryManager.getInstance().getBlockRegistry().getValue(packetBuffer.readResourceLocation());
        if (block.isEmpty())
            throw new IllegalStateException("Missing block from variant!");

        final IStateVariant stateVariant = providers.get(block.get()).deserializeFrom(packetBuffer);
        return new WrappingStateVariant(
          block.get(),
          stateVariant
        );
    }

    @Override
    public Optional<ItemStack> getItemStack(final IBlockInformation blockInformation)
    {
        bakeProviders();
        if (!providers.containsKey(blockInformation.getBlockState().getBlock()))
            return Optional.empty();

        return blockInformation.getVariant().flatMap(stateVariant -> providers.get(blockInformation.getBlockState().getBlock()).getItemStack(stateVariant));
    }

    @Override
    public Optional<FluidInformation> getFluidInformation(final IBlockInformation blockInformation, final long amount)
    {
        bakeProviders();
        if (!providers.containsKey(blockInformation.getBlockState().getBlock()))
            return Optional.empty();

        return blockInformation.getVariant().flatMap(variant -> providers.get(blockInformation.getBlockState().getBlock()).getFluidInformation(variant, amount));
    }

    public Optional<IStateVariant> unwrapVariant(Optional<IStateVariant> variant) {
        return variant.map(v -> {
            if (v instanceof WrappingStateVariant wrappingStateVariant) {
                return wrappingStateVariant.delegate();
            }
            return v;
        });
    }

    private record WrappingStateVariant(Block sourceBlock, IStateVariant delegate) implements IStateVariant {
            private static final Comparator<IStateVariant> VARIANT_COMPARATOR = Comparator.<IStateVariant, String>comparing(
                    variant -> variant.getClass().getName()
            ).thenComparing(Comparator.naturalOrder());

        @Override
            public int compareTo(@NotNull final IStateVariant o) {
                if (o instanceof WrappingStateVariant wrappingStateVariant) {
                    return VARIANT_COMPARATOR.compare(delegate, wrappingStateVariant.delegate);
                }
                return -1;
            }

            @Override
            public IStateVariant createSnapshot() {
                return new WrappingStateVariant(
                        sourceBlock,
                        delegate.createSnapshot()
                );
            }
        }
}
