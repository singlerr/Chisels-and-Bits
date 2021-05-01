package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.Property;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.Optional;

public class StringStateUtils
{

    private static final Logger LOGGER = LogManager.getLogger();

    private StringStateUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: StringStateUtils. This is a utility class");
    }

    public static int getStateIDFromName(
      final String name )
    {
        final String parts[] = name.split( "[?&]" );

        try
        {
            parts[0] = URLDecoder.decode( parts[0], "UTF-8" );
        }
        catch ( final UnsupportedEncodingException e )
        {
            LOGGER.error( "Failed to reload Property from store data : " + name, e );
        }

        final Block blk = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(parts[0]));

        if ( blk == null || blk == Blocks.AIR )
        {
            return 0;
        }

        BlockState state = blk.getDefaultState();

        // rebuild state...
        for ( int x = 1; x < parts.length; ++x )
        {
            try
            {
                if ( parts[x].length() > 0 )
                {
                    final String[] nameValues = parts[x].split( "[=]" );
                    if ( nameValues.length == 2 )
                    {
                        nameValues[0] = URLDecoder.decode( nameValues[0], "UTF-8" );
                        nameValues[1] = URLDecoder.decode( nameValues[1], "UTF-8" );

                        state = withState( state, blk, nameValues );
                    }
                }
            }
            catch ( final Exception err )
            {
                LOGGER.error( "Failed to reload Property from store data : " + name, err );
            }
        }

        return IBlockStateIdManager.getInstance().getIdFrom( state );
    }

    private static BlockState withState(
      final BlockState state,
      final Block blk,
      final String[] nameval )
    {
        final Property<?> prop = blk.getStateContainer().getProperty(nameval[0]);
        if ( prop == null )
        {
            LOGGER.info( nameval[0] + " is not a valid property for " + blk.getRegistryName() );
            return state;
        }

        return setPropValue(state, prop, nameval[1]);
    }

    public static <T extends Comparable<T>> BlockState setPropValue(BlockState blockState, Property<T> property, String value) {
        final Optional<T> pv = property.parseValue( value );
        if ( pv.isPresent() )
        {
            return blockState.with( property, pv.get());
        }
        else
        {
            LOGGER.info( value + " is not a valid value of " + property.getName() + " for " + blockState.getBlock().getRegistryName() );
            return blockState;
        }
    }

    public static String getNameFromStateID(
      final int key )
    {
        final BlockState state = IBlockStateIdManager.getInstance().getBlockStateFrom( key );
        final Block blk = state.getBlock();

        String sname = "air?";

        try
        {
            final StringBuilder stateName = new StringBuilder( URLEncoder.encode( Objects.requireNonNull(blk.getRegistryName()).toString(), "UTF-8" ) );
            stateName.append( '?' );

            boolean first = true;
            for ( final Property<?> p : state.getBlock().getStateContainer().getProperties() )
            {
                if ( !first )
                {
                    stateName.append( '&' );
                }

                first = false;

                final Comparable<?> propVal = state.get(p);

                String saveAs;
                if ( propVal instanceof IStringSerializable)
                {
                    saveAs = ( (IStringSerializable) propVal ).getString();
                }
                else
                {
                    saveAs = propVal.toString();
                }

                stateName.append( URLEncoder.encode( p.getName(), "UTF-8" ) );
                stateName.append( '=' );
                stateName.append( URLEncoder.encode( saveAs, "UTF-8" ) );
            }

            sname = stateName.toString();
        }
        catch ( final UnsupportedEncodingException e )
        {
            LOGGER.error( "Failed to Serialize State", e );
        }

        return sname;
    }
}
