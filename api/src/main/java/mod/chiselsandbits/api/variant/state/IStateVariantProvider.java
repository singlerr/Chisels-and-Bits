package mod.chiselsandbits.api.variant.state;

import com.communi.suggestu.scena.core.fluid.FluidInformation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Collection;
import java.util.Optional;

public interface IStateVariantProvider
{
    Optional<IStateVariant> getStateVariant(BlockState blockState, Optional<BlockEntity> blockEntity);

    Optional<IStateVariant> getStateVariant(FluidState fluidStateIn);

    Optional<IStateVariant> getStateVariant(BlockState blockState, ItemStack itemStack);

    Optional<IStateVariant> getStateVariant(FluidInformation fluidInformation);

    Collection<IStateVariant> getAllDefaultVariants(BlockState state);

    CompoundTag serializeNBT(IStateVariant variant);

    IStateVariant deserializeNBT(CompoundTag compound);

    void serializeInto(FriendlyByteBuf packetBuffer, IStateVariant delegate);

    IStateVariant deserializeFrom(FriendlyByteBuf packetBuffer);

    Optional<ItemStack> getItemStack(Optional<IStateVariant> variant);

    Optional<FluidInformation> getFluidInformation(Optional<IStateVariant> variant, long amount);
}
