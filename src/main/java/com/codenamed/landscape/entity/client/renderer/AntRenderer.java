package com.codenamed.landscape.entity.client.renderer;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.entity.Ant;
import com.codenamed.landscape.entity.Songbird;
import com.codenamed.landscape.entity.client.model.AntModel;
import com.codenamed.landscape.entity.client.model.SongbirdModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AntRenderer extends MobRenderer<Ant, AntModel<Ant>> {


    public AntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel<>(context.bakeLayer(AntModel.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(Ant ant) {

        return ResourceLocation.fromNamespaceAndPath(Landscape.MOD_ID, "textures/entity/ant.png");
    }

    @Override
    public void render(Ant ant, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        super.render(ant, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
