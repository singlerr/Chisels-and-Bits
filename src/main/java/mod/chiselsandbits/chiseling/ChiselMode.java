package mod.chiselsandbits.chiseling;

import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.item.leftclick.LeftClickProcessingState;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.util.IWithDisplayName;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ChiselMode extends ForgeRegistryEntry<IChiselMode> implements IWithDisplayName, IChiselMode
{

    public static final IChiselMode SINGLE             = new ChiselMode(LocalStrings.ChiselModeSingle);
    public static final IChiselMode SNAP2              = new ChiselMode(LocalStrings.ChiselModeSnap2);
    public static final IChiselMode SNAP4              = new ChiselMode(LocalStrings.ChiselModeSnap4);
    public static final IChiselMode SNAP8              = new ChiselMode(LocalStrings.ChiselModeSnap8);
    public static final IChiselMode LINE               = new ChiselMode(LocalStrings.ChiselModeLine);
    public static final IChiselMode PLANE              = new ChiselMode(LocalStrings.ChiselModePlane);
    public static final IChiselMode CONNECTED_PLANE    = new ChiselMode(LocalStrings.ChiselModeConnectedPlane);
    public static final IChiselMode CUBE_SMALL         = new ChiselMode(LocalStrings.ChiselModeCubeSmall);
    public static final IChiselMode CUBE_MEDIUM        = new ChiselMode(LocalStrings.ChiselModeCubeMedium);
    public static final IChiselMode CUBE_LARGE         = new ChiselMode(LocalStrings.ChiselModeCubeLarge);
    public static final IChiselMode SAME_MATERIAL      = new ChiselMode(LocalStrings.ChiselModeSameMaterial);
    public static final IChiselMode DRAWN_REGION       = new ChiselMode(LocalStrings.ChiselModeDrawnRegion);
    public static final IChiselMode CONNECTED_MATERIAL = new ChiselMode(LocalStrings.ChiselModeConnectedMaterial);

    private final ITextComponent displayName;

    ChiselMode(final ITextComponent displayName) {this.displayName = displayName;}

    ChiselMode(final TextFormatting formatting, final LocalStrings translationKey)
    {
        this(new TranslationTextComponent(translationKey.toString()).mergeStyle(formatting));
    }

    ChiselMode(final LocalStrings translationKey)
    {
        this(new TranslationTextComponent(translationKey.toString()));
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    @Override
    public LeftClickProcessingState onLeftClickBy(
      final PlayerEntity playerEntity, final IChiselingContext context)
    {
        return null;
    }

    @Override
    public void onStoppedLeftClicking(final PlayerEntity playerEntity, final IChiselingContext context)
    {

    }

    @Override
    public IAreaAccessor getCurrentAccessor(final IChiselingContext context)
    {
        return null;
    }
}
