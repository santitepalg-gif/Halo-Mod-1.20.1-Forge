package com.halomod.registry;

import com.halomod.HaloMod;
import com.halomod.entity.CovenantEntity;
import com.halomod.entity.CovenantSpec;
import com.halomod.entity.PhantomShipEntity;
import com.halomod.entity.PlasmaBoltEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HaloMod.MODID);

    public static final Map<CovenantSpec, RegistryObject<EntityType<CovenantEntity>>> COVENANT =
            new EnumMap<>(CovenantSpec.class);

    static {
        for (CovenantSpec spec : CovenantSpec.values()) {
            COVENANT.put(spec, ENTITIES.register(spec.id, () -> EntityType.Builder
                    .<CovenantEntity>of((type, level) -> new CovenantEntity(type, level, spec), MobCategory.MONSTER)
                    .sized(spec.width, spec.height)
                    .clientTrackingRange(10)
                    .build(HaloMod.MODID + ":" + spec.id)));
        }
    }

    public static final RegistryObject<EntityType<PhantomShipEntity>> PHANTOM = ENTITIES.register("phantom",
            () -> EntityType.Builder.of(PhantomShipEntity::new, MobCategory.MISC)
                    .sized(7.5f, 15.0f)          // 1.5 x 3 de tu JSON, escalado x5
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .fireImmune()
                    .build(HaloMod.MODID + ":phantom"));

    public static final RegistryObject<EntityType<PlasmaBoltEntity>> PLASMA_BOLT = ENTITIES.register("plasma_bolt",
            () -> EntityType.Builder.<PlasmaBoltEntity>of(PlasmaBoltEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(HaloMod.MODID + ":plasma_bolt"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        COVENANT.forEach((spec, type) -> event.put(type.get(), CovenantEntity.createAttributes(spec).build()));
        event.put(PHANTOM.get(), PhantomShipEntity.createAttributes().build());
    }
}
