package com.halomod.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

/** Proyectil de plasma que disparan tanto las armas del jugador como las tropas. */
public class PlasmaBoltEntity extends ThrowableProjectile {
    private WeaponType weapon = WeaponType.PLASMA_RIFLE;

    public PlasmaBoltEntity(EntityType<? extends PlasmaBoltEntity> type, Level level) {
        super(type, level);
    }

    public void setWeapon(WeaponType weapon) {
        this.weapon = weapon;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected float getGravity() {
        return 0.0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(new DustParticleOptions(new Vector3f(weapon.r, weapon.g, weapon.b), 1.0f),
                    getX(), getY(), getZ(), 0, 0, 0);
        } else if (tickCount > 80) {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (getOwner() instanceof CovenantEntity && (target instanceof CovenantEntity || target instanceof PhantomShipEntity)) {
            return false;
        }
        return super.canHitEntity(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide) {
            result.getEntity().hurt(damageSources().thrown(this, getOwner()), weapon.damage);
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
