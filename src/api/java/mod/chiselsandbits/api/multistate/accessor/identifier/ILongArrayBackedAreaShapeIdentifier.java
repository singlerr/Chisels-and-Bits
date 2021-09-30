package mod.chiselsandbits.api.multistate.accessor.identifier;

/**
 * Indicates that the {@link IAreaShapeIdentifier} is backed by a long array and as such its core data can directly be used to
 * compare the identifiers.
 */
public interface ILongArrayBackedAreaShapeIdentifier extends IAreaShapeIdentifier
{

    /**
     * Gives access to the backing long array.
     * @return The backing long array.
     */
    long[] getBackingData();
}
