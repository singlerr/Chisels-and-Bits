package mod.chiselsandbits.api.client.icon;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public interface IIconManager
{
    void registerIcon(ResourceLocation name);

    TextureAtlasSprite getIcon(ResourceLocation name);

    TextureAtlasSprite getSwapIcon();

    TextureAtlasSprite getPlaceIcon();

    TextureAtlasSprite getUndoIcon();

    TextureAtlasSprite getRedoIcon();

    TextureAtlasSprite getTrashIcon();

    TextureAtlasSprite getSortIcon();

    TextureAtlasSprite getRollXIcon();

    TextureAtlasSprite getRollZIcon();

    TextureAtlasSprite getWhiteIcon();

    void bindTexture();
}
