package mod.chiselsandbits.api.map.bit;


import mod.chiselsandbits.api.util.Vector2i;
import net.minecraft.core.Vec3i;

import java.util.stream.Stream;

/**
 * A depth map builder is a builder for a depth maps core information.
 */
public interface IDepthMapBuilder {

    /**
     * The size of the depth map.
     * @return The size of the depth map.
     */
    public Vec3i getSize();

    /**
     * The entries of the depth map.
     * @return The entries of the depth map.
     */
    public Stream<IDepthMapEntry> getEntries();
}
