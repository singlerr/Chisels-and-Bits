package mod.chiselsandbits.client.icon;

import mod.chiselsandbits.api.client.icon.IIconManager;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class IconManager implements IIconManager
{
    private static final IconManager INSTANCE = new IconManager();

    private static final ResourceLocation ICON_SWAP = new ResourceLocation(Constants.MOD_ID, "icons/swap");
    private static final ResourceLocation ICON_PLACE = new ResourceLocation(Constants.MOD_ID, "icons/place");
    private static final ResourceLocation ICON_UNDO = new ResourceLocation(Constants.MOD_ID, "icons/undo");
    private static final ResourceLocation ICON_REDO = new ResourceLocation(Constants.MOD_ID, "icons/redo");
    private static final ResourceLocation ICON_TRASH = new ResourceLocation(Constants.MOD_ID, "icons/trash");
    private static final ResourceLocation ICON_SORT = new ResourceLocation(Constants.MOD_ID, "icons/sort");
    private static final ResourceLocation ICON_ROLL_X = new ResourceLocation(Constants.MOD_ID, "icons/roll_x");
    private static final ResourceLocation ICON_ROLL_Z = new ResourceLocation(Constants.MOD_ID, "icons/roll_z");
    private static final ResourceLocation ICON_WHITE = new ResourceLocation(Constants.MOD_ID, "icons/white");

    public static IconManager getInstance()
    {
        return INSTANCE;
    }

    private IconSpriteUploader iconSpriteUploader = null;

    private IconManager()
    {
    }

    @SubscribeEvent
    public static void onBlockColorHandler(final ColorHandlerEvent.Block event)
    {
        //We use this event since this is virtually the only time we can init the IconManager and have it load the custom atlas.
        //Guard for doing stupid shit when data gen is running :D
        if (Minecraft.getInstance() != null)
            IconManager.getInstance().initialize();
    }

    private void initialize() {
        this.iconSpriteUploader = new IconSpriteUploader();
        IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof IReloadableResourceManager) {
            IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) resourceManager;
            reloadableResourceManager.addReloadListener(iconSpriteUploader);
        }

        registerIcon(ICON_SWAP);
        registerIcon(ICON_PLACE);
        registerIcon(ICON_UNDO);
        registerIcon(ICON_REDO);
        registerIcon(ICON_TRASH);
        registerIcon(ICON_SORT);
        registerIcon(ICON_ROLL_X);
        registerIcon(ICON_ROLL_Z);
        registerIcon(ICON_WHITE);
    }
    
    @Override
    public void registerIcon(final ResourceLocation name) {
        if (this.iconSpriteUploader == null)
            throw new IllegalStateException("Tried to register icon too early.");

        this.iconSpriteUploader.registerTexture(name);
    }
    
    @Override
    public TextureAtlasSprite getIcon(final ResourceLocation name) {
        if (this.iconSpriteUploader == null)
            throw new IllegalStateException("Tried to get icon too early.");

        return this.iconSpriteUploader.getSprite(name);
    }

    @Override
    public TextureAtlasSprite getSwapIcon() {
        return getIcon(ICON_SWAP);
    }

    @Override
    public TextureAtlasSprite getPlaceIcon() {
        return getIcon(ICON_PLACE);
    }

    @Override
    public TextureAtlasSprite getUndoIcon() {
        return getIcon(ICON_UNDO);
    }

    @Override
    public TextureAtlasSprite getRedoIcon() {
        return getIcon(ICON_REDO);
    }

    @Override
    public TextureAtlasSprite getTrashIcon() {
        return getIcon(ICON_TRASH);
    }

    @Override
    public TextureAtlasSprite getSortIcon() {
        return getIcon(ICON_SORT);
    }

    @Override
    public TextureAtlasSprite getRollXIcon() {
        return getIcon(ICON_ROLL_X);
    }

    @Override
    public TextureAtlasSprite getRollZIcon() {
        return getIcon(ICON_ROLL_Z);
    }

    @Override
    public TextureAtlasSprite getWhiteIcon() {
        return getIcon(ICON_WHITE);
    }

    @Override
    public void bindTexture()
    {
        Minecraft.getInstance().getTextureManager().bindTexture(IconSpriteUploader.TEXTURE_MAP_NAME);
    }
}
