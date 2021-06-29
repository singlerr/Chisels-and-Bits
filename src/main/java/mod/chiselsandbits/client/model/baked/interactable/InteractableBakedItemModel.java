package mod.chiselsandbits.client.model.baked.interactable;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class InteractableBakedItemModel implements IBakedModel
{

    private final IBakedModel innerModel;

    public InteractableBakedItemModel(final IBakedModel innerModel) {this.innerModel = innerModel;}

    @Override
    public @NotNull List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, final @NotNull Random rand)
    {
        return innerModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return innerModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return innerModel.isGui3d();
    }

    @Override
    public boolean isSideLit()
    {
        return innerModel.isSideLit();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return true;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture()
    {
        return innerModel.getParticleTexture();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides()
    {
        return innerModel.getOverrides();
    }

    public IBakedModel getInnerModel()
    {
        return innerModel;
    }
}
