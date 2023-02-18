package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

/**
 * Defines a depth map for bits.
 */
public interface IBitDepthMap extends IDepthMapBuilder {

    /**
     * Returns the depth at the given position.
     *
     * @param x The x position.
     * @param y The y position.
     * @return The depth at the given position.
     */
    double getDepth(int x, int y);

    /**
     * Returns the depth at the given position.
     *
     * @param position The position.
     * @return The depth at the given position.
     */
    double getDepth(Vector2i position);

    /**
     * Applies the given filter exactly once.
     *
     * @param filter The filter to apply.
     */
    default void applyFilter(IDepthMapFilter filter) {
        applyFilter(filter, 1);
    }

    /**
     * Applies the given filter the given amount of times.
     *
     * @param filter The filter to apply.
     * @param iterations The amount of times to apply the filter.
     */
    void applyFilter(IDepthMapFilter filter, int iterations);

    /**
     * Subtracts the given depth map from this one.
     *
     * @param other The other depth map.
     */
    void subtract(IBitDepthMap other);

    /**
     * Creates a deep copy as a snapshot of this depth map.
     *
     * @return The snapshot.
     */
    IBitDepthMap snapshot();
}
