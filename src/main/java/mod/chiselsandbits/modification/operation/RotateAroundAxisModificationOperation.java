package mod.chiselsandbits.modification.operation;

import mod.chiselsandbits.api.modification.operation.IModificationTableOperation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.util.Direction;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class RotateAroundAxisModificationOperation extends ForgeRegistryEntry<IModificationTableOperation> implements IModificationTableOperation
{
    private final Direction.Axis axis;

    private RotateAroundAxisModificationOperation(final Direction.Axis axis) {this.axis = axis;}

    @Override
    public IMultiStateSnapshot apply(final IMultiStateSnapshot source)
    {
        final IMultiStateSnapshot clone = source.clone();
        clone.rotate(axis);

        return clone;
    }

    public static final class Builder
    {
        private Direction.Axis axis;

        private Builder() {}

        public static Builder create() { return new Builder(); }

        public Builder withAxis(Direction.Axis axis)
        {
            this.axis = axis;
            return this;
        }

        public RotateAroundAxisModificationOperation build() { return new RotateAroundAxisModificationOperation(axis); }
    }
}
