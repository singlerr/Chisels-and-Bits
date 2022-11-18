package mod.chiselsandbits.client.model.baked.bit;

import com.mojang.math.Vector3f;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.model.baked.base.BaseBakedBlockModel;
import mod.chiselsandbits.client.model.baked.face.FaceManager;
import mod.chiselsandbits.client.model.baked.face.model.ModelQuadLayer;
import mod.chiselsandbits.client.util.BlockInformationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitBlockBakedModel extends BaseBakedBlockModel
{
    public static final float PIXELS_PER_BLOCK = 16.0f;

    private static final float BIT_BEGIN = 6.0f;
    private static final float BIT_END   = 10.0f;

    final List<BakedQuad> generic = new ArrayList<>(6);

    public BitBlockBakedModel(
      final BlockInformation blockInformation)
    {
        final FaceBakery faceBakery = new FaceBakery();

        final Vector3f to = new Vector3f(BIT_BEGIN, BIT_BEGIN, BIT_BEGIN);
        final Vector3f from = new Vector3f(BIT_END, BIT_END, BIT_END);

        final BlockModelRotation mr = BlockModelRotation.X0_Y0;

        for (final Direction myFace : Direction.values())
        {
            for (final RenderType layer : BlockInformationUtils.extractRenderTypes(blockInformation))
            {
                final ModelQuadLayer[] layers = FaceManager.getInstance().getCachedFace(blockInformation, myFace, layer, 0, layer);

                if (layers == null || layers.length == 0)
                {
                    continue;
                }

                for (final ModelQuadLayer quadLayer : layers)
                {
                    final BlockFaceUV uv = new BlockFaceUV(getFaceUvs(myFace), 0);
                    final BlockElementFace bpf = new BlockElementFace(myFace, 0, "", uv);

                    Vector3f toB, fromB;

                    switch (myFace)
                    {
                        case UP -> {
                            toB = new Vector3f(to.x(), from.y(), to.z());
                            fromB = new Vector3f(from.x(), from.y(), from.z());
                        }
                        case EAST -> {
                            toB = new Vector3f(from.x(), to.y(), to.z());
                            fromB = new Vector3f(from.x(), from.y(), from.z());
                        }
                        case NORTH -> {
                            toB = new Vector3f(to.x(), to.y(), to.z());
                            fromB = new Vector3f(from.x(), from.y(), to.z());
                        }
                        case SOUTH -> {
                            toB = new Vector3f(to.x(), to.y(), from.z());
                            fromB = new Vector3f(from.x(), from.y(), from.z());
                        }
                        case DOWN -> {
                            toB = new Vector3f(to.x(), to.y(), to.z());
                            fromB = new Vector3f(from.x(), to.y(), from.z());
                        }
                        case WEST -> {
                            toB = new Vector3f(to.x(), to.y(), to.z());
                            fromB = new Vector3f(to.x(), from.y(), from.z());
                        }
                        default -> throw new NullPointerException();
                    }

                    generic.add(faceBakery.bakeQuad(toB, fromB, bpf, quadLayer.getSprite(), myFace, mr, null, false, new ResourceLocation(Constants.MOD_ID, "bit")));
                }
            }
        }
    }

    private float[] getFaceUvs(
      final Direction face)
    {
        float[] afloat;

        final int from_x = 7;
        final int from_y = 7;
        final int from_z = 7;

        final int to_x = 8;
        final int to_y = 8;
        final int to_z = 8;

        afloat = switch (face)
                   {
                       case DOWN, UP -> new float[] {from_x, from_z, to_x, to_z};
                       case NORTH, SOUTH -> new float[] {from_x, PIXELS_PER_BLOCK - to_y, to_x, PIXELS_PER_BLOCK - from_y};
                       case WEST, EAST -> new float[] {from_z, PIXELS_PER_BLOCK - to_y, to_z, PIXELS_PER_BLOCK - from_y};
                   };

        return afloat;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final RandomSource rand)
    {
        if (side != null)
        {
            return Collections.emptyList();
        }

        return generic;
    }

    @Override
    public boolean usesBlockLight()
    {
        return true;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
    }
}
