package com.redstonedev.oathoftheocean.client.renderer;

import com.redstonedev.oathoftheocean.client.model.SeaEaterModel;
import com.redstonedev.oathoftheocean.entity.SeaEaterEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class SeaEaterRenderer extends GeoEntityRenderer<SeaEaterEntity> {
    public SeaEaterRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SeaEaterModel());
        this.shadowRadius = 1.5F;
        // Slightly bigger than the Bloop - reduced from the previous over-massive scale.
        this.widthScale  = 1.8F;
        this.heightScale = 1.8F;
    }
}
