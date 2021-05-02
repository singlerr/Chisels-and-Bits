package mod.chiselsandbits.client.model.baked.chiseled;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.client.culling.ICullTest;
import mod.chiselsandbits.client.culling.MCCullTest;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

import java.security.InvalidParameterException;
import java.util.Collection;

public enum ChiselRenderType
{
    SOLID( RenderType.getSolid(), VoxelType.SOLID ),
    SOLID_FLUID( RenderType.getSolid(), VoxelType.FLUID ),
    CUTOUT( RenderType.getCutout(), VoxelType.UNKNOWN ),
    CUTOUT_MIPPED( RenderType.getCutoutMipped(), VoxelType.UNKNOWN ),
    TRANSLUCENT( RenderType.getTranslucent(), VoxelType.UNKNOWN ),
    TRANSLUCENT_FLUID( RenderType.getTranslucent(), VoxelType.FLUID ),
    TRIPWIRE (RenderType.getTripwire(), VoxelType.UNKNOWN);

    public final RenderType layer;
    public final VoxelType type;

    private static final Multimap<VoxelType, ChiselRenderType> TYPED_RENDER_TYPES = HashMultimap.create();
    static {
        for (final ChiselRenderType value : values())
        {
            TYPED_RENDER_TYPES.put(value.type, value);
        }
    }

    ChiselRenderType(
      final RenderType layer,
      final VoxelType type)
    {
        this.layer = layer;
        this.type = type;
    }

    public boolean isRequiredForRendering(
      final IAreaAccessor accessor )
    {
        if ( accessor == null )
        {
            return false;
        }

        return accessor.stream()
          .anyMatch(this::isRequiredForRendering);
    }

    public boolean isRequiredForRendering(
      final IStateEntryInfo stateEntryInfo )
    {
        return this.type.isValidBlockState(stateEntryInfo.getState()) && RenderTypeLookup.canRenderInLayer(stateEntryInfo.getState(), this.layer);
    }

    public static ChiselRenderType fromLayer(
      RenderType layerInfo,
      final boolean isFluid )
    {
        if (layerInfo == null)
            layerInfo = RenderType.getSolid();

        if (ChiselRenderType.CUTOUT.layer.equals(layerInfo))
        {
            return CUTOUT;
        }
        else if (ChiselRenderType.CUTOUT_MIPPED.layer.equals(layerInfo))
        {
            return CUTOUT_MIPPED;
        }
        else if (ChiselRenderType.SOLID.layer.equals(layerInfo))
        {
            return isFluid ? SOLID_FLUID : SOLID;
        }
        else if (ChiselRenderType.TRANSLUCENT.layer.equals(layerInfo))
        {
            return isFluid ? TRANSLUCENT_FLUID : TRANSLUCENT;
        }
        else if (ChiselRenderType.TRIPWIRE.layer.equals(layerInfo))
        {
            return TRIPWIRE;
        }

        throw new InvalidParameterException();
    }

    public static Collection<ChiselRenderType> getRenderTypes(final VoxelType voxelType) {
        return TYPED_RENDER_TYPES.get(voxelType);
    }

    public ICullTest getTest()
    {
        return new MCCullTest();
    }

}
