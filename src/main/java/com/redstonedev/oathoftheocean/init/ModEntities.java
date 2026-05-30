package com.redstonedev.oathoftheocean.init;

import com.redstonedev.oathoftheocean.OathOfTheOcean;
import com.redstonedev.oathoftheocean.entity.ElGranMajaEntity;
import com.redstonedev.oathoftheocean.entity.SeaEaterEntity;
import com.redstonedev.oathoftheocean.entity.TheBloopEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, OathOfTheOcean.MODID);

    public static final RegistryObject<EntityType<ElGranMajaEntity>> EL_GRAN_MAJA =
            ENTITIES.register("el_gran_maja", () -> EntityType.Builder
                    .<ElGranMajaEntity>of(ElGranMajaEntity::new, MobCategory.MONSTER)
                    .sized(10.0F, 3.0F) // very LONG serpent (~675m irl scaled down) - low and long
                    .clientTrackingRange(20)
                    .build(new ResourceLocation(OathOfTheOcean.MODID, "el_gran_maja").toString()));

    public static final RegistryObject<EntityType<SeaEaterEntity>> SEA_EATER =
            ENTITIES.register("sea_eater", () -> EntityType.Builder
                    .<SeaEaterEntity>of(SeaEaterEntity::new, MobCategory.MONSTER)
                    .sized(4.0F, 7.0F) // slightly bigger than the Bloop, no longer absurdly huge
                    .clientTrackingRange(16)
                    .build(new ResourceLocation(OathOfTheOcean.MODID, "sea_eater").toString()));

    public static final RegistryObject<EntityType<TheBloopEntity>> THE_BLOOP =
            ENTITIES.register("the_bloop", () -> EntityType.Builder
                    .<TheBloopEntity>of(TheBloopEntity::new, MobCategory.MONSTER)
                    .sized(8.0F, 6.0F) // BIG whale-like cetacean
                    .clientTrackingRange(20)
                    .build(new ResourceLocation(OathOfTheOcean.MODID, "the_bloop").toString()));
}
