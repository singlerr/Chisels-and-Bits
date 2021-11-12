package mod.chiselsandbits.client.model.baked.base;

import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.platforms.core.client.models.IDataAwareBakedModel;
import mod.chiselsandbits.platforms.core.client.models.data.IModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public abstract class BaseSmartModel implements BakedModel, IDataAwareBakedModel
{

	private final ItemOverrides overrides;

	private static class OverrideHelper extends ItemOverrides
	{
		final BaseSmartModel parent;

		public OverrideHelper(
				final BaseSmartModel p )
		{
			super();
			parent = p;
		}

        @Nullable
        @Override
        public BakedModel resolve(
          final @NotNull BakedModel p_173465_,
          final @NotNull ItemStack p_173466_,
          @Nullable final ClientLevel p_173467_,
          @Nullable final LivingEntity p_173468_,
          final int p_173469_)
        {
            return parent.resolve(p_173465_, p_173466_, p_173467_, p_173468_);
        }
	}

	public BaseSmartModel()
	{
		overrides = new OverrideHelper( this );
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	@NotNull
    @Override
	public TextureAtlasSprite getParticleIcon()
	{
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon( Blocks.STONE.defaultBlockState() );
	}

	@NotNull
    @Override
	public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

    @Override
	@NotNull
    public List<BakedQuad> getQuads(
			@Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        final BakedModel model = handleBlockState( state, rand, extraData );
		if (model instanceof IDataAwareBakedModel dataAwareBakedModel)
			return dataAwareBakedModel.getQuads(state, side, rand, extraData);

        return model.getQuads( state, side, rand);
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand)
    {
        final BakedModel model = handleBlockState( state, rand );
        return model.getQuads( state, side, rand );
    }

	public BakedModel handleBlockState(
			final BlockState state,
			final Random rand )
	{
		return NullBakedModel.instance;
	}

	public BakedModel handleBlockState(
	  final BlockState state,
      final Random random,
      final IModelData modelData
    )
    {
        return NullBakedModel.instance;
    }

	@NotNull
    @Override
	public ItemOverrides getOverrides()
	{
		return overrides;
	}

	public BakedModel resolve(
			final BakedModel originalModel,
			final ItemStack stack,
			final Level world,
			final LivingEntity entity )
	{
		return originalModel;
	}

}
