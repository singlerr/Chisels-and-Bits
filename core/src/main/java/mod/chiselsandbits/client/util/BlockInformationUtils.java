package mod.chiselsandbits.client.util;

import com.communi.suggestu.scena.core.client.rendering.type.IRenderTypeManager;
import com.google.common.collect.Sets;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.client.variant.state.IClientStateVariantManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.ThreadSafeLegacyRandomSource;

import java.util.Set;

@SuppressWarnings("deprecation")
public final class BlockInformationUtils {

    private static final RandomSource RANDOM = new ThreadSafeLegacyRandomSource(42);

    private BlockInformationUtils() {
        throw new IllegalStateException("Can not instantiate an instance of: IMultiStateObjectStatisticsUtils. This is a utility class");
    }

    public static Set<RenderType> extractRenderTypes(IBlockInformation blockInformation) {
        return extractRenderTypes(Sets.newHashSet(blockInformation));
    }

    public static Set<RenderType> extractRenderTypes(Set<IBlockInformation> blocks) {
        final Set<RenderType> renderTypes = Sets.newHashSet();
        for (IBlockInformation blockInformation : blocks) {
            if (blockInformation.isAir())
                continue;

            if (blockInformation.isFluid()) {
                renderTypes.add(ItemBlockRenderTypes.getRenderLayer(blockInformation.getBlockState().getFluidState()));
                continue;
            }

            final BakedModel bakedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockInformation.getBlockState());
            renderTypes.addAll(
                    IRenderTypeManager.getInstance().getRenderTypesFor(
                            bakedModel,
                            blockInformation.getBlockState(),
                            RANDOM,
                            IClientStateVariantManager.getInstance().getBlockModelData(blockInformation)
                    )
            );
        }

        return renderTypes;
    }
}
