package com.convallyria.taleofkingdoms.client.gui.image;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resources.ResourceLocation;

public interface IImage {

    ResourceLocation getResourceLocation();

    void render(MatrixStack matrices, Screen gui);

}
