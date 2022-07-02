package mod.chiselsandbits.client.model.baked.chiseled;

import com.google.common.collect.Lists;
import com.mojang.math.Vector3f;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.culling.ICullTest;
import mod.chiselsandbits.client.model.baked.base.BaseBakedBlockModel;
import mod.chiselsandbits.client.model.baked.face.FaceManager;
import mod.chiselsandbits.client.model.baked.face.FaceRegion;
import mod.chiselsandbits.client.model.baked.face.model.ModelQuadLayer;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.utils.ModelUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class ChiseledBlockBakedModel extends BaseBakedBlockModel {
    
    public static final ChiseledBlockBakedModel EMPTY = new ChiseledBlockBakedModel(
            BlockInformation.AIR,
            ChiselRenderType.SOLID,
            null,
            vector3d -> BlockInformation.AIR,
            0);

    private static final FaceBakery FACE_BAKERY = new FaceBakery();

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
            final BlockInformation state,
            final ChiselRenderType layer,
            final IAreaAccessor data,
            final Function<Vec3, BlockInformation> neighborStateSupplier,
            final long primaryStateRenderSeed) {
        chiselRenderType = layer;
        BakedModel originalModel = null;

        if (state != null && !state.isAir()) {
            originalModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state.getBlockState());
        }

        if (originalModel != null && data != null) {
            final boolean shouldLayerRender;
            try (final IProfilerSection ignoredLayerCheck = ProfilingManager.getInstance().withSection("check")) {
                shouldLayerRender = layer.isRequiredForRendering(data);
            }

            if (shouldLayerRender) {
                final ChiseledBlockModelBuilder builder = new ChiseledBlockModelBuilder();
                try (final IProfilerSection ignoredFaceGeneration = ProfilingManager.getInstance().withSection("facegeneration")) {
                    generateFaces(builder, data, neighborStateSupplier, primaryStateRenderSeed);
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
            final Function<Vec3, BlockInformation> neighborStateSupplier,
            final long primaryStateRenderSeed) {
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
            // re-usable float[]'s to minimize garbage cleanup.
            final float[] uvs = new float[8];
            final float[] localUvs = new float[4];
            final float[] pos = new float[3];

            try (final IProfilerSection ignoredMerging = ProfilingManager.getInstance().withSection("merging")) {
                for (final List<FaceRegion> src : resultingFaces) {
                    mergeFaces(src);
                }
            }

            try (final IProfilerSection ignoredQuadGeneration = ProfilingManager.getInstance().withSection("quadGeneration")) {
                for (final List<FaceRegion> src : resultingFaces) {
                    for (final FaceRegion region : src) {
                        final Direction myFace = region.getFace();

                        final Vector3f from = createFromVector(region);
                        final Vector3f to = createToVector(region);
                        final ModelQuadLayer[] mpc = FaceManager.getInstance().getCachedFace(region.getBlockInformation(), myFace, chiselRenderType.layer, primaryStateRenderSeed);

                        Vector3f toB, fromB;

                        switch (myFace) {
                            case UP -> {
                                toB = new Vector3f(to.x(), from.y(), to.z());
                                fromB = new Vector3f(from.x(), from.y(), from.z());
                            }
                            case EAST -> {
                                toB = new Vector3f(from.x(), to.y(), to.z());
                                fromB = new Vector3f(from.x(), from.y(), from.z());
                            }
                            case NORTH -> {
                                toB = new Vector3f(to.x(), to.y(), to.z());
                                fromB = new Vector3f(from.x(), from.y(), to.z());
                            }
                            case SOUTH -> {
                                toB = new Vector3f(to.x(), to.y(), from.z());
                                fromB = new Vector3f(from.x(), from.y(), from.z());
                            }
                            case DOWN -> {
                                toB = new Vector3f(to.x(), to.y(), to.z());
                                fromB = new Vector3f(from.x(), to.y(), from.z());
                            }
                            case WEST -> {
                                toB = new Vector3f(to.x(), to.y(), to.z());
                                fromB = new Vector3f(to.x(), from.y(), from.z());
                            }
                            default -> throw new NullPointerException();
                        }

                        if (mpc != null) {
                            for (final ModelQuadLayer pc : mpc) {
                                getFaceUvs(uvs, myFace, from, to, pc.getUvs());
                                extractLocalUvs(localUvs, myFace, uvs);

                                final BakedQuad quad = FACE_BAKERY.bakeQuad(
                                        fromB,
                                        toB,
                                        new BlockElementFace(myFace, pc.getTint(), pc.getSprite().getName().toString(), new BlockFaceUV(localUvs, 0)),
                                        pc.getSprite(),
                                        myFace,
                                        new ModelState() {
                                            @Override
                                            public boolean isUvLocked() {
                                                return false;
                                            }
                                        },
                                        null,
                                        pc.isShade(),
                                        new ResourceLocation(Constants.MOD_ID, "block")
                                );

                                fixColorsInQuad(quad, pc.getColor());

                                // build it.
                                if (region.isEdge()) {
                                    builder.getList(myFace).add(quad);
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

    private void fixColorsInQuad(final BakedQuad quad, final int color) {
        final int alpha = (color >> 24) & 0xFF;
        final int red = (color >> 16) & 0xFF;
        final int green = (color >> 8) & 0xFF;
        final int blue = color & 0xFF;
        final int renderColor = (alpha << 24) | (blue << 16) | (green << 8) | red;

        for (int i = 3; i < (4 * 8); i +=8) {
            quad.getVertices()[i] = renderColor;
        }
    }

    private Vector3f createFromVector(FaceRegion faceRegion) {
        final Vector3f result = new Vector3f(faceRegion.getMinX(), faceRegion.getMinY(), faceRegion.getMinZ());
        result.mul(16);
        return result;
    }

    private Vector3f createToVector(FaceRegion faceRegion) {
        final Vector3f result = new Vector3f(faceRegion.getMaxX(), faceRegion.getMaxY(), faceRegion.getMaxZ());
        result.mul(16);
        return result;
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
            final Function<Vec3, BlockInformation> neighborStateSupplier) {
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
            final Function<Vec3, BlockInformation> neighborStateSupplier) {
        return Optional.of(target)
                .filter(stateEntryInfo -> {
                    final Vec3 faceOffSet = Vec3.atLowerCornerOf(facing.getNormal()).multiply(
                            StateEntrySize.current().getSizePerBit(),
                            StateEntrySize.current().getSizePerBit(),
                            StateEntrySize.current().getSizePerBit()
                    );
                    final Vec3 offsetTarget = stateEntryInfo.getStartPoint().add(faceOffSet);

                    if (!blob.isInside(offsetTarget)) {
                        final BlockInformation externalNeighborState = neighborStateSupplier.apply(offsetTarget);

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

    private void getFaceUvs(
            final float[] uvs,
            final Direction face,
            final Vector3f from,
            final Vector3f to,
            final float[] quadsUV) {
        float to_u = 0;
        float to_v = 0;
        float from_u = 0;
        float from_v = 0;


        switch (face) {
            case UP-> {
                to_u = 1 - to.x() / 16f;
                to_v = 1 - to.z() / 16f;
                from_u = 1 - from.x() / 16f;
                from_v = 1 - from.z() / 16f;
            }
            case DOWN -> {
                to_u = 1 - to.x() / 16f;
                to_v = from.z() / 16f;
                from_u = 1 - from.x() / 16f;
                from_v = to.z() / 16f;
            }
            case SOUTH -> {
                to_u = 1 -to.x() / 16f;
                to_v = from.y() / 16f;
                from_u = 1 - from.x() / 16f;
                from_v = to.y() / 16f;
            }
            case NORTH -> {
                to_u = from.x() / 16f;
                to_v = from.y() / 16f;
                from_u = to.x() / 16f;
                from_v = to.y() / 16f;
            }
            case WEST -> {
                to_u = 1 - to.z() / 16f;
                to_v = from.y() / 16f;
                from_u = 1 - from.z() / 16f;
                from_v = to.y() / 16f;
            }
            case EAST -> {
                to_u = from.z() / 16f;
                to_v = from.y() / 16f;
                from_u = to.z() / 16f;
                from_v = to.y() / 16f;
            }
            default -> {
            }
        }

        /*
        UV:
        (0,0) ------------------ (1,0)
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
        (0,1) ------------------ (1,1)

        QUBE:
        (0,16) ---------------- (16,16)
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
          |                        |
        (0,0) ------------------ (16,0)


        1) Lower left
        2) Upper left
        3) Lower right
        4) Upper right
        */

        //U maps 0 to 0 and 16 to 1 -> Normal lerping between quadUvs[0] and quadUvs[4]
        //V maps 16 to 0 and 0 to 1 -> Inverse lerping between quadUvs[1] and quadUvs[3]

        //0,1,0,0,1,1,1,0

        final int[] uvOrder = determineUVOrder(quadsUV);

        //LowerLeft
        uvs[0] = u(quadsUV, uvOrder, from_u, to_v) * 16; // 0
        uvs[1] = v(quadsUV, uvOrder, from_u, to_v) * 16; // 1

        //UpperLeft
        uvs[2] = u(quadsUV, uvOrder, from_u, from_v) * 16; // 2
        uvs[3] = v(quadsUV, uvOrder, from_u, from_v) * 16; // 3

        //LowerRight
        uvs[4] = u(quadsUV, uvOrder, to_u, to_v) * 16; // 2
        uvs[5] = v(quadsUV, uvOrder, to_u, to_v) * 16; // 3

        //UpperRight
        uvs[6] = u(quadsUV, uvOrder, to_u, from_v) * 16; // 0
        uvs[7] = v(quadsUV, uvOrder, to_u, from_v) * 16; // 1

        cleanUvs(uvs);
    }

    private int[] determineUVOrder(final float[] quadsUV) {
        final int[] uvOrder = new int[8];

        final float minU = Math.min(Math.min(quadsUV[0], quadsUV[2]), Math.min(quadsUV[4], quadsUV[6]));
        final float maxU = Math.max(Math.max(quadsUV[0], quadsUV[2]), Math.max(quadsUV[4], quadsUV[6]));
        final float minV = Math.min(Math.min(quadsUV[1], quadsUV[3]), Math.min(quadsUV[5], quadsUV[7]));
        final float maxV = Math.max(Math.max(quadsUV[1], quadsUV[3]), Math.max(quadsUV[5], quadsUV[7]));

        for (int i = 0; i < 4; i++)
        {
            final int uIndex = i * 2;
            final int vIndex = uIndex + 1;

            final float u = quadsUV[uIndex];
            final float v = quadsUV[vIndex];

            if (ModelUtil.is(u, minU) && ModelUtil.is(v, maxV)) {
                //LowerLeft should be indexes 0 and 1
                uvOrder[4] = uIndex;
                uvOrder[5] = vIndex;
            }

            if (ModelUtil.is(u, minU) && ModelUtil.is(v, minV)) {
                //UpperLeft should be indexes 2 and 3
                uvOrder[0] = uIndex;
                uvOrder[1] = vIndex;
            }

            if (ModelUtil.is(u, maxU) && ModelUtil.is(v, maxV)) {
                //LowerRight should be indexes 4 and 5
                uvOrder[6] = uIndex;
                uvOrder[7] = vIndex;
            }

            if (ModelUtil.is(u, maxU) && ModelUtil.is(v, minV)) {
                //UpperRight should be indexes 6 and 7
                uvOrder[2] = uIndex;
                uvOrder[3] = vIndex;
            }
        }

        return uvOrder;
    }

    float u(
            final float[] src,
            final int[] uvOrder,
            final float inU,
            final float inV)
    {
        final float inv = 1.0f - inU;
        final float u1 = src[uvOrder[0]] * inU + inv * src[uvOrder[2]];
        final float u2 = src[uvOrder[4]] * inU + inv * src[uvOrder[6]];
        return u1 * inV + (1.0f - inV) * u2;
    }

    float v(
            final float[] src,
            final int[] uvOrder,
            final float inU,
            final float inV)
    {
        final float inv = 1.0f - inU;
        final float v1 = src[uvOrder[1]] * inU + inv * src[uvOrder[3]];
        final float v2 = src[uvOrder[5]] * inU + inv * src[uvOrder[7]];
        return v1 * inV + (1.0f - inV) * v2;
    }

    private static void cleanUvs(
            float[] uvs
    ) {
        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = Math.round(uvs[i]);
        }
    }

    private void extractLocalUvs(
            final float[] localUvs,
            final Direction myFace,
            final float[] uvs
    ) {

        switch (myFace) {
            case DOWN, UP, NORTH, SOUTH, WEST, EAST -> {
                localUvs[0] = uvs[2];
                localUvs[1] = uvs[3];
                localUvs[2] = uvs[4];
                localUvs[3] = uvs[5];
            }
        }


    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand) {
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
