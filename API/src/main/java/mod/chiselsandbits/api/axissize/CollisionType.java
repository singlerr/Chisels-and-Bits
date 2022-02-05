package mod.chiselsandbits.api.axissize;

import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.BlockStatePredicates;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public enum CollisionType
{
    NONE_AIR(BlockStatePredicates.NOT_AIR),
    COLLIDEABLE_ONLY(BlockStatePredicates.COLLIDEABLE_ONLY),
    ALL(BlockStatePredicates.ALL);

    private final Predicate<BlockState> isValidFor;

    CollisionType(final Predicate<BlockState> isValidFor) {this.isValidFor = isValidFor;}

    public boolean isValidFor(final IStateEntryInfo info) {return isValidFor.test(info.getState());}

    public boolean isValidFor(final BlockState blockState) {return isValidFor.test(blockState);}
}
