package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

/**
 * Defines a kernel which can be used by kernel based filters on depth maps.
 */
public interface IKernel {
    /**
     * The size of the kernel.
     * @return The size of the kernel.
     */
    Vector2i size();

    /**
     * The origin of the kernel.
     * @return The origin of the kernel.
     */
    Vector2i origin();

    /**
     * The matrix (also known as the data payload) of the kernel.
     *
     * @return The matrix of the kernel.
     */
    double[] matrix();
}
