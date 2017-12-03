package mod.chiselsandbits.registry;

import mod.chiselsandbits.blueprints.EntityBlueprint;
import mod.chiselsandbits.blueprints.RenderEntityBlueprint;
import mod.chiselsandbits.config.ModConfig;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class ModEntities
{

	boolean isClient;

	public ModEntities(
			final ModConfig config,
			Side side )
	{
		ChiselsAndBits.registerWithBus( this );
		isClient = side == Side.CLIENT;
	}

	@SubscribeEvent
	public void entityRegistration(
			final RegistryEvent.Register<EntityEntry> event )
	{
		ResourceLocation id = new ResourceLocation( ChiselsAndBits.MODID, "blueprint" );
		String name = "mod.chiselsandbits.blueprint";

		EntityEntry e = new EntityEntry( EntityBlueprint.class, name );
		e.setRegistryName( id );
		event.getRegistry().register( e );

		EntityRegistry.registerModEntity( id, EntityBlueprint.class, "bluprint", 0, ChiselsAndBits.getInstance(), 200, 20, false );

		if ( isClient )
		{
			RenderingRegistry.registerEntityRenderingHandler( EntityBlueprint.class, new IRenderFactory<EntityBlueprint>() {

				@Override
				public Render<EntityBlueprint> createRenderFor(
						final RenderManager manager )
				{
					return new RenderEntityBlueprint( manager );
				}

			} );
		}
	}
}
