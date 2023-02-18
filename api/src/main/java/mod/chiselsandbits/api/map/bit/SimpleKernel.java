package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

public class SimpleKernel implements IKernel {

    private final Vector2i size;
    private final Vector2i origin;
    private final double[] matrix;

    public SimpleKernel(Vector2i size, double[] matrix) {
        this.size = size;
        this.origin = new Vector2i((size.getX() - 1) >> 1, (size.getY() - 1) >> 1);
        this.matrix = matrix;
    }

    @Override
    public final Vector2i size() {
        return size;
    }

    @Override
    public final Vector2i origin() {
        return origin;
    }

    @Override
    public final double[] matrix() {
        return matrix;
    }
}
