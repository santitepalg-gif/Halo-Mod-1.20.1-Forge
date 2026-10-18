package com.halomod.registry;

import com.halomod.HaloMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HaloMod.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.halo.main"))
            .icon(() -> new ItemStack(ModItems.PHANTOM_EGG.get()))
            .displayItems((params, output) -> ModItems.TAB_ITEMS.forEach(i -> output.accept(i.get())))
            .build());
}
