package mod.chiselsandbits.api.item.measuring;

import mod.chiselsandbits.api.item.INoHitEffectsItem;
import mod.chiselsandbits.api.item.click.IRightClickControllingItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.measuring.MeasuringMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IMeasuringTapeItem extends INoHitEffectsItem, IWithModeItem<MeasuringMode>, IRightClickControllingItem
{

    /**
     * Determines the start point stored in the stack of the item.
     *
     * @param stack The stack to get the start point from if it is available.
     * @return An optional with the start point if it is set.
     */
    @NotNull
    Optional<Vec3> getStart(@NotNull final ItemStack stack);

    /**
     * Sets the start point of the current measurement.
     *
     * @param stack The stack to set the start on.
     * @param start The start point.
     */
    void setStart(@NotNull final ItemStack stack, @NotNull final Vec3 start);

    /**
     * Clears the measurement state of a given stack.
     * @param stack The stack to clear the measurement state from.
     */
    void clear(@NotNull final ItemStack stack);
}
