package mod.chiselsandbits.client.screens.components.toasts;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.util.IWithColor;
import mod.chiselsandbits.api.util.IWithIcon;
import mod.chiselsandbits.api.util.IWithText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChiselsAndBitsNotificationToast<T extends IWithColor & IWithIcon & IWithText> implements Toast
{
    private final T contents;

    public static <G extends IWithColor & IWithIcon & IWithText> void notifyOf(G contents) {
        Minecraft.getInstance().getToasts().addToast(new ChiselsAndBitsNotificationToast<>(contents));
    }

    private ChiselsAndBitsNotificationToast(final T contents) {this.contents = contents;}

    @Override
    public @NotNull Visibility render(final @NotNull PoseStack poseStack, final @NotNull ToastComponent toastComponent, final long time)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        toastComponent.blit(poseStack, 0, 0, 0, 0, this.width(), this.height());

        List<FormattedCharSequence> list = toastComponent.getMinecraft().font.split(contents.getText(), 125);
        int textColor = 16746751;
        if (list.size() == 1)
        {
            toastComponent.getMinecraft().font.draw(poseStack, list.get(0), 30.0F, 18.0F, -1);
        }
        else
        {
            int fontColor = Mth.floor(Mth.clamp((float) (time) / 40.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
            int verticalOffset = this.height() / 2 - list.size() * 9 / 2;

            for (FormattedCharSequence formattedcharsequence : list)
            {
                toastComponent.getMinecraft().font.draw(poseStack, formattedcharsequence, 30.0F, (float) verticalOffset, 16777215 | fontColor);
                verticalOffset += 9;
            }
        }

        poseStack.pushPose();
        poseStack.translate(8,8,0); //TODO Check if this is correct
        poseStack.pushPose();

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderColor(
          (float) contents.getColorVector().x(),
          (float) contents.getColorVector().y(),
          (float) contents.getColorVector().z(),
          (float) contents.getAlphaChannel()
        );
        RenderSystem.setShaderTexture(0, contents.getIcon());
        GuiComponent.blit(poseStack, 0, 0, 16,16, 0, 0, 18, 18, 18, 18);

        poseStack.popPose();
        poseStack.popPose();

        return time >= 5000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}
