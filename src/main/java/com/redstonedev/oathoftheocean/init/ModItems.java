package com.redstonedev.oathoftheocean.init;

import com.redstonedev.oathoftheocean.OathOfTheOcean;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OathOfTheOcean.MODID);

    // Colors sampled from each entity's texture - the most common pixel pair.
    public static final RegistryObject<ForgeSpawnEggItem> EL_GRAN_MAJA_SPAWN_EGG =
            ITEMS.register("el_gran_maja_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.EL_GRAN_MAJA,
                            0x00437E, // deep blue
                            0x093358, // darker blue
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<ForgeSpawnEggItem> SEA_EATER_SPAWN_EGG =
            ITEMS.register("sea_eater_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.SEA_EATER,
                            0x353637, // dark grey
                            0x1A1A1B, // near black
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<ForgeSpawnEggItem> THE_BLOOP_SPAWN_EGG =
            ITEMS.register("the_bloop_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.THE_BLOOP,
                            0x415168, // slate blue-grey
                            0x171718, // near black
                            new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
