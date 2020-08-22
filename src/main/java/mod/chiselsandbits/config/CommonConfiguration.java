package mod.chiselsandbits.config;

import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class CommonConfiguration extends AbstractConfiguration
{

    public ForgeConfigSpec.BooleanValue enableHelp;

    public CommonConfiguration(ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "help");

        enableHelp = defineBoolean(builder, "help.enabled", true);

        finishCategory(builder);
    }


    public void helpText(
      final LocalStrings string,
      final List<ITextComponent> tooltip,
      final String... variables )
    {
        if ( enableHelp.get() )
        {
            int varOffset = 0;

            final String[] lines = string.getLocal().split( ";" );
            for ( String a : lines )
            {
                while ( a.contains( "{}" ) && variables.length > varOffset )
                {
                    final int offset = a.indexOf( "{}" );
                    if ( offset >= 0 )
                    {
                        final String pre = a.substring( 0, offset );
                        final String post = a.substring( offset + 2 );
                        a = new StringBuilder( pre ).append( variables[varOffset++] ).append( post ).toString();
                    }
                }

                tooltip.add( new StringTextComponent(a));
            }
        }
    }
}
