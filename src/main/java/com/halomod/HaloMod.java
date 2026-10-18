package com.halomod;

import com.halomod.registry.ModEntities;
import com.halomod.registry.ModItems;
import com.halomod.registry.ModTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HaloMod.MODID)
public class HaloMod {
    public static final String MODID = "halo";

    public HaloMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        ModTabs.TABS.register(bus);
        bus.addListener(ModEntities::registerAttributes);
    }
}
