package mod.chiselsandbits.api.variant.state;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Collection;
import java.util.Optional;

public interface IStateVariantManager
{
    static IStateVariantManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getAdditionalStateInfoManager();
    }

    Optional<IStateVariant> getStateVariant(BlockState blockState, Optional<BlockEntity> blockEntity);

    Optional<IStateVariant> getStateVariant(FluidState fluidStateIn);

    Optional<IStateVariant> getStateVariant(final BlockState blockState, ItemStack itemStack);

    Optional<IStateVariant> getStateVariant(FluidInformation fluidInformation);

    Collection<BlockInformation> getAllDefaultVariants(final BlockState state);

    CompoundTag serializeNBT(IStateVariant additionalStateInfo);

    IStateVariant deserializeNBT(CompoundTag tag);

    void serializeInto(FriendlyByteBuf packetBuffer, IStateVariant info);

    IStateVariant deserializeFrom(FriendlyByteBuf packetBuffer);

    Optional<ItemStack> getItemStack(BlockInformation blockInformation);

    Optional<FluidInformation> getFluidInformation(BlockInformation state, long amount);

}
