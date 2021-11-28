package mod.chiselsandbits.platforms.core.client.rendering.type;

import mod.chiselsandbits.platforms.core.client.rendering.IRenderingManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Manager for handling the different render types which are available to
 * fluids, items and blocks.
 */
public interface IRenderTypeManager
{

    /**
     * The current render type manager.
     * Deals with the render types which are available on different platforms.
     *
     * @return The render type manager.
     */
    static IRenderTypeManager getInstance() {
        return IRenderingManager.getInstance().getRenderTypeManager();
    }

    /**
     * The currently used {@link RenderType} for the platform.
     * Might be empty if not rendering in a layered world rendering context.
     *
     * @return The current rendering type.
     */
    @NotNull
    Optional<RenderType> getCurrentRenderType();

    /**
     * Sets the current {@link RenderType}, if null is passed in then no
     * layered rendering is performed.
     *
     * @param renderType The new current {@link RenderType}, might be null.
     */
    void setCurrentRenderType(RenderType renderType);

    /**
     * Indicates if the blockstate needs to be rendered in the render type.
     *
     * @param blockState The block state in question.
     * @param renderType The render type.
     * @return True when rendering in the given render type is required, false when not.
     */
    boolean canRenderInType(final BlockState blockState, final RenderType renderType);

    /**
     * Indicates if the fluidState needs to be rendered in the render type.
     *
     * @param fluidState The fluid state in question.
     * @param renderType The render type.
     * @return True when rendering in the given render type is required, false when not.
     */
    boolean canRenderInType(final FluidState fluidState, final RenderType renderType);

    /**
     * Sets in which render type a block needs to be rendered.
     * The predicate for dynamically rendered platforms is used when the platform supports it.
     * Else the default render type is used.
     *
     * @param block The block to set the render types for.
     * @param defaultRenderType The default render type, if the platform does not support dynamic render type selection.
     * @param dynamicSelector The dynamic render type selector.
     */
    void setPossibleRenderTypesFor(
      final Block block,
      final RenderType defaultRenderType,
      final Predicate<RenderType> dynamicSelector
    );

    /**
     * Sets in which render type a block needs to be rendered.
     *
     * @param block The block to set the render types for.
     * @param defaultRenderType The render type to render the block in.
     */
    default void setPossibleRenderTypesFor(
      final Block block,
      final RenderType defaultRenderType
    ) {
        setPossibleRenderTypesFor(block, defaultRenderType, (type) -> type == defaultRenderType);
    }
}
