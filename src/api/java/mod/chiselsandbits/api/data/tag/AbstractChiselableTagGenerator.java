package mod.chiselsandbits.api.data.tag;

import com.ldtteam.datagenerators.tags.TagJson;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractChiselableTagGenerator extends BlockTagsProvider
{

    protected AbstractChiselableTagGenerator(
      final DataGenerator generator,
      final ExistingFileHelper existingFileHelper,
      final Mode mode)
    {
        super(generator, Constants.MOD_ID, existingFileHelper);
        this.mode = mode;
    }

    public enum Mode
    {
        FORCED,
        BLOCKED
    }

    private final Mode        mode;

    @Override
    protected void addTags()
    {
        final TagsProvider.Builder<Block> builder = this.tag(
          mode == Mode.FORCED ?
            IChiselsAndBitsAPI.getInstance().getForcedTag() :
            IChiselsAndBitsAPI.getInstance().getBlockedTag()
        );

        addElements(builder);
    }

    protected abstract void addElements(final TagsProvider.Builder<Block> builder);

    @Override
    public @NotNull String getName()
    {
        return StringUtils.capitalize(mode.toString().toLowerCase(Locale.ROOT)) + " chiselable tag generator";
    }
}
