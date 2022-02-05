package mod.chiselsandbits.client.model.baked.chiseled;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Vector3f;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.culling.ICullTest;
import mod.chiselsandbits.client.model.baked.base.BaseBakedBlockModel;
import mod.chiselsandbits.client.model.baked.face.ChiselsAndBitsBakedQuad;
import mod.chiselsandbits.client.model.baked.face.FaceManager;
import mod.chiselsandbits.client.model.baked.face.FaceRegion;
import mod.chiselsandbits.client.model.baked.face.IFaceBuilder;
import mod.chiselsandbits.client.model.baked.face.model.ModelQuadLayer;
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
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class ChiseledBlockBakedModel extends BaseBakedBlockModel
{
    public static final ChiseledBlockBakedModel EMPTY = new ChiseledBlockBakedModel(
      Blocks.AIR.defaultBlockState(),
      ChiselRenderType.SOLID,
      null,
      vector3d -> Blocks.AIR.defaultBlockState(),
      0);

    private final static int[][]     faceVertMap      = new int[6][4];
    private final static float[][][] quadMapping      = new float[6][4][6];

    private static final Direction[] X_Faces = new Direction[] {Direction.EAST, Direction.WEST};
    private static final Direction[] Y_Faces = new Direction[] {Direction.UP, Direction.DOWN};
    private static final Direction[] Z_Faces = new Direction[] {Direction.SOUTH, Direction.NORTH};
    // Analyze FaceBakery / makeBakedQuad and prepare static data for face gen.
    static
    {
        final Vector3f to = new Vector3f(0, 0, 0);
        final Vector3f from = new Vector3f(16, 16, 16);

        for (final Direction myFace : Direction.values())
        {
            final FaceBakery faceBakery = new FaceBakery();

            final BlockModelRotation mr = BlockModelRotation.X0_Y0;

            final float[] defUVs = new float[] {0, 0, 1, 1};
            final BlockFaceUV uv = new BlockFaceUV(defUVs, 0);
            final BlockElementFace bpf = new BlockElementFace(myFace, 0, "", uv);

            final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("missingno"));
            final BakedQuad q = faceBakery.bakeQuad(to, from, bpf, texture, myFace, mr, null, true, new ResourceLocation(Constants.MOD_ID, "chiseled_block"));

            final int[] vertData = q.getVertices();

            int a = 0;
            int b = 2;

            switch (myFace)
            {
                case NORTH, SOUTH -> {
                    a = 0;
                    b = 1;
                }
                case EAST, WEST -> {
                    a = 1;
                    b = 2;
                }
                default -> {
                }
            }

            final int p = vertData.length / 4;
            for (int vertNum = 0; vertNum < 4; vertNum++)
            {
                final float A = Float.intBitsToFloat(vertData[vertNum * p + a]);
                final float B = Float.intBitsToFloat(vertData[vertNum * p + b]);

                for (int o = 0; o < 3; o++)
                {
                    final float v = Float.intBitsToFloat(vertData[vertNum * p + o]);
                    final float scaler = 1.0f / 16.0f; // pos start in the 0-16
                    quadMapping[myFace.ordinal()][vertNum][o * 2] = v * scaler;
                    quadMapping[myFace.ordinal()][vertNum][o * 2 + 1] = (1.0f - v) * scaler;
                }

                if (ModelUtil.isZero(A) && ModelUtil.isZero(B))
                {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 0;
                }
                else if (ModelUtil.isZero(A) && ModelUtil.isOne(B))
                {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 3;
                }
                else if (ModelUtil.isOne(A) && ModelUtil.isZero(B))
                {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 1;
                }
                else
                {
                    faceVertMap[myFace.get3DDataValue()][vertNum] = 2;
                }
            }
        }
    }

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
      final Direction side)
    {
        if (side != null)
        {
            switch (side)
            {
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
      final BakedQuad[] array)
    {
        if (array == null)
        {
            return Collections.emptyList();
        }

        return Arrays.asList(array);
    }

    public ChiseledBlockBakedModel(
      final BlockState state,
      final ChiselRenderType layer,
      final IAreaAccessor data,
      final Function<Vec3, BlockState> neighborStateSupplier,
      final long primaryStateRenderSeed)
    {
        chiselRenderType = layer;
        BakedModel originalModel = null;

        if (state != null && state.getBlock() != Blocks.AIR)
        {
            originalModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
        }

        if (originalModel != null && data != null)
        {
            final boolean shouldLayerRender;
            try(final IProfilerSection ignoredLayerCheck = ProfilingManager.getInstance().withSection("check")) {
                shouldLayerRender = layer.isRequiredForRendering(data);
            }

            if (shouldLayerRender)
            {
                final ChiseledBlockModelBuilder builder = new ChiseledBlockModelBuilder();
                try (final IProfilerSection ignoredFaceGeneration = ProfilingManager.getInstance().withSection("facegeneration")) {
                    generateFaces(builder, data, neighborStateSupplier, primaryStateRenderSeed);
                }

                // convert from builder to final storage.
                try(final IProfilerSection ignoredFinalize = ProfilingManager.getInstance().withSection("finalize")) {
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

    public boolean isEmpty()
    {
        boolean trulyEmpty = getList(null).isEmpty();

        for (final Direction e : Direction.values())
        {
            trulyEmpty = trulyEmpty && getList(e).isEmpty();
        }

        return trulyEmpty;
    }

    IFaceBuilder getBuilder()
    {
        return new ChiselsAndBitsBakedQuad.Builder(DefaultVertexFormat.BLOCK);
    }

    private void generateFaces(
      final ChiseledBlockModelBuilder builder,
      final IAreaAccessor accessor,
      final Function<Vec3, BlockState> neighborStateSupplier,
      final long primaryStateRenderSeed)
    {
        final List<List<FaceRegion>> resultingFaces = new ArrayList<>();

        try(final IProfilerSection ignoredFaceProcessing = ProfilingManager.getInstance().withSection("processing")) {
            try(final IProfilerSection ignoredXFaces = ProfilingManager.getInstance().withSection("x")) {
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

            try(final IProfilerSection ignoredXFaces = ProfilingManager.getInstance().withSection("y")) {
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

            try(final IProfilerSection ignoredXFaces = ProfilingManager.getInstance().withSection("z")) {
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

        try(final IProfilerSection ignoredFaceBuilding = ProfilingManager.getInstance().withSection("building")) {
            // re-usable float[]'s to minimize garbage cleanup.
            final double[] to = new double[3];
            final double[] from = new double[3];
            final float[] uvs = new float[8];
            final float[] pos = new float[3];

            // single reusable face builder.
            final IFaceBuilder faceBuilder = getBuilder();

            try(final IProfilerSection ignoredMerging = ProfilingManager.getInstance().withSection("merging")) {
                for (final List<FaceRegion> src : resultingFaces) {
                    mergeFaces(src);
                }
            }

            try (final IProfilerSection ignoredQuadGeneration = ProfilingManager.getInstance().withSection("quadGeneration")) {
                for (final List<FaceRegion> src : resultingFaces) {
                    for (final FaceRegion region : src)
                    {
                        final Direction myFace = region.getFace();

                        // keep integers up until the last moment... ( note I tested
                        // snapping the floats after this stage, it made no
                        // difference. )
                        offsetVec(to, region.getMaxX(), region.getMaxY(), region.getMaxZ());
                        offsetVec(from, region.getMinX(), region.getMinY(), region.getMinZ());
                        final ModelQuadLayer[] mpc = FaceManager.getInstance().getCachedFace(region.getBlockState(), myFace, chiselRenderType.layer, primaryStateRenderSeed);

                        if (mpc != null)
                        {
                            for (final ModelQuadLayer pc : mpc)
                            {
                                VertexFormat builderFormat = faceBuilder.getFormat();

                                faceBuilder.begin();
                                faceBuilder.setFace(myFace, pc.getTint());

                                final float maxLightmap = 32.0f / 0xffff;
                                getFaceUvs(uvs, myFace, from, to, pc.getUvs());

                                // build it.
                                for (int vertNum = 0; vertNum < 4; vertNum++)
                                {
                                    for (int elementIndex = 0; elementIndex < builderFormat.getElements().size(); elementIndex++)
                                    {
                                        final VertexFormatElement element = builderFormat.getElements().get(elementIndex);
                                        switch (element.getUsage())
                                        {
                                            case POSITION:
                                                getVertexPos(pos, myFace, vertNum, to, from);
                                                faceBuilder.put(elementIndex, pos[0], pos[1], pos[2]);
                                                break;

                                            case COLOR:
                                                final int cb = pc.getColor();
                                                faceBuilder.put(elementIndex, byteToFloat(cb >> 16), byteToFloat(cb >> 8), byteToFloat(cb), NotZero(byteToFloat(cb >> 24)));
                                                break;

                                            case NORMAL:
                                                // this fixes a bug with Forge AO?? and
                                                // solid blocks.. I have no idea why...
                                                final float normalShift = 0.999f;
                                                faceBuilder.put(elementIndex, normalShift * myFace.getStepX(), normalShift * myFace.getStepY(), normalShift * myFace.getStepZ());
                                                break;

                                            case UV:
                                                if (element.getIndex() == 2)
                                                {
                                                    final float v = maxLightmap * Math.max(0, Math.min(15, pc.getLight()));
                                                    faceBuilder.put(elementIndex, v, v);
                                                }
                                                else
                                                {
                                                    final float u = uvs[faceVertMap[myFace.get3DDataValue()][vertNum] * 2];
                                                    final float v = uvs[faceVertMap[myFace.get3DDataValue()][vertNum] * 2 + 1];
                                                    faceBuilder.put(elementIndex, pc.getSprite().getU(u), pc.getSprite().getV(v));
                                                }
                                                break;

                                            default:
                                                faceBuilder.put(elementIndex);
                                                break;
                                        }
                                    }
                                }

                                if (region.isEdge())
                                {
                                    builder.getList(myFace).add(faceBuilder.create(pc.getSprite()));
                                }
                                else
                                {
                                    builder.getList(null).add(faceBuilder.create(pc.getSprite()));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private float NotZero(
      float byteToFloat)
    {
        if (byteToFloat < 0.00001f)
        {
            return 1;
        }

        return byteToFloat;
    }

    private float byteToFloat(
      final int i)
    {
        return (i & 0xff) / 255.0f;
    }

    private void mergeFaces(
      final List<FaceRegion> src)
    {
        boolean restart;

        do
        {
            restart = false;

            final int size = src.size();
            final int sizeMinusOne = size - 1;

            restart:
            for (int x = 0; x < sizeMinusOne; ++x)
            {
                final FaceRegion faceA = src.get(x);

                for (int y = x + 1; y < size; ++y)
                {
                    final FaceRegion faceB = src.get(y);

                    if (faceA.extend(faceB))
                    {
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
      final Function<Vec3, BlockState> neighborStateSupplier) {
        final ArrayList<FaceRegion> regions = Lists.newArrayList();
        final ICullTest test = chiselRenderType.getTest();

        for (final Direction facing : potentialDirections)
        {
            final FaceBuildingState state = new FaceBuildingState();

            //noinspection Convert2Lambda Performance optimizations.
            accessor.forEachWithPositionMutator(
              analysisOrder,
              new Consumer<>()
              {
                  @Override
                  public void accept(final IStateEntryInfo stateEntryInfo)
                  {
                      if (!ChiseledBlockBakedModel.this.chiselRenderType.isRequiredForRendering(stateEntryInfo))
                      {
                          return;
                      }

                      if (state.getRegionBuildingAxisValue() != regionBuildingAxisValueExtractor.apply(stateEntryInfo.getStartPoint()))
                      {
                          if (!regions.isEmpty())
                          {
                              resultingRegions.add(Lists.newArrayList(regions));
                          }
                          regions.clear();
                          state.setCurrentRegion(null);
                      }
                      state.setRegionBuildingAxisValue(regionBuildingAxisValueExtractor.apply(stateEntryInfo.getStartPoint()));

                      if (state.getFaceBuildingAxisValue() != faceBuildingAxisValueExtractor.apply(stateEntryInfo.getStartPoint()))
                      {
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

                      if (potentialRegionData.isEmpty())
                      {
                          state.setCurrentRegion(null);
                          return;
                      }


                      if (state.getCurrentRegion() != null)
                      {
                          if (state.getCurrentRegion().extend(potentialRegionData.get()))
                          {
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
      final Function<Vec3, BlockState> neighborStateSupplier)
    {
        return Optional.of(target)
          .filter(stateEntryInfo -> {
              final Vec3 faceOffSet = Vec3.atLowerCornerOf(facing.getNormal()).multiply(
                StateEntrySize.current().getSizePerBit(),
                StateEntrySize.current().getSizePerBit(),
                StateEntrySize.current().getSizePerBit()
              );
              final Vec3 offsetTarget = stateEntryInfo.getStartPoint().add(faceOffSet);

              if (!blob.isInside(offsetTarget)) {
                  final BlockState externalNeighborState = neighborStateSupplier.apply(offsetTarget);

                  return Optional.of(externalNeighborState)
                           .map(neighborState -> test.isVisible(stateEntryInfo.getState(), neighborState))
                           .orElseGet(() -> !stateEntryInfo.getState().isAir());
              }

              //TODO: Replace isAir in 1.17
              return blob.getInAreaTarget(offsetTarget)
                .map(IStateEntryInfo::getState)
                .map(neighborState -> test.isVisible(stateEntryInfo.getState(), neighborState))
                .orElseGet(() -> !stateEntryInfo.getState().isAir());
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
                stateEntryInfo.getState(),
                !blob.isInside(offsetTarget)
              );
          });
    }

    // generate final pos from static data.
    private void getVertexPos(
      final float[] pos,
      final Direction side,
      final int vertNum,
      final double[] to,
      final double[] from)
    {
        final float[] interpos = quadMapping[side.ordinal()][vertNum];

        pos[0] = (float) (to[0] * 16f * interpos[0] + from[0] * 16f * interpos[1]);
        pos[1] = (float) (to[1] * 16f * interpos[2] + from[1] * 16f * interpos[3]);
        pos[2] = (float) (to[2] * 16f * interpos[4] + from[2] * 16f * interpos[5]);
    }

    private void getFaceUvs(
      final float[] uvs,
      final Direction face,
      final double[] from,
      final double[] to,
      final float[] quadsUV)
    {
        float to_u = 0;
        float to_v = 0;
        float from_u = 0;
        float from_v = 0;

        switch (face)
        {
            case UP, DOWN -> {
                to_u = (float) to[0];
                to_v = (float) to[2];
                from_u = (float) from[0];
                from_v = (float) from[2];
            }
            case SOUTH, NORTH -> {
                to_u = (float) to[0];
                to_v = (float) to[1];
                from_u = (float) from[0];
                from_v = (float) from[1];
            }
            case EAST, WEST -> {
                to_u = (float) to[1];
                to_v = (float) to[2];
                from_u = (float) from[1];
                from_v = (float) from[2];
            }
            default -> {
            }
        }

        uvs[0] = u(quadsUV, to_u, to_v) * 16; // 0
        uvs[1] = v(quadsUV, to_u, to_v) * 16; // 1

        uvs[2] = u(quadsUV, from_u, to_v) * 16; // 2
        uvs[3] = v(quadsUV, from_u, to_v) * 16; // 3

        uvs[4] = u(quadsUV, from_u, from_v) * 16; // 2
        uvs[5] = v(quadsUV, from_u, from_v) * 16; // 3

        uvs[6] = u(quadsUV, to_u, from_v) * 16; // 0
        uvs[7] = v(quadsUV, to_u, from_v) * 16; // 1
    }

    float u(
      final float[] src,
      final float inU,
      final float inV)
    {
        final float inv = 1.0f - inU;
        final float u1 = src[0] * inU + inv * src[2];
        final float u2 = src[4] * inU + inv * src[6];
        return u1 * inV + (1.0f - inV) * u2;
    }

    float v(
      final float[] src,
      final float inU,
      final float inV)
    {
        final float inv = 1.0f - inU;
        final float v1 = src[1] * inU + inv * src[3];
        final float v2 = src[5] * inU + inv * src[7];
        return v1 * inV + (1.0f - inV) * v2;
    }

    static private void offsetVec(
      final double[] result,
      final double toX,
      final double toY,
      final double toZ)
    {
        result[0] = toX;
        result[1] = toY;
        result[2] = toZ;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, @NotNull final Random rand)
    {
        return getList(side);
    }

    @Override
    public boolean usesBlockLight()
    {
        return true;
    }

    @NotNull
    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
    }

    private static final class FaceBuildingState {
        private double regionBuildingAxis = -1d;
        private double faceBuildingAxis = -1d;

        private FaceRegion currentRegion;

        public double getRegionBuildingAxisValue()
        {
            return regionBuildingAxis;
        }

        public void setRegionBuildingAxisValue(final double regionBuildingAxis)
        {
            this.regionBuildingAxis = regionBuildingAxis;
        }

        public double getFaceBuildingAxisValue()
        {
            return faceBuildingAxis;
        }

        public void setFaceBuildingAxisValue(final double faceBuildingAxis)
        {
            this.faceBuildingAxis = faceBuildingAxis;
        }

        public FaceRegion getCurrentRegion()
        {
            return currentRegion;
        }

        public void setCurrentRegion(final FaceRegion currentRegion)
        {
            this.currentRegion = currentRegion;
        }
    }
}
