package mod.chiselsandbits.api.variant.state;

import com.communi.suggestu.scena.core.fluid.FluidInformation;
import com.mojang.serialization.Codec;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents the state variant manager, which allows external mods to provide their own state variants.
 */
public interface IStateVariantManager
{

    /**
     * The codec for the state variant provider.
     * <p>
     *     This codec is used to serialize and deserialize the state variant provider that created a given state variant.
     *     It is used to store the state variant provider type in the serialized data.
     *
     * @return The codec.
     */
    Codec<IStateVariantProvider> byNameCodec();

    /**
     * The codec for the state variant provider.
     * <p>
     *     This codec is used to serialize and deserialize the state variant provider that created a given state variant.
     *     It is used to store the state variant provider type in the serialized streaming data.
     *
     * @return The codec.
     */
    StreamCodec<RegistryFriendlyByteBuf, IStateVariantProvider> byNameStreamCodec();

    /**
     * The current instance of the state variant manager.
     *
     * @return The instance.
     */
    static IStateVariantManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getStateVariantManager();
    }

    /**
     * Registers a new {@link IStateVariantProvider provider} for the given block.
     *
     * @param block The block to register the provider for.
     * @param provider The provider to register.
     * @return The manager instance.
     */
    IStateVariantManager registerProvider(final Supplier<Block> block, final IStateVariantProvider provider);

    /**
     * Returns the state variant, if one exists, for the blockstate and block entity combination.
     *
     * @param blockState The block state.
     * @param blockEntity The block entity.
     * @return The state variant, if one exists.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<IStateVariant> getStateVariant(BlockState blockState, Optional<BlockEntity> blockEntity);

    /**
     * Returns the state variant, if one exists, for the fluid state.
     *
     * @param fluidStateIn The fluid state.
     * @return The state variant, if one exists.
     */
    Optional<IStateVariant> getStateVariant(FluidState fluidStateIn);

    /**
     * Returns the state variant, if one exists, for the block state and item stack combination.
     *
     * @param blockState The block state.
     * @param itemStack The item stack.
     * @return The state variant, if one exists.
     */
    Optional<IStateVariant> getStateVariant(final BlockState blockState, ItemStack itemStack);

    /**
     * Returns the state variant, if one exists, for the fluid information.
     *
     * @param fluidInformation The fluid information.
     * @return The state variant, if one exists.
     */
    Optional<IStateVariant> getStateVariant(FluidInformation fluidInformation);

    /**
     * Returns all the default variants for the given block state.
     *
     * @param state The block state.
     * @return The default variants.
     */
    Collection<BlockInformation> getAllDefaultVariants(final BlockState state);

    /**
     * Returns the item stack for the given block information entry.
     *
     * @param blockInformation The block information entry.
     * @return The item stack.
     */
    Optional<ItemStack> getItemStack(BlockInformation blockInformation);

    /**
     * Returns the fluid information for the given block information entry.
     *
     * @param blockInformation The block information entry.
     * @param amount The amount of fluid.
     * @return The fluid information.
     */
    Optional<FluidInformation> getFluidInformation(BlockInformation blockInformation, long amount);

    /**
     * returns the name of the contained variant if it is present.
     *
     * @param blockInformation The block information with the variant for which a name is being looked up.
     * @return The name if present.
     */
    Optional<Component> getName(BlockInformation blockInformation);
}
