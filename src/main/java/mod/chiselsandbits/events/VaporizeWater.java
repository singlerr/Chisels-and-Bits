package mod.chiselsandbits.events;

import mod.chiselsandbits.api.EventFullBlockRestoration;
import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VaporizeWater
{

	@SubscribeEvent
	public void handle(
			final EventFullBlockRestoration e )
	{
		if ( e.getState().getBlock() == Blocks.WATER && e.getWorld().func_230315_m_().func_236040_e_() )
		{
            double i = e.getPos().getX();
            double j = e.getPos().getY();
            double k = e.getPos().getZ();
            e.getWorld().playSound(i,j,k, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (e.getWorld().rand.nextFloat() - e.getWorld().rand.nextFloat()) * 0.8F, true);

            for(int l = 0; l < 8; ++l) {
                e.getWorld().addParticle(ParticleTypes.LARGE_SMOKE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0D, 0.0D, 0.0D);
            }

			e.getWorld().setBlockState( e.getPos(), Blocks.AIR.getDefaultState() );
			e.setCanceled( true );
		}
	}

}
