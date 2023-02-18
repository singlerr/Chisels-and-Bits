package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

public class KernelBasedBitDepthMapFilter implements IDepthMapFilter {

    private final IKernel kernel;

    public KernelBasedBitDepthMapFilter(IKernel kernel) {
        this.kernel = kernel;
    }

    @Override
    public double applyFilter(IBitDepthMap bitHeightMap, Vector2i position, double depth, double offset) {
        return calculateDepth(bitHeightMap, position, offset);
    }

    private double calculateDepth(IBitDepthMap map, Vector2i position, double offset) {
        final Vector2i mapSize = new Vector2i(map.getSize().getX(), map.getSize().getY());
        final Vector2i kernelSize = kernel.size();
        final Vector2i kernelOrigin = kernel.origin();
        final double[] matrix = kernel.matrix();

        float z = 0;

        for (int ky = 0; ky < kernelSize.getY(); ++ky) {
            int offsetY = position.getY() + ky - kernelOrigin.getY();
            // Clamp coordinates inside data
            if (offsetY < 0 || offsetY >= mapSize.getY()) {
                offsetY = position.getY();
            }

            int matrixOffset = ky * kernelSize.getX();
            for (int kx = 0; kx < kernelSize.getX(); ++kx) {
                double f = matrix[matrixOffset + kx];
                if (f == 0) {
                    continue;
                }

                int offsetX = position.getX() + kx - kernelOrigin.getX();
                // Clamp coordinates inside data
                if (offsetX < 0 || offsetX >= mapSize.getX()) {
                    offsetX = position.getX();
                }

                z += f * map.getDepth(offsetX, offsetY);
            }
        }
        return z + offset;
    }

}
