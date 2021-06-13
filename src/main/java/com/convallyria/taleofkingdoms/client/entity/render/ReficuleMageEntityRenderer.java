package com.convallyria.taleofkingdoms.client.entity.render;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.reficule.ReficuleMageEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ReficuleMageEntityRenderer<T extends ReficuleMageEntity> extends EntityRenderer<ReficuleMageEntity, PlayerModel<ReficuleMageEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(TaleOfKingdoms.MODID, "textures/entity/updated_textures/reficulemage.png");

    public ReficuleMageEntityRenderer(EntityRendererProvider.Context context, PlayerModel<ReficuleMageEntity> modelBipedIn) {
        super(context, modelBipedIn, 0.5f);
        this.addFeature(new CustomHeadLayer<>(this, context.get()));
        this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this) {
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Illusioner illusioner, float f, float g, float h, float j, float k, float l) {
                if (illusioner.isCastingSpell() || illusioner.isAggressive()) {
                    super.render(poseStack, multiBufferSource, i, illusioner, f, g, h, j, k, l);
                }

            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(ReficuleMageEntity reficuleMageEntity) {
        return TEXTURE;
    }

    @Override
    public void render(Illusioner illusioner, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        if (illusioner.isInvisible()) {
            Vec3[] vec3s = illusioner.getIllusionOffsets(g);
            float h = this.getBob(illusioner, g);

            for(int j = 0; j < vec3s.length; ++j) {
                poseStack.pushPose();
                poseStack.translate(vec3s[j].x + (double) Mth.cos((float)j + h * 0.5F) * 0.025D, vec3s[j].y + (double)Mth.cos((float)j + h * 0.75F) * 0.0125D, vec3s[j].z + (double)Mth.cos((float)j + h * 0.7F) * 0.025D);
                super.render(illusioner, f, g, poseStack, multiBufferSource, i);
                poseStack.popPose();
            }
        } else {
            super.render(illusioner, f, g, poseStack, multiBufferSource, i);
        }
    }

    @Override
    protected boolean isVisible(ReficuleMageEntity reficuleMageEntity) {
        return true;
    }

    @Override
    protected void scale(ReficuleMageEntity illagerEntity, PoseStack matrixStack, float f) {
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
