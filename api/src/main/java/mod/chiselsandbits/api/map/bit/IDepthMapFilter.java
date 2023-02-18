package mod.chiselsandbits.api.map.bit;

import mod.chiselsandbits.api.util.Vector2i;

public interface IDepthMapFilter {
    double applyFilter(IBitDepthMap bitHeightMap, Vector2i position, double depth, double offset);
}
