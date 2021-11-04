package mod.chiselsandbits.platforms.core.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public record FluidInformation(Fluid fluid, long amount, CompoundTag data)
{
}
