package mod.chiselsandbits.api.util.constants;

public class NbtConstants
{


    private NbtConstants()
    {
        throw new IllegalStateException("Can not instantiate an instance of: NbtConstants. This is a utility class");
    }


    public static final String CHISEL_BLOCK_ENTITY_DATA = "chiselBlockData";
    public static final String COMPRESSED_STORAGE = "compressedStorage";
    public static final String PALETTE = "palette";
    public static final String BLOCK_STATES  = "blockStates";
    public static final String PRIMARY_STATE = "primaryState";
    public static final String BLOCK_STATE = "blockState";
    public static final String COUNT = "count";
    public static final String STATISTICS = "statistics";
    public static final String TOTAL_BLOCK_COUNT = "blockCount";
    public static final String TOTAL_SHOULD_CHECK_WEAK_POWER_COUNT = "blockShouldCheckWeakPowerCount";
}
