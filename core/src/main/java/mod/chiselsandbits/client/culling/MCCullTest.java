package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;

/**
 * Determine Culling using Block's Native Checks.
 * <p>
 * Simplified version of {@link net.minecraft.world.level.block.Block#shouldRenderFace Block.shouldRenderFace}
 */
public class MCCullTest implements ICullTest
{
	public record BlockStatePairKey(BlockState first, BlockState second, Direction direction) {}

	private static final SimpleMaxSizedCache<BlockStatePairKey, Boolean> CACHE = new SimpleMaxSizedCache<>(
			() -> IClientConfiguration.getInstance().getCullTestingCacheSize().get()
	);

	@Override
	public boolean isVisible(
			final IStateEntryInfo stateEntry,
			final BlockInformation neighbor,
			final Direction offsetDirectory )
	{
		final BlockInformation aInfo = stateEntry.getBlockInformation();
		if(aInfo == neighbor)
		{
			return false;
		}
		if(stateEntry.getBlockInformation().blockState().skipRendering(neighbor.blockState(), offsetDirectory))
		{
			return false;
		}
		if(!(stateEntry instanceof IInWorldStateEntryInfo aBlockEntity))
		{
			// Shouldn't happen?  Maybe if someone's picked up a Chiseled block or another mod is rendering it?
			return true;
		}
		if(neighbor.blockState().canOcclude() && Minecraft.getInstance().level != null)
		{
			return shouldRenderFace(
					aInfo.blockState(),
					neighbor.blockState(),
					new BlockPos(aBlockEntity.getBlockPos()),
					offsetDirectory
			);
		}
		return true;
	}

	private static boolean shouldRenderFace(final BlockState state, final BlockState neighborState, final BlockPos position, final Direction offsetDirection) {
		final BlockPos neighborPosition = position.relative(offsetDirection);
		if (state.skipRendering(neighborState, offsetDirection)) {
			return false;
		} else if (neighborState.canOcclude()) {
			BlockStatePairKey cacheKey = new BlockStatePairKey(state, neighborState, offsetDirection);
			return CACHE.get(cacheKey, () -> {
				VoxelShape voxelshape = state.getFaceOcclusionShape(Objects.requireNonNull(Minecraft.getInstance().level), position, offsetDirection);
				if (voxelshape.isEmpty()) {
					return true;
				} else {
					VoxelShape voxelshape1 = neighborState.getFaceOcclusionShape(Minecraft.getInstance().level, neighborPosition, offsetDirection.getOpposite());
					return Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.ONLY_FIRST);
				}
			});
		} else {
			return true;
		}
	}
}
