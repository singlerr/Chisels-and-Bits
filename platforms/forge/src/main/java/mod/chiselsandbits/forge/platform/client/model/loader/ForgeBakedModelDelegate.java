package mod.chiselsandbits.forge.platform.client.model.loader;

import mod.chiselsandbits.forge.platform.client.model.data.ForgeBlockModelDataPlatformDelegate;
import mod.chiselsandbits.platforms.core.client.models.IDataAwareBakedModel;
import mod.chiselsandbits.platforms.core.client.models.IDelegatingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class ForgeBakedModelDelegate implements BakedModel, IDelegatingBakedModel
{
    private final BakedModel delegate;

    public ForgeBakedModelDelegate(final BakedModel delegate) {this.delegate = delegate;}

    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState p_119123_, @Nullable final Direction p_119124_, final Random p_119125_)
    {
        return delegate.getQuads(p_119123_, p_119124_, p_119125_);
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        if (delegate instanceof IDataAwareBakedModel dataAwareBakedModel) {
            return dataAwareBakedModel.getQuads(state, side, rand, new ForgeBlockModelDataPlatformDelegate(extraData));
        }

        return delegate.getQuads(state, side, rand, extraData);
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return delegate.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return delegate.isGui3d();
    }

    @Override
    public boolean usesBlockLight()
    {
        return delegate.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer()
    {
        return delegate.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return delegate.getParticleIcon();
    }

    @Override
    public ItemOverrides getOverrides()
    {
        return delegate.getOverrides();
    }

    @Override
    public BakedModel getDelegate()
    {
        return delegate;
    }
}
