package com.halomod.registry;

import com.halomod.HaloMod;
import com.halomod.entity.CovenantSpec;
import com.halomod.entity.WeaponType;
import com.halomod.item.HaloGunItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HaloMod.MODID);

    public static final Map<WeaponType, RegistryObject<Item>> GUNS = new EnumMap<>(WeaponType.class);
    public static final Map<CovenantSpec, RegistryObject<Item>> EGGS = new EnumMap<>(CovenantSpec.class);
    public static final List<RegistryObject<Item>> TAB_ITEMS = new ArrayList<>();
    public static final RegistryObject<Item> PHANTOM_EGG;

    static {
        for (WeaponType w : WeaponType.values()) {
            RegistryObject<Item> item = ITEMS.register(w.id, () -> new HaloGunItem(w, new Item.Properties()));
            GUNS.put(w, item);
            TAB_ITEMS.add(item);
        }
        for (CovenantSpec s : CovenantSpec.values()) {
            RegistryObject<Item> egg = ITEMS.register(s.id + "_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.COVENANT.get(s), 0x4b3a7a, 0x9be07a, new Item.Properties()));
            EGGS.put(s, egg);
            TAB_ITEMS.add(egg);
        }
        PHANTOM_EGG = ITEMS.register("phantom_spawn_egg",
                () -> new ForgeSpawnEggItem(ModEntities.PHANTOM, 0x2a1b4f, 0x5b3d8f, new Item.Properties()));
        TAB_ITEMS.add(PHANTOM_EGG);
    }
}
