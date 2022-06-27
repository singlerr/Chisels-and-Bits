package mod.chiselsandbits.forge.platform.block;

import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.forge.client.block.ForgeChiseledBlockRenderProperties;
import net.minecraftforge.client.IBlockRenderProperties;

import java.util.function.Consumer;

public class ForgeChiseledBlock extends ChiseledBlock
{
    public ForgeChiseledBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IBlockRenderProperties> consumer)
    {
        consumer.accept(new ForgeChiseledBlockRenderProperties());
    }
}
