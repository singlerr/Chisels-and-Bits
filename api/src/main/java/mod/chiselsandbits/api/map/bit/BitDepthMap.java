package mod.chiselsandbits.api.map.bit;

import com.mojang.logging.LogUtils;
import mod.chiselsandbits.api.util.Vector2i;
import net.minecraft.core.Vec3i;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A heightmap specially for processing bit heights;
 */
public class BitDepthMap implements mod.chiselsandbits.api.map.bit.IBitDepthMap {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final double[] data;

    private final Vec3i size;

    public BitDepthMap(IDepthMapBuilder builder) {
        this.size = builder.getSize();
        this.data = new double[this.size.getX() * this.size.getY()];
        //Arrays.fill(this.data, Double.NEGATIVE_INFINITY);

        builder.getEntries().forEach(entry -> {
            final int index = entry.depthMapPosition().getX() + entry.depthMapPosition().getY() * this.size.getX();
            if (index >= 0 && index < this.data.length) {
                this.data[index] = entry.depth();
            }
            else {
                LOGGER.error("Invalid index for bit height map entry: " + entry);
            }
        });
    }

    public BitDepthMap(BitDepthMap source) {
        this.size = source.getSize();
        this.data = Arrays.copyOf(source.data, source.data.length);
    }

    @Override
    public double getDepth(final int x, final int y) {
        return this.data[x + y * this.size.getX()];
    }

    @Override
    public double getDepth(final Vector2i position) {
        return this.getDepth(position.getX(), position.getY());
    }

    @Override
    public void applyFilter(IDepthMapFilter filter, int iterations) {
        for (int i = 0; i < iterations; i++) {
            final double[] newData = new double[this.data.length];
            for (int x = 0; x < this.size.getX(); x++) {
                for (int y = 0; y < this.size.getY(); y++) {
                    final Vector2i position = new Vector2i(x, y);
                    final double depth = this.getDepth(position);
                    final double newDepth = filter.applyFilter(this, position, depth, 0.5d);
                    newData[x + y * this.size.getX()] = newDepth;
                }
            }

            System.arraycopy(newData, 0, this.data, 0, newData.length);
        }
    }

    @Override
    public Vec3i getSize() {
        return size;
    }

    @Override
    public Stream<IDepthMapEntry> getEntries() {
        return IntStream.range(0, this.data.length).mapToObj(index -> {
            final int x = index % this.size.getX();
            final int y = index / this.size.getX();
            return new DepthMapEntry(new Vector2i(x, y), this.data[index]);
        });
    }

    @Override
    public void subtract(final IBitDepthMap other) {
        if (this.getSize() != other.getSize())
            throw new IllegalArgumentException("Cannot subtract two bit depth maps with different sizes.");

        for (int index = 0; index < this.data.length; index++) {
            this.data[index] -= other.getDepth(index % this.size.getX(), index / this.size.getX());
        }
    }

    @Override
    public IBitDepthMap snapshot() {
        return new BitDepthMap(this);
    }
}
