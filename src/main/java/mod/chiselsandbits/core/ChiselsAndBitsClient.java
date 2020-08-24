package mod.chiselsandbits.core;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ChiselsAndBitsClient
{

    @OnlyIn(Dist.CLIENT)
    public static void onClientInit(FMLClientSetupEvent clientSetupEvent)
    {
        // load this after items are created...
        //TODO: Load clipboard
        //CreativeClipboardTab.load( new File( configFile.getParent(), MODID + "_clipboard.cfg" ) );

        ClientSide.instance.preinit( ChiselsAndBits.getInstance() );
        ClientSide.instance.init( ChiselsAndBits.getInstance() );
        ClientSide.instance.postinit( ChiselsAndBits.getInstance() );
    }
}
