package mod.chiselsandbits.api.variant.state;

import com.communi.suggestu.scena.core.fluid.FluidInformation;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Collection;
import java.util.Optional;

public interface IStateVariantProvider
{
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
    Collection<IStateVariant> getAllDefaultVariants(final BlockState state);

    /**
     * Serializes the given state variant into a compound tag.
     *
     * @param variant The state variant.
     * @return The serialized state variant.
     */
    CompoundTag serializeNBT(IStateVariant variant);

    /**
     * Deserializes the given compound tag into a state variant.
     *
     * @param tag The compound tag.
     * @return The deserialized state variant.
     */
    IStateVariant deserializeNBT(CompoundTag tag);

    /**
     * Serializes the given state variant into a packet buffer.
     *
     * @param packetBuffer The packet buffer.
     * @param variant The state variant.
     */
    void serializeInto(FriendlyByteBuf packetBuffer, IStateVariant variant);

    /**
     * Deserializes the given packet buffer into a state variant.
     *
     * @param packetBuffer The packet buffer.
     * @return The deserialized state variant.
     */
    IStateVariant deserializeFrom(FriendlyByteBuf packetBuffer);

    /**
     * Returns the item stack for the given state variant.
     *
     * @param stateVariant The state variant.
     * @return The item stack.
     */
    Optional<ItemStack> getItemStack(IStateVariant stateVariant);

    /**
     * Returns the fluid information for the given state variant.
     *
     * @param stateVariant The state variant.
     * @param amount The amount of fluid.
     * @return The fluid information.
     */
    Optional<FluidInformation> getFluidInformation(IStateVariant stateVariant, long amount);

    /**
     * Returns the name of the given state variant.
     *
     * @param variant The state variant to get the name for.
     * @return The name of the variant if present.
     */
    Optional<Component> getName(IStateVariant variant);
}
