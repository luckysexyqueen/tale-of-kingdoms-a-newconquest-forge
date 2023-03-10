package com.convallyria.taleofkingdoms.client.gui.entity.widget;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.shop.ShopItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;


public class ShopButtonWidget extends ButtonWidget {

    private final ShopScreenInterface shopScreen;
    private final ShopItem shopItem;
    protected final int width;
    private final int xPosition;
    private final int yPosition;
    private final boolean enabled = true;
    private final TextRenderer textRenderer;

    public ShopButtonWidget(@NotNull ShopItem shopItem, @NotNull ShopScreenInterface shopScreen, int x, int y, TextRenderer textRenderer) {
        super(x, y, 110, 20, new LiteralText("Buy Button"), button -> shopScreen.setSelectedItem(shopItem));
        this.textRenderer = textRenderer;
        this.shopScreen = shopScreen;
        this.shopItem = shopItem;
        this.width = 110;
        this.height = 20;
        this.xPosition = x;
        this.yPosition = y;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        Identifier identifier = new Identifier(TaleOfKingdoms.MODID,"textures/gui/gui.png");
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, identifier);
        boolean flag = isMouseOver(mouseX, mouseY);
        int k;
        if (shopScreen.getSelectedItem().equals(shopItem)) {
            k = 2;
        } else {
            k = 1;
        }

        this.drawTexture(matrices, xPosition, yPosition, 0, 46 + k * 20, width / 2, height);
        this.drawTexture(matrices, xPosition + width / 2, yPosition, 200 - width / 2, 46 + k * 20, width / 2, height);
        super.mouseDragged(mouseX, mouseY, 0, delta, delta); // Don't know what deltaX and deltaY are.
        if (!enabled) {
            drawCenteredText(matrices, textRenderer, shopItem.getName(), (xPosition + width / 2) - 20, yPosition + (height - 8) / 2, 0xffffcc00);
        } else if (!flag) {
            drawCenteredText(matrices, textRenderer, shopItem.getName(), (xPosition + width / 2) - 20, yPosition + (height - 8) / 2, 0xffffff);
        } else {
            drawCenteredText(matrices, textRenderer, shopItem.getName(), (xPosition + width / 2) - 20, yPosition + (height - 8) / 2, 0x00cc00);
        }
    }
}