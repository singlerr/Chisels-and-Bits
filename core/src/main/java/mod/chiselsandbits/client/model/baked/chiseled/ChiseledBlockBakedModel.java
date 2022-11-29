package mod.chiselsandbits.client.model.baked.chiseled;

import com.communi.suggestu.scena.core.client.models.IModelManager;
import com.google.common.collect.*;
import com.mojang.math.Vector3f;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.culling.ICullTest;
import mod.chiselsandbits.client.model.baked.base.BaseBakedBlockModel;
import mod.chiselsandbits.client.model.baked.face.FaceManager;
import mod.chiselsandbits.client.model.baked.face.FaceRegion;
import mod.chiselsandbits.client.model.baked.face.model.BakedQuadAdapter;
import mod.chiselsandbits.client.model.baked.face.model.ModelQuadLayer;
import mod.chiselsandbits.client.model.baked.face.model.VertexData;
import mod.chiselsandbits.client.util.Vector2f;
import mod.chiselsandbits.client.util.VectorUtils;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.utils.LightUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChiseledBlockBakedModel extends BaseBakedBlockModel {

    public static final ChiseledBlockBakedModel EMPTY = new ChiseledBlockBakedModel(
            BlockInformation.AIR,
            ChiselRenderType.SOLID,
            null,
            vector3d -> BlockInformation.AIR,
            0, RenderType.solid());

    private static final Direction[] X_Faces = new Direction[]{Direction.EAST, Direction.WEST};
    private static final Direction[] Y_Faces = new Direction[]{Direction.UP, Direction.DOWN};
    private static final Direction[] Z_Faces = new Direction[]{Direction.SOUTH, Direction.NORTH};

    private final ChiselRenderType chiselRenderType;

    // keep memory requirements low by using arrays.
    private BakedQuad[] up;
    private BakedQuad[] down;
    private BakedQuad[] north;
    private BakedQuad[] south;
    private BakedQuad[] east;
    private BakedQuad[] west;
    private BakedQuad[] generic;

    private List<BakedQuad> getList(
            final Direction side) {
        if (side != null) {
            switch (side) {
                case DOWN:
                    return asList(down);
                case EAST:
                    return asList(east);
                case NORTH:
                    return asList(north);
                case SOUTH:
                    return asList(south);
                case UP:
                    return asList(up);
                case WEST:
                    return asList(west);
                default:
            }
        }

        return asList(generic);
    }

    private List<BakedQuad> asList(
            final BakedQuad[] array) {
        if (array == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(array);
    }

    public ChiseledBlockBakedModel(
            final IBlockInformation state,
            final ChiselRenderType layer,
            final IAreaAccessor data,
            final Function<Vec3, IBlockInformation> neighborStateSupplier,
            final long primaryStateRenderSeed,
            @NotNull final RenderType renderType) {
        chiselRenderType = layer;
        BakedModel originalModel = null;

        if (state != null && !state.isAir()) {
            originalModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state.getBlockState());
            originalModel = IModelManager.getInstance().adaptToPlatform(originalModel);
        }

        if (originalModel != null && data != null) {
            final boolean shouldLayerRender;
            try (final IProfilerSection ignoredLayerCheck = ProfilingManager.getInstance().withSection("check")) {
                shouldLayerRender = layer.isRequiredForRendering(data);
            }

            if (shouldLayerRender) {
                final ChiseledBlockModelBuilder builder = new ChiseledBlockModelBuilder();
                try (final IProfilerSection ignoredFaceGeneration = ProfilingManager.getInstance().withSection("facegeneration")) {
                    generateFaces(builder, data, neighborStateSupplier, primaryStateRenderSeed, renderType);
                }

                // convert from builder to final storage.
                try (final IProfilerSection ignoredFinalize = ProfilingManager.getInstance().withSection("finalize")) {
                    up = builder.getSide(Direction.UP);
                    down = builder.getSide(Direction.DOWN);
                    east = builder.getSide(Direction.EAST);
                    west = builder.getSide(Direction.WEST);
                    north = builder.getSide(Direction.NORTH);
                    south = builder.getSide(Direction.SOUTH);
                    generic = builder.getSide(null);
                }
            }
        }
    }

    public boolean isEmpty() {
        boolean trulyEmpty = getList(null).isEmpty();

        for (final Direction e : Direction.values()) {
            trulyEmpty = trulyEmpty && getList(e).isEmpty();
        }

        return trulyEmpty;
    }

    private void generateFaces(
            final ChiseledBlockModelBuilder builder,
            final IAreaAccessor accessor,
            final Function<Vec3, IBlockInformation> neighborStateSupplier,
            final long primaryStateRenderSeed,
            @NotNull final RenderType renderType) {
        final List<List<FaceRegion>> resultingFaces = new ArrayList<>();

        try (final IProfilerSection ignoredFaceProcessing = ProfilingManager.getInstance().withSection("processing")) {
            try (final IProfilerSection ignoredXFaces = ProfilingManager.getInstance().withSection("x")) {
                processFaces(
                        accessor,
                        resultingFaces,
                        IPositionMutator.xzy(),
                        X_Faces,
                        Vec3::x,
                        Vec3::z,
                        neighborStateSupplier
                );
            }

            try (final IProfilerSection ignoredXFaces = ProfilingManager.getInstance().withSection("y")) {
                processFaces(
                        accessor,
                        resultingFaces,
                        IPositionMutator.zxy(),
                        Y_Faces,
                        Vec3::y,
                        Vec3::z,
                        neighborStateSupplier
                );
            }

            try (final IProfilerSection ignoredXFaces = ProfilingManager.getInstance().withSection("z")) {
                processFaces(
                        accessor,
                        resultingFaces,
                        IPositionMutator.zyx(),
                        Z_Faces,
                        Vec3::z,
                        Vec3::y,
                        neighborStateSupplier
                );
            }
        }

        try (final IProfilerSection ignoredFaceBuilding = ProfilingManager.getInstance().withSection("building")) {
            try (final IProfilerSection ignoredMerging = ProfilingManager.getInstance().withSection("merging")) {
                for (final List<FaceRegion> src : resultingFaces) {
                    mergeFaces(src);
                }
            }

            try (final IProfilerSection ignoredQuadGeneration = ProfilingManager.getInstance().withSection("quadGeneration")) {
                for (final List<FaceRegion> src : resultingFaces) {
                    for (final FaceRegion region : src) {
                        final Direction cullDirection = region.getFace();

                        final Vector3f from = region.minVector();
                        final Vector3f to = region.maxVector();
                        final Collection<ModelQuadLayer> quadLayers = FaceManager.getInstance().getCachedLayersFor(region.getBlockInformation(), cullDirection, chiselRenderType.layer, primaryStateRenderSeed, renderType);

                        if (quadLayers != null) {
                            for (final ModelQuadLayer layer : quadLayers) {

                                final Collection<VertexData> adaptedVertices = adaptVertices(layer.vertexData(), cullDirection, from, to);

                                final BakedQuadAdapter adapter = new BakedQuadAdapter(adaptedVertices, layer.color());
                                LightUtil.put(adapter, layer.sourceQuad());
                                adapter.setQuadTint(layer.tint());
                                adapter.setApplyDiffuseLighting(layer.shade());
                                adapter.setTexture(layer.sprite());
                                adapter.setQuadOrientation(cullDirection);
                                final BakedQuad quad = adapter.build();

                                // build it.
                                if (region.isEdge()) {
                                    builder.getList(cullDirection).add(quad);
                                } else {
                                    builder.getList(null).add(quad);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private Collection<VertexData> adaptVertices(VertexData[] vertexData, final Direction cullDirection, Vector3f from, Vector3f to) {
        final BiMap<VertexData, Vector2f> projectedVertexPositions = HashBiMap.create();
        Arrays.stream(vertexData).forEach(vertex -> projectedVertexPositions.put(vertex, vertex.projectOntoPlaneOf(cullDirection)));
        final Collection<Vector2f> boxCorners = buildCorners(cullDirection, from, to);
        final Map<Vector2f, VertexData> closestCorners = buildClosestVertices(vertexData, boxCorners, cullDirection);

        final InterpolationHelper interpolationHelper = new InterpolationHelper(
                projectedVertexPositions.get(vertexData[0]).x(),
                projectedVertexPositions.get(vertexData[0]).y(),
                projectedVertexPositions.get(vertexData[1]).x(),
                projectedVertexPositions.get(vertexData[1]).y(),
                projectedVertexPositions.get(vertexData[2]).x(),
                projectedVertexPositions.get(vertexData[2]).y(),
                projectedVertexPositions.get(vertexData[3]).x(),
                projectedVertexPositions.get(vertexData[3]).y()
        );

        final Collection<VertexData> adaptedVertices = new ArrayList<>();
        for (Vector2f newPosition : boxCorners) {
            interpolationHelper.locate(newPosition.x(), newPosition.y());
            final float u = interpolationHelper.interpolate(
                    vertexData[0].u(),
                    vertexData[1].u(),
                    vertexData[2].u(),
                    vertexData[3].u()
            );
            final float v = interpolationHelper.interpolate(
                    vertexData[0].v(),
                    vertexData[1].v(),
                    vertexData[2].v(),
                    vertexData[3].v()
            );

            final Vector3f position = VectorUtils.unprojectFromPlaneOf(newPosition, from, cullDirection);
            final VertexData vertex = new VertexData(position, new Vector2f(u,v), closestCorners.get(newPosition).vertexIndex());
            adaptedVertices.add(vertex);
        }

        return adaptedVertices;
    }

    private Map<Vector2f, VertexData> buildClosestVertices(final VertexData[] vertexData, final Collection<Vector2f> boxCorners, Direction cullDirection) {
        final Vector2f center = new Vector2f();
        for (final Vector2f corner : boxCorners) {
            center.add(corner);
        }
        center.mul(1f / boxCorners.size());

        final Vector2f centerOfVertices = new Vector2f();
        for (final VertexData vertex : vertexData) {
            centerOfVertices.add(vertex.projectOntoPlaneOf(cullDirection));
        }
        centerOfVertices.mul(1f / vertexData.length);

        enum Corner {
            TOP_LEFT,
            TOP_RIGHT,
            BOTTOM_LEFT,
            BOTTOM_RIGHT;

            private static Corner closest(final Vector2f vector2f, final Vector2f center) {
                if (vector2f.x() < center.x()) {
                    if (vector2f.y() < center.y()) {
                        return BOTTOM_LEFT;
                    } else {
                        return TOP_LEFT;
                    }
                } else {
                    if (vector2f.y() < center.y()) {
                        return BOTTOM_RIGHT;
                    } else {
                        return TOP_RIGHT;
                    }
                }
            }
        }

        final BiMap<Corner, Vector2f> boxCornersByCorner = HashBiMap.create();
        boxCorners.forEach(corner -> boxCornersByCorner.put(Corner.closest(corner, center), corner));

        final BiMap<Corner, VertexData> vertexByCorner = HashBiMap.create();
        for (VertexData vertexDatum : vertexData) {
            vertexByCorner.put(Corner.closest(vertexDatum.projectOntoPlaneOf(cullDirection), centerOfVertices), vertexDatum);
        }

        final Map<Vector2f, VertexData> closestVertices = new HashMap<>();
        boxCornersByCorner.forEach((corner, vector2f) -> closestVertices.put(vector2f, vertexByCorner.get(corner)));
        return closestVertices;
    }

    private Collection<Vector2f> buildCorners(final Direction cullDirection, final Vector3f from, final Vector3f to) {
        final Collection<Vector2f> corners = new ArrayList<>();

        final float x1 = from.x();
        final float y1 = from.y();
        final float z1 = from.z();

        final float x2 = to.x();
        final float y2 = to.y();
        final float z2 = to.z();

        switch (cullDirection) {
            case DOWN -> {
                corners.add(new Vector2f(x1, z1));
                corners.add(new Vector2f(x1, z2));
                corners.add(new Vector2f(x2, z2));
                corners.add(new Vector2f(x2, z1));
            }
            case UP -> {
                corners.add(new Vector2f(x1, z1));
                corners.add(new Vector2f(x2, z1));
                corners.add(new Vector2f(x2, z2));
                corners.add(new Vector2f(x1, z2));
            }
            case NORTH -> {
                corners.add(new Vector2f(x1, y1));
                corners.add(new Vector2f(x2, y1));
                corners.add(new Vector2f(x2, y2));
                corners.add(new Vector2f(x1, y2));
            }
            case SOUTH -> {
                corners.add(new Vector2f(x1, y1));
                corners.add(new Vector2f(x1, y2));
                corners.add(new Vector2f(x2, y2));
                corners.add(new Vector2f(x2, y1));
            }
            case WEST -> {
                corners.add(new Vector2f(z1, y1));
                corners.add(new Vector2f(z1, y2));
                corners.add(new Vector2f(z2, y2));
                corners.add(new Vector2f(z2, y1));
            }
            case EAST -> {
                corners.add(new Vector2f(z1, y1));
                corners.add(new Vector2f(z2, y1));
                corners.add(new Vector2f(z2, y2));
                corners.add(new Vector2f(z1, y2));
            }
        }

        return corners;
    }

    private void mergeFaces(
            final List<FaceRegion> src) {
        boolean restart;

        do {
            restart = false;

            final int size = src.size();
            final int sizeMinusOne = size - 1;

            restart:
            for (int x = 0; x < sizeMinusOne; ++x) {
                final FaceRegion faceA = src.get(x);

                for (int y = x + 1; y < size; ++y) {
                    final FaceRegion faceB = src.get(y);

                    if (faceA.extend(faceB)) {
                        src.set(y, src.get(sizeMinusOne));
                        src.remove(sizeMinusOne);

                        restart = true;
                        break restart;
                    }
                }
            }
        }
        while (restart);
    }

    private void processFaces(
            final IAreaAccessor accessor,
            final List<List<FaceRegion>> resultingRegions,
            final IPositionMutator analysisOrder,
            final Direction[] potentialDirections,
            final Function<Vec3, Double> regionBuildingAxisValueExtractor,
            final Function<Vec3, Double> faceBuildingAxisValueExtractor,
            final Function<Vec3, IBlockInformation> neighborStateSupplier) {
        final ArrayList<FaceRegion> regions = Lists.newArrayList();
        final ICullTest test = chiselRenderType.getTest();

        for (final Direction facing : potentialDirections) {
            final FaceBuildingState state = new FaceBuildingState();

            //noinspection Convert2Lambda Performance optimizations.
            accessor.forEachWithPositionMutator(
                    analysisOrder,
                    new Consumer<>() {
                        @Override
                        public void accept(final IStateEntryInfo stateEntryInfo) {
                            if (!ChiseledBlockBakedModel.this.chiselRenderType.isRequiredForRendering(stateEntryInfo)) {
                                return;
                            }

                            if (state.getRegionBuildingAxisValue() != regionBuildingAxisValueExtractor.apply(stateEntryInfo.getStartPoint())) {
                                if (!regions.isEmpty()) {
                                    resultingRegions.add(Lists.newArrayList(regions));
                                }
                                regions.clear();
                                state.setCurrentRegion(null);
                            }
                            state.setRegionBuildingAxisValue(regionBuildingAxisValueExtractor.apply(stateEntryInfo.getStartPoint()));

                            if (state.getFaceBuildingAxisValue() != faceBuildingAxisValueExtractor.apply(stateEntryInfo.getStartPoint())) {
                                state.setCurrentRegion(null);
                            }
                            state.setFaceBuildingAxisValue(faceBuildingAxisValueExtractor.apply(stateEntryInfo.getStartPoint()));

                            final Optional<FaceRegion> potentialRegionData = ChiseledBlockBakedModel.this.buildFaceRegion(
                                    accessor,
                                    facing,
                                    stateEntryInfo,
                                    test,
                                    neighborStateSupplier
                            );

                            if (potentialRegionData.isEmpty()) {
                                state.setCurrentRegion(null);
                                return;
                            }


                            if (state.getCurrentRegion() != null) {
                                if (state.getCurrentRegion().extend(potentialRegionData.get())) {
                                    return;
                                }
                            }

                            state.setCurrentRegion(potentialRegionData.get());
                            regions.add(potentialRegionData.get());
                        }
                    }
            );

            if (!regions.isEmpty()) {
                resultingRegions.add(Lists.newArrayList(regions));
            }
            regions.clear();
        }
    }

    private Optional<FaceRegion> buildFaceRegion(
            final IAreaAccessor blob,
            final Direction facing,
            final IStateEntryInfo target,
            final ICullTest test,
            final Function<Vec3, IBlockInformation> neighborStateSupplier) {
        return Optional.of(target)
                .filter(stateEntryInfo -> {
                    final Vec3 faceOffSet = Vec3.atLowerCornerOf(facing.getNormal()).multiply(
                            StateEntrySize.current().getSizePerBit(),
                            StateEntrySize.current().getSizePerBit(),
                            StateEntrySize.current().getSizePerBit()
                    );
                    final Vec3 offsetTarget = stateEntryInfo.getStartPoint().add(faceOffSet);

                    if (!blob.isInside(offsetTarget)) {
                        final IBlockInformation externalNeighborState = neighborStateSupplier.apply(offsetTarget);

                        return Optional.of(externalNeighborState)
                                .map(neighborState -> test.isVisible(stateEntryInfo.getBlockInformation(), neighborState))
                                .orElseGet(() -> !stateEntryInfo.getBlockInformation().isAir());
                    }

                    return blob.getInAreaTarget(offsetTarget)
                            .map(IStateEntryInfo::getBlockInformation)
                            .map(neighborState -> test.isVisible(stateEntryInfo.getBlockInformation(), neighborState))
                            .orElseGet(() -> !stateEntryInfo.getBlockInformation().isAir());
                })
                .map(stateEntryInfo -> {
                    final Vec3 faceOffSet = Vec3.atLowerCornerOf(facing.getNormal()).multiply(
                            StateEntrySize.current().getSizePerBit(),
                            StateEntrySize.current().getSizePerBit(),
                            StateEntrySize.current().getSizePerBit()
                    );
                    final Vec3 offsetTarget = stateEntryInfo.getStartPoint().add(faceOffSet);

                    return FaceRegion.createFrom3DObjectWithFacing(
                            stateEntryInfo.getStartPoint(),
                            stateEntryInfo.getEndPoint(),
                            facing,
                            stateEntryInfo.getBlockInformation(),
                            !blob.isInside(offsetTarget)
                    );
                });
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final RandomSource rand) {
        return getList(side);
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
    }

    private static final class FaceBuildingState {
        private double regionBuildingAxis = -1d;
        private double faceBuildingAxis = -1d;

        private FaceRegion currentRegion;

        public double getRegionBuildingAxisValue() {
            return regionBuildingAxis;
        }

        public void setRegionBuildingAxisValue(final double regionBuildingAxis) {
            this.regionBuildingAxis = regionBuildingAxis;
        }

        public double getFaceBuildingAxisValue() {
            return faceBuildingAxis;
        }

        public void setFaceBuildingAxisValue(final double faceBuildingAxis) {
            this.faceBuildingAxis = faceBuildingAxis;
        }

        public FaceRegion getCurrentRegion() {
            return currentRegion;
        }

        public void setCurrentRegion(final FaceRegion currentRegion) {
            this.currentRegion = currentRegion;
        }
    }
}
