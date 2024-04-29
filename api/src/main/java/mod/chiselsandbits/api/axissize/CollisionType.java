package mod.chiselsandbits.api.axissize;

import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.BlockStatePredicates;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public enum CollisionType
{
    NONE_AIR(BlockStatePredicates.NOT_AIR, Blocks.STONE.defaultBlockState(), false),
    COLLIDEABLE_ONLY(BlockStatePredicates.COLLIDEABLE_ONLY, Blocks.STONE.defaultBlockState(), true),
    ALL(BlockStatePredicates.ALL, Blocks.AIR.defaultBlockState(), false);

    private final Predicate<BlockState> isValidFor;
    private final BlockState exampleState;
    private final boolean canBeEmptyWithJustFluids;

    CollisionType(final Predicate<BlockState> isValidFor, final BlockState exampleState, boolean canBeEmptyWithJustFluids) {this.isValidFor = isValidFor;
        this.exampleState = exampleState;
        this.canBeEmptyWithJustFluids = canBeEmptyWithJustFluids;
    }

    public boolean isValidFor(final IStateEntryInfo info) {return isValidFor.test(info.getBlockInformation().getBlockState());}

    public boolean isValidFor(final BlockState blockState) {return isValidFor.test(blockState);}

    public boolean canBeEmptyWithJustFluids() {
        return canBeEmptyWithJustFluids;
    }

    public BlockState getExampleState()
    {
        return exampleState;
    }
}
