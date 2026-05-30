package com.redstonedev.oathoftheocean;

import com.mojang.logging.LogUtils;
import com.redstonedev.oathoftheocean.client.ClientSetup;
import com.redstonedev.oathoftheocean.entity.ElGranMajaEntity;
import com.redstonedev.oathoftheocean.entity.SeaEaterEntity;
import com.redstonedev.oathoftheocean.entity.TheBloopEntity;
import com.redstonedev.oathoftheocean.event.ForgeEvents;
import com.redstonedev.oathoftheocean.init.ModEntities;
import com.redstonedev.oathoftheocean.init.ModItems;
import com.redstonedev.oathoftheocean.init.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib3.GeckoLib;

@Mod(OathOfTheOcean.MODID)
public class OathOfTheOcean {
    public static final String MODID = "oath_of_the_ocean";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OathOfTheOcean() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        GeckoLib.initialize();

        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::entityAttributes);

        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Oath of the Ocean - the deep stirs");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientSetup.onClientSetup(event);
    }

    private void entityAttributes(final EntityAttributeCreationEvent event) {
        event.put(ModEntities.EL_GRAN_MAJA.get(), ElGranMajaEntity.createAttributes().build());
        event.put(ModEntities.SEA_EATER.get(),    SeaEaterEntity.createAttributes().build());
        event.put(ModEntities.THE_BLOOP.get(),    TheBloopEntity.createAttributes().build());
    }
}
