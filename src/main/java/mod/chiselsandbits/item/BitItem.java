package mod.chiselsandbits.item;

import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.item.chisel.IChiselingItem;
import mod.chiselsandbits.api.item.leftclick.LeftClickProcessingState;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BitItem extends Item implements IChiselingItem
{

    private static final Logger LOGGER = LogManager.getLogger();

    public BitItem(final Properties properties)
    {
        super(properties);
    }

    /**
     * Callback invoked when a supported item is used to left click.
     * <p>
     * Allows for said item to take over the processing logic of the left clicking and can afterwards block the further vanilla processing.
     *
     * @param playerEntity The entity who left clicked.
     * @param hand         The hand with which the entity left clicked.
     * @param position     The position on which the entity left clicked.
     * @param face         The face on which the entity left clicked.
     * @param currentState The current state of the left click processing.
     * @return The processing state with which the processing of the left click should continue.
     */
    @Override
    public LeftClickProcessingState handleLeftClickProcessing(
      final PlayerEntity playerEntity, final Hand hand, final BlockPos position, final Direction face, final LeftClickProcessingState currentState)
    {
        final ItemStack itemStack = playerEntity.getHeldItem(hand);
        if (itemStack.isEmpty() || itemStack.getItem() != this)
            return currentState;

        final IChiselingItem chiselingItem = (IChiselingItem) itemStack.getItem();
        final IChiselMode chiselMode = chiselingItem.getMode(itemStack);

        final IChiselingContext context = IChiselingManager.getInstance().getOrCreateContext(
          playerEntity,
          chiselMode
        );

        return chiselMode.onLeftClickBy(
          playerEntity,
          context
        );
    }

    @NotNull
    @Override
    public IChiselMode getMode(final ItemStack stack)
    {
        final CompoundNBT stackNbt = stack.getOrCreateTag();
        if (stackNbt.contains(NbtConstants.CHISEL_MODE))
        {
            final String chiselModeName = stackNbt.getString(NbtConstants.CHISEL_MODE);
            try {
                final IChiselMode registryMode = IChiselMode.getRegistry().getValue(new ResourceLocation(chiselModeName));
                if (registryMode == null)
                {
                    return IChiselMode.getDefaultMode();
                }

                return registryMode;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error(String.format("An ItemStack got loaded with a name that is not a valid chisel mode: %s", chiselModeName));
                this.setMode(stack, IChiselMode.getDefaultMode());
            }
        }

        return IChiselMode.getDefaultMode();
    }

    @Override
    public void setMode(final ItemStack stack, final IChiselMode mode)
    {
        stack.getOrCreateTag().putString(NbtConstants.CHISEL_MODE, Objects.requireNonNull(mode.getRegistryName()).toString());
    }

    @NotNull
    @Override
    public Iterable<IChiselMode> getPossibleModes()
    {
        return IChiselMode.getRegistry().getValues();
    }
}
