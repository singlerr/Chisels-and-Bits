package mod.chiselsandbits.modification.operation;

import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.modification.operation.IModificationOperationGroup;
import mod.chiselsandbits.api.multistate.mutator.IGenerallyModifiableAreaMutator;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.registrars.ModModificationOperationGroups;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RotateAroundAxisModificationOperation extends ForgeRegistryEntry<IModificationOperation> implements IModificationOperation
{
    private final Direction.Axis axis;

    private RotateAroundAxisModificationOperation(final Direction.Axis axis) {this.axis = axis;}

    @Override
    public void apply(final IGenerallyModifiableAreaMutator source)
    {
        source.rotate(axis);
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return ModModificationOperationGroups.ROTATE.getIcon();
    }

    @Override
    public @NotNull Optional<IModificationOperationGroup> getGroup()
    {
        return Optional.of(ModModificationOperationGroups.ROTATE);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        switch (axis)
        {
            case X:
                return LocalStrings.PatternModificationAcrossXAxis.getText();
            case Y:
                return LocalStrings.PatternModificationAcrossYAxis.getText();
            case Z:
                return LocalStrings.PatternModificationAcrossZAxis.getText();
            default:
                throw new IllegalStateException("Unexpected value: " + axis);
        }
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
