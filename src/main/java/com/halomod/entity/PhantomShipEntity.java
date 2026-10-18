package com.halomod.entity;

import com.halomod.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Phantom: llega desde el cielo, abre las puertas laterales, suelta tropas y se va.
 *
 * Fases (sincronizadas al cliente) y animación GeckoLib que reproduce cada una:
 *   ARRIVE -> "arrive"           (bucle mientras desciende desde el cielo)
 *   OPEN   -> "open_doors"       (una vez, se queda en el último frame)
 *   DROP   -> "doors_open_idle"  (bucle; aquí se sueltan las tropas)
 *   CLOSE  -> "close_doors"      (una vez, se queda en el último frame)
 *   DEPART -> "depart"           (bucle mientras sube y desaparece)
 *
 * Se invoca con el huevo de aparición o con /summon halo:phantom: el punto donde aparece se toma como
 * el suelo objetivo; la nave se coloca sola muy arriba y baja hasta ahí.
 */
public class PhantomShipEntity extends Mob implements GeoEntity {
    public static final int PHASE_ARRIVE = 0, PHASE_OPEN = 1, PHASE_DROP = 2, PHASE_CLOSE = 3, PHASE_DEPART = 4;
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(PhantomShipEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation ARRIVE = RawAnimation.begin().thenLoop("arrive");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlayAndHold("open_doors");
    private static final RawAnimation DROP = RawAnimation.begin().thenLoop("doors_open_idle");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlayAndHold("close_doors");
    private static final RawAnimation DEPART = RawAnimation.begin().thenLoop("depart");

    // Parámetros de la secuencia (ticks / bloques). Ajústalos a tu modelo.
    private static final double START_HEIGHT = 160.0;   // de dónde cae sobre el suelo objetivo
    private static final double HOVER_HEIGHT = 14.0;    // altura de vuelo sobre el suelo
    private static final double DOOR_OFFSET = 4.5;      // separación lateral de cada puerta respecto al centro
    private static final int ARRIVE_TICKS = 120;
    private static final int OPEN_TICKS = 40;
    private static final int DROP_INTERVAL = 20;
    private static final int CLOSE_TICKS = 40;
    private static final int DEPART_TICKS = 100;

    /** Tropas que suelta, alternando puerta izquierda y derecha. */
    private static final List<CovenantSpec> ROSTER = List.of(
            CovenantSpec.ELITE_MAYOR, CovenantSpec.ELITE_MENOR, CovenantSpec.BRUTE_MENOR, CovenantSpec.GRUNT,
            CovenantSpec.GRUNT, CovenantSpec.JACKAL, CovenantSpec.JACKAL, CovenantSpec.HUNTER_MENOR);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean initialized;
    private double hoverY, startY;
    private int phaseTicks, dropped;

    public PhantomShipEntity(EntityType<? extends PhantomShipEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 2500.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 100.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_PHASE, PHASE_ARRIVE);
    }

    public int getPhase() {
        return entityData.get(DATA_PHASE);
    }

    private void setPhase(int phase) {
        entityData.set(DATA_PHASE, phase);
        phaseTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (!initialized) {
            initialized = true;
            double groundY = getY();
            hoverY = groundY + HOVER_HEIGHT;
            startY = groundY + START_HEIGHT;
            moveTo(getX(), startY, getZ(), getYRot(), 0);
            level().playSound(null, getX(), hoverY, getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE,
                    6.0f, 0.5f);
        }
        phaseTicks++;
        switch (getPhase()) {
            case PHASE_ARRIVE -> {
                double t = Math.min(1.0, phaseTicks / (double) ARRIVE_TICKS);
                double ease = 1.0 - Math.pow(1.0 - t, 3.0);
                setPos(getX(), startY + (hoverY - startY) * ease, getZ());
                if (phaseTicks >= ARRIVE_TICKS) {
                    setPhase(PHASE_OPEN);
                }
            }
            case PHASE_OPEN -> {
                if (phaseTicks >= OPEN_TICKS) {
                    setPhase(PHASE_DROP);
                }
            }
            case PHASE_DROP -> {
                if (dropped < ROSTER.size()) {
                    if (phaseTicks % DROP_INTERVAL == 0) {
                        dropTroop(ROSTER.get(dropped), dropped % 2 == 0 ? -1 : 1);
                        dropped++;
                    }
                } else if (phaseTicks >= DROP_INTERVAL) {
                    setPhase(PHASE_CLOSE);
                }
            }
            case PHASE_CLOSE -> {
                if (phaseTicks >= CLOSE_TICKS) {
                    setPhase(PHASE_DEPART);
                }
            }
            default -> {
                double t = Math.min(1.0, phaseTicks / (double) DEPART_TICKS);
                setPos(getX(), hoverY + (START_HEIGHT * t * t), getZ());
                if (phaseTicks >= DEPART_TICKS) {
                    discard();
                }
            }
        }
    }

    private void dropTroop(CovenantSpec spec, int side) {
        EntityType<CovenantEntity> type = ModEntities.COVENANT.get(spec).get();
        CovenantEntity troop = type.create(level());
        if (troop == null) {
            return;
        }
        double yaw = Math.toRadians(getYRot());
        double rx = -Math.cos(yaw) * DOOR_OFFSET * side;
        double rz = -Math.sin(yaw) * DOOR_OFFSET * side;
        troop.moveTo(getX() + rx, getY() - 1.5, getZ() + rz, getYRot(), 0);
        troop.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 15, 0, false, false));
        troop.finalizeSpawn((ServerLevel) level(), level().getCurrentDifficultyAt(troop.blockPosition()),
                MobSpawnType.EVENT, null, null);
        level().addFreshEntity(troop);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof CovenantEntity) {
            return false;
        }
        int phase = getPhase();
        if (phase == PHASE_ARRIVE || phase == PHASE_DEPART) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 5.0f, Level.ExplosionInteraction.NONE);
        }
        super.die(source);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Init", initialized);
        tag.putDouble("HoverY", hoverY);
        tag.putDouble("StartY", startY);
        tag.putInt("Phase", getPhase());
        tag.putInt("PhaseTicks", phaseTicks);
        tag.putInt("Dropped", dropped);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        initialized = tag.getBoolean("Init");
        hoverY = tag.getDouble("HoverY");
        startY = tag.getDouble("StartY");
        entityData.set(DATA_PHASE, tag.getInt("Phase"));
        phaseTicks = tag.getInt("PhaseTicks");
        dropped = tag.getInt("Dropped");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> switch (getPhase()) {
            case PHASE_OPEN -> state.setAndContinue(OPEN);
            case PHASE_DROP -> state.setAndContinue(DROP);
            case PHASE_CLOSE -> state.setAndContinue(CLOSE);
            case PHASE_DEPART -> state.setAndContinue(DEPART);
            default -> state.setAndContinue(ARRIVE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
