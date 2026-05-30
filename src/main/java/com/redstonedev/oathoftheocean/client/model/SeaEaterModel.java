package com.redstonedev.oathoftheocean.client.model;

import com.redstonedev.oathoftheocean.OathOfTheOcean;
import com.redstonedev.oathoftheocean.entity.SeaEaterEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SeaEaterModel extends AnimatedGeoModel<SeaEaterEntity> {
    private static final ResourceLocation MODEL =
            new ResourceLocation(OathOfTheOcean.MODID, "geo/sea_eater.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(OathOfTheOcean.MODID, "textures/entity/sea_eater.png");
    private static final ResourceLocation ANIMATIONS =
            new ResourceLocation(OathOfTheOcean.MODID, "animations/sea_eater.animation.json");

    @Override public ResourceLocation getModelResource(SeaEaterEntity e)     { return MODEL; }
    @Override public ResourceLocation getTextureResource(SeaEaterEntity e)   { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(SeaEaterEntity e) { return ANIMATIONS; }
}
