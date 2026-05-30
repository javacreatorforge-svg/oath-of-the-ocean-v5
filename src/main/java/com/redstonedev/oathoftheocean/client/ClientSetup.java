package com.redstonedev.oathoftheocean.client;

import com.redstonedev.oathoftheocean.client.renderer.ElGranMajaRenderer;
import com.redstonedev.oathoftheocean.client.renderer.SeaEaterRenderer;
import com.redstonedev.oathoftheocean.client.renderer.TheBloopRenderer;
import com.redstonedev.oathoftheocean.init.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.EL_GRAN_MAJA.get(), ElGranMajaRenderer::new);
            EntityRenderers.register(ModEntities.SEA_EATER.get(),    SeaEaterRenderer::new);
            EntityRenderers.register(ModEntities.THE_BLOOP.get(),    TheBloopRenderer::new);
        });
    }
}
