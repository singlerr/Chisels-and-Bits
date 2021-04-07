package mod.chiselsandbits.client.model.baked.simple;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class NullBakedModel implements IBakedModel
{

    public static final NullBakedModel instance = new NullBakedModel();

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return false;
    }

    @Override
    public boolean isGui3d()
    {
        return false;
    }

    @Override
    public boolean isSideLit()
    {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
          .apply(MissingTextureSprite.getLocation());
    }

    @NotNull
    @Override
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return ItemCameraTransforms.DEFAULT;
    }

    @NotNull
    @Override
    public ItemOverrideList getOverrides()
    {
        return ItemOverrideList.EMPTY;
    }

}