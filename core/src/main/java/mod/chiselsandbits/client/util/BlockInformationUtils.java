package mod.chiselsandbits.client.util;

import com.communi.suggestu.scena.core.client.models.data.IBlockModelData;
import com.communi.suggestu.scena.core.client.rendering.IRenderingManager;
import com.communi.suggestu.scena.core.client.rendering.type.IRenderTypeManager;
import com.google.common.collect.Sets;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
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

    public static Set<RenderType> extractRenderTypes(BlockInformation blockInformation) {
        return extractRenderTypes(Sets.newHashSet(blockInformation));
    }

    public static Set<RenderType> extractRenderTypes(Set<BlockInformation> blocks) {
        final Set<RenderType> renderTypes = Sets.newHashSet();
        for (BlockInformation blockInformation : blocks) {
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
                            IBlockModelData.empty()
                    )
            );
        }

        return renderTypes;
    }
}
