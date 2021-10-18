package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

public final class ModChiselModeGroups
{

    private ModChiselModeGroups()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModChiselModeGroups. This is a utility class");
    }

    public static IToolModeGroup CUBED = new IToolModeGroup() {
        @Override
        public @NotNull ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/cube_medium.png");
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.cubed"
            );
        }
    };

    public static IToolModeGroup CUBED_ALIGNED = new IToolModeGroup() {
        @Override
        public @NotNull ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/snap4.png");
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.snap"
            );
        }
    };

    public static IToolModeGroup LINE = new IToolModeGroup() {
        @Override
        public @NotNull ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/line.png");
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.line"
            );
        }
    };

    public static IToolModeGroup PLANE = new IToolModeGroup() {
        @Override
        public @NotNull ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/plane.png");
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.plane"
            );
        }
    };

    public static IToolModeGroup PLANE_FILTERED = new IToolModeGroup() {
        @Override
        public @NotNull ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/connected_material.png");
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.connected_material"
            );
        }
    };

    public static IToolModeGroup SPHERE = new IToolModeGroup() {
        @Override
        public @NotNull ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/sphere_medium.png");
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.sphere"
            );
        }
    };

    public static IToolModeGroup CONNECTED_PLANE = new IToolModeGroup() {
        @Override
        public @NotNull ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/connected_plane.png");
        }

        @Override
        public ITextComponent getDisplayName()
        {
            return LocalStrings.ChiselModeConnectedPlane.getText();
        }
    };
}
