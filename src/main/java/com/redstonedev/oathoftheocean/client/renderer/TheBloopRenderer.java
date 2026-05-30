package com.redstonedev.oathoftheocean.client.renderer;

import com.redstonedev.oathoftheocean.client.model.TheBloopModel;
import com.redstonedev.oathoftheocean.entity.TheBloopEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class TheBloopRenderer extends GeoEntityRenderer<TheBloopEntity> {
    public TheBloopRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TheBloopModel());
        this.shadowRadius = 4.0F;
        // The Bloop is HUGE - whale-sized cetacean. Both axes 3x to match the big hitbox.
        this.widthScale  = 3.0F;
        this.heightScale = 3.0F;
    }
}
