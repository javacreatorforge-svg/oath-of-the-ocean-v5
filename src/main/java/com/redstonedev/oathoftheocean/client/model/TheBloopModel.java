package com.redstonedev.oathoftheocean.client.model;

import com.redstonedev.oathoftheocean.OathOfTheOcean;
import com.redstonedev.oathoftheocean.entity.TheBloopEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TheBloopModel extends AnimatedGeoModel<TheBloopEntity> {
    private static final ResourceLocation MODEL =
            new ResourceLocation(OathOfTheOcean.MODID, "geo/the_bloop.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(OathOfTheOcean.MODID, "textures/entity/the_bloop.png");
    private static final ResourceLocation ANIMATIONS =
            new ResourceLocation(OathOfTheOcean.MODID, "animations/the_bloop.animation.json");

    @Override public ResourceLocation getModelResource(TheBloopEntity e)     { return MODEL; }
    @Override public ResourceLocation getTextureResource(TheBloopEntity e)   { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(TheBloopEntity e) { return ANIMATIONS; }
}
