package com.redstonedev.oathoftheocean.init;

import com.redstonedev.oathoftheocean.OathOfTheOcean;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, OathOfTheOcean.MODID);

    // Each monster gets 8 SEPARATE idle sound events: idle, idle2, idle3 ... idle8.
    public static final List<RegistryObject<SoundEvent>> EL_GRAN_MAJA_IDLES = new ArrayList<>();
    public static final List<RegistryObject<SoundEvent>> SEA_EATER_IDLES    = new ArrayList<>();
    public static final List<RegistryObject<SoundEvent>> THE_BLOOP_IDLES    = new ArrayList<>();

    static {
        for (int i = 1; i <= 8; i++) {
            String suffix = (i == 1) ? "" : String.valueOf(i); // idle, idle2, idle3, ...
            EL_GRAN_MAJA_IDLES.add(register("el_gran_maja_idle" + suffix));
            SEA_EATER_IDLES.add(register("sea_eater_idle" + suffix));
            THE_BLOOP_IDLES.add(register("the_bloop_idle" + suffix));
        }
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> new SoundEvent(new ResourceLocation(OathOfTheOcean.MODID, name)));
    }
}
