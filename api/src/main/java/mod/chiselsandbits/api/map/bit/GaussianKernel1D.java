package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

public class GaussianKernel1D extends SimpleKernel {

    public enum KernelAxis {
        VERTICAL,
        HORIZONTAL;

        private Vector2i createSize(final int radius) {
            return switch (this) {
                case VERTICAL -> new Vector2i(1, radius * 2 + 1);
                case HORIZONTAL -> new Vector2i(radius * 2 + 1, 1);
            };
        }
    }

    public GaussianKernel1D(int radius, double sigma, KernelAxis axis) {
        super(
                axis.createSize(radius),
                createKernel(radius, sigma)
        );
    }

    private static double[] createKernel(int radius, double sigma) {
        int diameter = radius * 2 + 1;
        double[] data = new double[diameter];

        double sigma22 = 2 * sigma * sigma;
        double constant = Math.PI * sigma22;
        for (int x = -radius; x <= radius; ++x) {
            data[x + radius] = (float) (Math.exp(-(x * x) / sigma22) / constant);
        }

        return data;
    }
}
