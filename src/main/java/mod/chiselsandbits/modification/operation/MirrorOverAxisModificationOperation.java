package mod.chiselsandbits.modification.operation;

import mod.chiselsandbits.api.modification.operation.IModificationTableOperation;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.core.Direction;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class MirrorOverAxisModificationOperation extends ForgeRegistryEntry<IModificationTableOperation> implements IModificationTableOperation
{
    private final Direction.Axis axis;

    private MirrorOverAxisModificationOperation(final Direction.Axis axis) {this.axis = axis;}

    @Override
    public IMultiStateSnapshot apply(final IMultiStateSnapshot source)
    {
        final IMultiStateSnapshot clone = source.clone();
        clone.mirror(axis);

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

        public MirrorOverAxisModificationOperation build() { return new MirrorOverAxisModificationOperation(axis); }
    }
}
