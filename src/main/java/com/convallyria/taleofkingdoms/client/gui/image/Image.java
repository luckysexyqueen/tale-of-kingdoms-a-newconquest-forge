package com.convallyria.taleofkingdoms.client.gui.image;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public record Image(ResourceLocation resourceLocation, int x, int y, int[] dimensions) implements IImage {

    public int getWidth() {
        return dimensions[0];
    }

    public int getHeight() {
        return dimensions[1];
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    @Override
    public void render(PoseStack matrices, Screen gui) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resourceLocation);
        //client.getTextureManager().bindTexture(resourceLocation);
        GuiComponent.blit(matrices, x, y, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
    }
}
