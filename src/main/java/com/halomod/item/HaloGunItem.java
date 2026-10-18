package com.halomod.item;

import com.halomod.client.GunRenderer;
import com.halomod.entity.PlasmaBoltEntity;
import com.halomod.entity.WeaponType;
import com.halomod.registry.ModEntities;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * Arma de plasma del jugador. Mantén clic derecho para disparar.
 * Animaciones esperadas en animations/item/<id>.animation.json: idle, fire
 */
public class HaloGunItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final WeaponType type;

    public HaloGunItem(WeaponType type, Properties properties) {
        super(properties.stacksTo(1));
        this.type = type;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity shooter, ItemStack stack, int remaining) {
        if (level.isClientSide) {
            return;
        }
        int used = getUseDuration(stack) - remaining;
        if (used % type.cooldown != 0) {
            return;
        }
        PlasmaBoltEntity bolt = new PlasmaBoltEntity(ModEntities.PLASMA_BOLT.get(), level);
        bolt.setOwner(shooter);
        bolt.setWeapon(type);
        Vec3 look = shooter.getLookAngle();
        bolt.setPos(shooter.getEyePosition().add(look.scale(0.6)).subtract(0, 0.15, 0));
        bolt.shoot(look.x, look.y, look.z, type.velocity, 0.8f);
        level.addFreshEntity(bolt);
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS, 0.6f, 1.5f);
        if (level instanceof ServerLevel serverLevel) {
            triggerAnim(shooter, GeoItem.getOrAssignId(stack, serverLevel), "main", "fire");
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GunRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new GunRenderer(type.id);
                }
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("idle")))
                .triggerableAnim("fire", RawAnimation.begin().thenPlay("fire")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
