package mod.chiselsandbits.client.culling;

import net.minecraft.block.BlockState;

public interface ICullTest
{

	boolean isVisible(
			BlockState mySpot,
            BlockState secondSpot );

}
