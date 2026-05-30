package com.redstonedev.oathoftheocean.client.model;

import com.redstonedev.oathoftheocean.OathOfTheOcean;
import com.redstonedev.oathoftheocean.entity.ElGranMajaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ElGranMajaModel extends AnimatedGeoModel<ElGranMajaEntity> {
    private static final ResourceLocation MODEL =
            new ResourceLocation(OathOfTheOcean.MODID, "geo/el_gran_maja.geo.json");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(OathOfTheOcean.MODID, "textures/entity/el_gran_maja.png");
    private static final ResourceLocation ANIMATIONS =
            new ResourceLocation(OathOfTheOcean.MODID, "animations/el_gran_maja.animation.json");

    @Override public ResourceLocation getModelResource(ElGranMajaEntity e)     { return MODEL; }
    @Override public ResourceLocation getTextureResource(ElGranMajaEntity e)   { return TEXTURE; }
    @Override public ResourceLocation getAnimationResource(ElGranMajaEntity e) { return ANIMATIONS; }
}
