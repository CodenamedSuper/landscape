package com.codenamed.landscape.entity.client.renderer;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.entity.Songbird;
import com.codenamed.landscape.entity.client.model.SongbirdModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SongbirdRenderer extends MobRenderer<Songbird, SongbirdModel<Songbird>> {


    public SongbirdRenderer(EntityRendererProvider.Context context) {
        super(context, new SongbirdModel<>(context.bakeLayer(SongbirdModel.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(Songbird songbird) {
        return ResourceLocation.fromNamespaceAndPath(Landscape.MOD_ID, "textures/entity/songbird.png");
    }

    @Override
    public void render(Songbird entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {


        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
