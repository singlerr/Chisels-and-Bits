package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public final class ModChiselModeGroups
{

    private ModChiselModeGroups()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModChiselModeGroups. This is a utility class");
    }

    public static IToolModeGroup CUBED = new IToolModeGroup() {
        @Override
        public ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/cube_medium.png");
        }

        @Override
        public Component getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.cubed"
            );
        }
    };

    public static IToolModeGroup CUBED_ALIGNED = new IToolModeGroup() {
        @Override
        public ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/snap4.png");
        }

        @Override
        public Component getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.snap"
            );
        }
    };

    public static IToolModeGroup LINE = new IToolModeGroup() {
        @Override
        public ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/line.png");
        }

        @Override
        public Component getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.line"
            );
        }
    };

    public static IToolModeGroup PLANE = new IToolModeGroup() {
        @Override
        public ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/plane.png");
        }

        @Override
        public Component getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.plane"
            );
        }
    };

    public static IToolModeGroup PLANE_FILTERED = new IToolModeGroup() {
        @Override
        public ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/connected_material.png");
        }

        @Override
        public Component getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.connected_material"
            );
        }
    };


    public static IToolModeGroup SPHERE = new IToolModeGroup() {
        @Override
        public ResourceLocation getIcon()
        {
            return new ResourceLocation(Constants.MOD_ID,"textures/icons/sphere_medium.png");
        }

        @Override
        public Component getDisplayName()
        {
            return TranslationUtils.build(
              "chiselmode.sphere"
            );
        }
    };
}
