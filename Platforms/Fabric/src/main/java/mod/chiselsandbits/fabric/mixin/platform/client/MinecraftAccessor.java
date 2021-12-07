package mod.chiselsandbits.fabric.mixin.platform.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor
{

    @Accessor("itemColors")
    public ItemColors getItemColors();
}
