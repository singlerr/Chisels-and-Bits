package mod.chiselsandbits.client.util;

import com.communi.suggestu.scena.core.client.models.data.IBlockModelData;
import com.communi.suggestu.scena.core.client.rendering.type.IRenderTypeManager;
import com.google.common.collect.Sets;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;

import java.util.Set;

public final class BlockInformationUtils {

    private static final RandomSource RANDOM = RandomSource.create(42);

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
