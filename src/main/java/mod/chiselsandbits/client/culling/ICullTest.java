package mod.chiselsandbits.client.culling;

import net.minecraft.world.level.block.state.BlockState;

public interface ICullTest
{

	boolean isVisible(
			BlockState mySpot,
            BlockState secondSpot );

}
