package com.codenamed.landscape.entity.client.model;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.entity.Songbird;
import com.codenamed.landscape.entity.client.animation.SongbirdAnimations;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SongbirdModel<T extends Songbird> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Landscape.MOD_ID, "songbird"), "main");
    private final ModelPart songbird;
    private final ModelPart main;
    private final ModelPart left_wing;
    private final ModelPart right_wing;
    private final ModelPart legs;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public SongbirdModel(ModelPart root) {
        this.songbird = root.getChild("songbird");
        this.main = this.songbird.getChild("main");
        this.left_wing = this.main.getChild("left_wing");
        this.right_wing = this.main.getChild("right_wing");
        this.legs = this.songbird.getChild("legs");
        this.right_leg = this.legs.getChild("right_leg");
        this.left_leg = this.legs.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition songbird = partdefinition.addOrReplaceChild("songbird", CubeListBuilder.create(), PartPose.offset(0.0F, 27.0F, 2.0F));

        PartDefinition main = songbird.addOrReplaceChild("main", CubeListBuilder.create().texOffs(1, 17).addBox(-2.0F, -6.0F, -5.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition cube_r1 = main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -8.0F, -1.0F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.0F, -1.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition cube_r2 = main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -5.0F, 6.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.0F, -1.0F, 0.1309F, 0.0F, 0.0F));

        PartDefinition left_wing = main.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offset(-2.7149F, -7.7185F, 1.5F));

        PartDefinition cube_r3 = left_wing.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(24, 1).addBox(-6.8F, -2.6F, 4.0F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7149F, 4.7185F, -6.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition right_wing = main.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offset(2.6652F, -7.7344F, 1.075F));

        PartDefinition cube_r4 = right_wing.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 1).addBox(2.0F, -4.0F, 4.0F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6652F, 4.7344F, -5.5F, 0.0F, 0.0F, -0.3491F));

        PartDefinition legs = songbird.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_leg = legs.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(0.5F, -4.05F, -2.0F));

        PartDefinition cube_r5 = right_leg.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(18, 16).addBox(-4.0F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 4.05F, -1.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r6 = right_leg.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(18, 16).addBox(-1.0F, 1.0F, 3.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 4.05F, -3.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition left_leg = legs.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -2.0F));

        PartDefinition cube_r7 = left_leg.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(18, 16).addBox(-4.0F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 4.0F, -1.0F, 0.0F, 0.0F, 1.5708F));

        PartDefinition cube_r8 = left_leg.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(18, 16).addBox(-1.0F, 1.0F, 3.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, -3.0F, 1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 48, 48);
    }

    @Override
    public void setupAnim(Songbird songbird, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animateWalk(SongbirdAnimations.FLY, limbSwing, limbSwingAmount, 3f, 2.5f);
    }

    private void applyHeadRotation(float headYaw, float headPitch) {
        headYaw = Mth.clamp(headYaw, -30f, 30f);
        headPitch = Mth.clamp(headPitch, -25f, 45);

        this.songbird.yRot = headYaw * ((float)Math.PI / 180f);
        this.songbird.xRot = headPitch *  ((float)Math.PI / 180f);

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        songbird.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return songbird;
    }
}
