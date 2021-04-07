package mod.chiselsandbits.client.model.baked.simple;

import mod.chiselsandbits.client.model.baked.base.BaseBakedBlockModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SuppressWarnings("deprecation")
public class CombinedModel extends BaseBakedBlockModel
{

    private static final Random COMBINED_RANDOM_MODEL = new Random();

    IBakedModel[] merged;

    List<BakedQuad>[] face;
    List<BakedQuad>   generic;

    boolean isSideLit;

    @SuppressWarnings( "unchecked" )
    public CombinedModel(
      final IBakedModel... args )
    {
        face = new ArrayList[Direction.values().length];

        generic = new ArrayList<>();
        for ( final Direction f : Direction.values() )
        {
            face[f.ordinal()] = new ArrayList<>();
        }

        merged = args;

        for ( final IBakedModel m : merged )
        {
            generic.addAll( m.getQuads( null, null, COMBINED_RANDOM_MODEL ) );
            for ( final Direction f : Direction.values() )
            {
                face[f.ordinal()].addAll( m.getQuads( null, f, COMBINED_RANDOM_MODEL ) );
            }
        }

        isSideLit = Arrays.stream(args).anyMatch(IBakedModel::isSideLit);
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleTexture()
    {
        for ( final IBakedModel a : merged )
        {
            return a.getParticleTexture();
        }

        return Minecraft.getInstance().getAtlasSpriteGetter(
          PlayerContainer.LOCATION_BLOCKS_TEXTURE
        ).apply(MissingTextureSprite.getLocation());
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(
      @Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand, @NotNull final IModelData extraData)
    {
        if ( side != null )
        {
            return face[side.ordinal()];
        }

        return generic;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand)
    {
        if ( side != null )
        {
            return face[side.ordinal()];
        }

        return generic;
    }

    @Override
    public boolean isSideLit()
    {
        return isSideLit;
    }
}