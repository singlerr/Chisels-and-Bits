package mod.chiselsandbits.forge.data.tag;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

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
        final TagsProvider.TagAppender<Block> builder = this.tag(
          mode == Mode.FORCED ?
            IChiselsAndBitsAPI.getInstance().getForcedTag() :
            IChiselsAndBitsAPI.getInstance().getBlockedTag()
        );

        addElements(builder);
    }

    protected abstract void addElements(final TagsProvider.TagAppender<Block> builder);

    @Override
    public @NotNull String getName()
    {
        return StringUtils.capitalize(mode.toString().toLowerCase(Locale.ROOT)) + " chiselable tag generator";
    }
}
