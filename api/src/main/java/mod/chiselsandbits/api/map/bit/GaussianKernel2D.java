package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

public class GaussianKernel2D extends SimpleKernel {

    public GaussianKernel2D(int radius, double sigma) {
        super(
                new Vector2i(radius * 2 + 1, radius * 2 + 1),
                createKernel(radius, sigma)
        );
    }

    private static double[] createKernel(int radius, double sigma) {
        int diameter = radius * 2 + 1;
        double[] data = new double[diameter * diameter];

        double sigma22 = 2 * sigma * sigma;
        double constant = Math.PI * sigma22;
        for (int y = -radius; y <= radius; ++y) {
            for (int x = -radius; x <= radius; ++x) {
                data[(y + radius) * diameter + x + radius] = (float) (Math.exp(-(x * x + y * y) / sigma22) / constant);
            }
        }

        return data;
    }
}
