package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

public interface IDepthMapEntry {
    Vector2i depthMapPosition();

    double depth();
}
