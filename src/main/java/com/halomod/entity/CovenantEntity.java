package com.halomod.entity;

import com.halomod.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

/**
 * Tropa Covenant genérica: Elite, Grunt, Jackal, Brute y Hunter son la misma clase con un {@link CovenantSpec}.
 * Animaciones GeckoLib esperadas en el .animation.json de cada entidad (nombres exactos):
 *   idle, walk, run, shoot, melee, block, death
 */
public class CovenantEntity extends Monster implements GeoEntity {
    public static final int STATE_IDLE = 0, STATE_SHOOT = 1, STATE_MELEE = 2, STATE_BLOCK = 3;
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(CovenantEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation SHOOT = RawAnimation.begin().thenLoop("shoot");
    private static final RawAnimation MELEE = RawAnimation.begin().thenPlay("melee");
    private static final RawAnimation BLOCK = RawAnimation.begin().thenLoop("block");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final CovenantSpec spec;
    private int stateTimer;
    private int shieldTicks;

    public CovenantEntity(EntityType<? extends CovenantEntity> type, Level level, CovenantSpec spec) {
        super(type, level);
        this.spec = spec;
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes(CovenantSpec spec) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, spec.health)
                .add(Attributes.MOVEMENT_SPEED, spec.speed)
                .add(Attributes.ATTACK_DAMAGE, spec.melee)
                .add(Attributes.FOLLOW_RANGE, spec.followRange)
                .add(Attributes.KNOCKBACK_RESISTANCE, spec.knockbackResistance);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_STATE, STATE_IDLE);
    }

    public int getState() {
        return entityData.get(DATA_STATE);
    }

    public void setState(int state) {
        if (getState() != state) {
            entityData.set(DATA_STATE, state);
        }
    }

    public boolean isShielding() {
        return shieldTicks > 0;
    }

    // ------------------------------------------------------------------ IA

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new CombatGoal(this));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                e -> e instanceof Enemy && !(e instanceof CovenantEntity)));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof CovenantEntity || attacker instanceof PhantomShipEntity) {
            return false; // sin fuego amigo
        }
        if (!level().isClientSide && spec.shieldChance > 0 && source.is(DamageTypeTags.IS_PROJECTILE)) {
            if (isShielding()) {
                return false;
            }
            if (random.nextFloat() < spec.shieldChance) {
                shieldTicks = spec.shieldTicks;
                setState(STATE_BLOCK);
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        if (shieldTicks > 0 && --shieldTicks == 0) {
            setState(STATE_IDLE);
        }
        if (stateTimer > 0 && --stateTimer == 0 && getState() == STATE_MELEE) {
            setState(STATE_IDLE);
        }
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime >= 40 && !level().isClientSide() && !isRemoved()) {
            level().broadcastEntityEvent(this, (byte) 60);
            remove(RemovalReason.KILLED);
        }
    }

    public void fire(LivingEntity target) {
        PlasmaBoltEntity bolt = new PlasmaBoltEntity(ModEntities.PLASMA_BOLT.get(), level());
        bolt.setOwner(this);
        bolt.setWeapon(spec.weapon);
        Vec3 from = getEyePosition().add(getLookAngle().scale(0.8)).subtract(0, 0.2, 0);
        bolt.setPos(from);
        double dx = target.getX() - from.x;
        double dy = target.getEyeY() - from.y;
        double dz = target.getZ() - from.z;
        bolt.shoot(dx, dy, dz, spec.weapon.velocity, 3.0f);
        level().addFreshEntity(bolt);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.6f,
                1.4f + random.nextFloat() * 0.3f);
    }

    public void meleeHit(LivingEntity target) {
        swing(InteractionHand.MAIN_HAND);
        doHurtTarget(target);
        setState(STATE_MELEE);
        stateTimer = 12;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ShieldTicks", shieldTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        shieldTicks = tag.getInt("ShieldTicks");
    }

    // ------------------------------------------------------- Animaciones

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, state -> {
            if (isDeadOrDying()) {
                return state.setAndContinue(DEATH);
            }
            switch (getState()) {
                case STATE_BLOCK:
                    return state.setAndContinue(BLOCK);
                case STATE_MELEE:
                    return state.setAndContinue(MELEE);
                case STATE_SHOOT:
                    return state.setAndContinue(SHOOT);
                default:
                    break;
            }
            if (state.isMoving()) {
                return state.setAndContinue(isAggressive() ? RUN : WALK);
            }
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ------------------------------------------------------ Goal de combate

    /** Dispara ráfagas a distancia, ataca cuerpo a cuerpo de cerca (los Grunt huyen). */
    private static class CombatGoal extends Goal {
        private final CovenantEntity mob;
        private int burstLeft, burstTimer, cooldown, meleeCooldown;
        private boolean fleeing, wasInMelee;

        CombatGoal(CovenantEntity mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = mob.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void stop() {
            burstLeft = 0;
            mob.setAggressive(false);
            if (!mob.isShielding()) {
                mob.setState(STATE_IDLE);
            }
        }

        @Override
        public void tick() {
            LivingEntity t = mob.getTarget();
            if (t == null) {
                return;
            }
            CovenantSpec s = mob.spec;
            mob.setAggressive(true);
            mob.getLookControl().setLookAt(t, 30f, 30f);
            if (cooldown > 0) cooldown--;
            if (meleeCooldown > 0) meleeCooldown--;

            if (mob.isShielding()) {
                mob.getNavigation().stop();
                return;
            }

            double d2 = mob.distanceToSqr(t);
            if (d2 <= s.meleeRange * s.meleeRange) {
                burstLeft = 0;
                if (!wasInMelee) {
                    wasInMelee = true;
                    fleeing = s.cowardly && mob.random.nextFloat() < 0.96f;
                }
                if (fleeing) {
                    Vec3 away = DefaultRandomPos.getPosAway(mob, 12, 6, t.position());
                    if (away != null) {
                        mob.getNavigation().moveTo(away.x, away.y, away.z, 1.4);
                    }
                    return;
                }
                mob.getNavigation().moveTo(t, 1.3);
                double w = mob.getBbWidth() * 2.0f * s.reach;
                double reachSqr = w * w + t.getBbWidth();
                if (meleeCooldown <= 0 && d2 <= reachSqr && mob.getSensing().hasLineOfSight(t)) {
                    mob.meleeHit(t);
                    meleeCooldown = 20;
                }
                return;
            }
            wasInMelee = false;

            if (mob.getSensing().hasLineOfSight(t) && d2 <= s.range * s.range) {
                mob.getNavigation().stop();
                if (burstLeft > 0) {
                    mob.setState(STATE_SHOOT);
                    if (--burstTimer <= 0) {
                        mob.fire(t);
                        burstLeft--;
                        burstTimer = s.burstInterval;
                        if (burstLeft == 0) {
                            cooldown = s.burstCooldown + mob.random.nextInt(20);
                        }
                    }
                } else {
                    if (mob.getState() == STATE_SHOOT) {
                        mob.setState(STATE_IDLE);
                    }
                    if (cooldown <= 0) {
                        burstLeft = s.burstMin + mob.random.nextInt(s.burstMax - s.burstMin + 1);
                        burstTimer = 0;
                    }
                }
            } else {
                mob.getNavigation().moveTo(t, 1.0);
            }
        }
    }
}
