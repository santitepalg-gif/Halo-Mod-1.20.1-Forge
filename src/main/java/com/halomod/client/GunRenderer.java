package com.halomod.client;

import com.halomod.HaloMod;
import com.halomod.item.HaloGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Renderer GeckoLib de las armas: geo/item/<id>.geo.json, animations/item/<id>.animation.json, textures/item/<id>.png */
public class GunRenderer extends GeoItemRenderer<HaloGunItem> {
    public GunRenderer(String id) {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(HaloMod.MODID, id)));
    }
}
