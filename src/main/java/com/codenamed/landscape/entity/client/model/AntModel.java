package com.codenamed.landscape.entity.client.model;

import com.codenamed.landscape.Landscape;
import com.codenamed.landscape.entity.Ant;
import com.codenamed.landscape.entity.Songbird;
import com.codenamed.landscape.entity.client.animation.AntAnimations;
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


public class AntModel<T extends Ant> extends HierarchicalModel<T> {

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Landscape.MOD_ID, "ant"), "main");
    private final ModelPart ant;
    private final ModelPart head;
    private final ModelPart thorax;
    private final ModelPart limbs;
    private final ModelPart left_limb1;
    private final ModelPart left_limb2;
    private final ModelPart left_limb3;
    private final ModelPart right_limb1;
    private final ModelPart right_limb2;
    private final ModelPart right_limb3;
    private final ModelPart gaster;

    public AntModel(ModelPart root) {
        this.ant = root.getChild("ant");
        this.head = this.ant.getChild("head");
        this.thorax = this.ant.getChild("thorax");
        this.limbs = this.ant.getChild("limbs");
        this.left_limb1 = this.limbs.getChild("left_limb1");
        this.left_limb2 = this.limbs.getChild("left_limb2");
        this.left_limb3 = this.limbs.getChild("left_limb3");
        this.right_limb1 = this.limbs.getChild("right_limb1");
        this.right_limb2 = this.limbs.getChild("right_limb2");
        this.right_limb3 = this.limbs.getChild("right_limb3");
        this.gaster = this.ant.getChild("gaster");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ant = partdefinition.addOrReplaceChild("ant", CubeListBuilder.create(), PartPose.offset(2.0F, 21.0F, -2.0F));

        PartDefinition head = ant.addOrReplaceChild("head", CubeListBuilder.create().texOffs(16, 31).addBox(0.0F, -10.0F, -2.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 32).addBox(4.0F, -10.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 32).addBox(-9.0F, -10.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 20).addBox(0.0F, -10.0F, -2.0F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 31).addBox(-5.0F, -10.0F, -2.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 21).addBox(-9.0F, -10.0F, -2.0F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-7.0F, -6.0F, -5.0F, 9.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 31).addBox(-1.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -3.5F, -5.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 31).addBox(-2.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -3.5F, -5.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition thorax = ant.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(0, 22).addBox(-4.0F, -3.0F, -2.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition limbs = ant.addOrReplaceChild("limbs", CubeListBuilder.create(), PartPose.offset(-2.0F, 4.0F, 2.0F));

        PartDefinition left_limb1 = limbs.addOrReplaceChild("left_limb1", CubeListBuilder.create(), PartPose.offset(1.2F, -4.0F, 0.0F));

        PartDefinition cube_r3 = left_limb1.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(31, 1).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, 0.0F, -1.5708F, -0.7854F, 2.6616F));

        PartDefinition left_limb2 = limbs.addOrReplaceChild("left_limb2", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -1.5F));

        PartDefinition cube_r4 = left_limb2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(31, 1).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, 1.5F, 0.0F, 0.0F, -1.5708F, 1.0908F));

        PartDefinition left_limb3 = limbs.addOrReplaceChild("left_limb3", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -3.0F));

        PartDefinition cube_r5 = left_limb3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(31, 1).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, 1.5F, 0.0F, 1.5708F, -0.7854F, -0.48F));

        PartDefinition right_limb1 = limbs.addOrReplaceChild("right_limb1", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition cube_r6 = right_limb1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(31, 1).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, 1.5F, 0.0F, -1.5708F, 0.7854F, -2.6616F));

        PartDefinition right_limb2 = limbs.addOrReplaceChild("right_limb2", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -1.5F));

        PartDefinition cube_r7 = right_limb2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(31, 1).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, 1.5F, 0.0F, 0.0F, 1.5708F, -1.0908F));

        PartDefinition right_limb3 = limbs.addOrReplaceChild("right_limb3", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -3.0F));

        PartDefinition cube_r8 = right_limb3.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(31, 1).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, 1.5F, 0.0F, 1.5708F, 0.7854F, 0.48F));

        PartDefinition gaster = ant.addOrReplaceChild("gaster", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r9 = gaster.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -2.5F, -4.0F, 7.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -3.5F, 7.0F, 0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    public void setupAnim(Ant ant, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animateWalk(AntAnimations.WALK, limbSwing, limbSwingAmount, 3f, 2.5f);
        this.animate(ant.idleAnimationState, AntAnimations.IDLE, ageInTicks, 1f);


    }

    private void applyHeadRotation(float headYaw, float headPitch) {
        headYaw = Mth.clamp(headYaw, -30f, 30f);
        headPitch = Mth.clamp(headPitch, -25f, 45);

        this.ant.yRot = headYaw * ((float)Math.PI / 180f);
        this.ant.xRot = headPitch *  ((float)Math.PI / 180f);

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        ant.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return ant;
    }

}
