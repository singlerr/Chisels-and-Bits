package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import net.minecraft.world.level.block.state.BlockState;

public interface ICullTest
{

	boolean isVisible(
			BlockInformation mySpot,
      BlockInformation secondSpot );

}
