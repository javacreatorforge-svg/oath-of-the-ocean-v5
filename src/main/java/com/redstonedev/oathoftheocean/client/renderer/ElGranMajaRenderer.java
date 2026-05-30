package com.redstonedev.oathoftheocean.client.renderer;

import com.redstonedev.oathoftheocean.client.model.ElGranMajaModel;
import com.redstonedev.oathoftheocean.entity.ElGranMajaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class ElGranMajaRenderer extends GeoEntityRenderer<ElGranMajaEntity> {
    public ElGranMajaRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ElGranMajaModel());
        this.shadowRadius = 5.0F;
        // El Gran Maja is 675m long IRL - a colossal serpent. Stretch the model very long
        // and keep it relatively low (eel-shaped, not tall). widthScale scales X+Z, so this
        // makes the body extremely long along its length.
        this.widthScale  = 4.0F;
        this.heightScale = 1.8F;
    }
}
