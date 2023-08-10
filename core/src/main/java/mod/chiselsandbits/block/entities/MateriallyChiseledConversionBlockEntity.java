package mod.chiselsandbits.block.entities;

import mod.chiselsandbits.registrars.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MateriallyChiseledConversionBlockEntity extends BlockEntity {

    private CompoundTag tag = new CompoundTag();

    public MateriallyChiseledConversionBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntityTypes.MATERIAL_CHISELED_CONVERSION.get(), pPos, pBlockState);
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.tag = pTag;
    }

    public CompoundTag getTag() {
        return tag;
    }
}
