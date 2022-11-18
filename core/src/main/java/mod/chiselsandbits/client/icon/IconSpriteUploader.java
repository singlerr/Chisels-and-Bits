package mod.chiselsandbits.client.icon;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class IconSpriteUploader extends TextureAtlasHolder
{
    private final List<ResourceLocation> textures = Lists.newArrayList();

    public static final ResourceLocation TEXTURE_MAP_NAME = new ResourceLocation(Constants.MOD_ID, "textures/atlas/icons.png");

    public IconSpriteUploader()
    {
        super(Minecraft.getInstance().getTextureManager(), TEXTURE_MAP_NAME, "");
    }

    public void registerTexture(final ResourceLocation location) {
        textures.add(location);
    }

    @NotNull
    @Override
    protected Stream<ResourceLocation> getResourcesToLoad()
    {
        return this.textures.stream();
    }

    /**
     * Overridden to make it public
     */
    @Override
    public @NotNull TextureAtlasSprite getSprite(@NotNull ResourceLocation location) {
        return super.getSprite(location);
    }
}
