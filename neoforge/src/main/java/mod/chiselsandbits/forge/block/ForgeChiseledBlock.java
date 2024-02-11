package mod.chiselsandbits.forge.block;

import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.forge.client.block.ForgeClientChiseledBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;


public class ForgeChiseledBlock extends ChiseledBlock
{
    public ForgeChiseledBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new ForgeClientChiseledBlockExtensions());
    }
}
