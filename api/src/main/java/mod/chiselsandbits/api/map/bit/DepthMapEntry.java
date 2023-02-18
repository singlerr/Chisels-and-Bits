package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

public record DepthMapEntry(Vector2i depthMapPosition, double depth) implements IDepthMapEntry {
}
