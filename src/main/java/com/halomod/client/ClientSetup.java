package com.halomod.client;

import com.halomod.HaloMod;
import com.halomod.entity.CovenantEntity;
import com.halomod.entity.PhantomShipEntity;
import com.halomod.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid = HaloMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ModEntities.COVENANT.forEach((spec, type) -> event.registerEntityRenderer(type.get(),
                ctx -> new GeoEntityRenderer<CovenantEntity>(ctx,
                        new DefaultedEntityGeoModel<>(new ResourceLocation(HaloMod.MODID, spec.id))) {
                    @Override
                    protected float getDeathMaxRotation(CovenantEntity animatable) {
                        return 0.0f; // la animación "death" del modelo hace la caída, no el giro de vanilla
                    }
                }));

        event.registerEntityRenderer(ModEntities.PHANTOM.get(),
                ctx -> new GeoEntityRenderer<PhantomShipEntity>(ctx,
                        new DefaultedEntityGeoModel<>(new ResourceLocation(HaloMod.MODID, "phantom"))) {
                    {
                        withScale(5.0f); // minecraft:scale 5 de tu JSON de Bedrock
                    }
                });

        event.registerEntityRenderer(ModEntities.PLASMA_BOLT.get(), NoopRenderer::new);
    }
}
