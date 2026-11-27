package geko.alimentabestia.entity;

import geko.alimentabestia.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CeboEntity extends Projectile implements GeoEntity {
    private static final double MAX_OWNER_DISTANCE_SQR = 1024.0d;
    private static final double GRAVITY = 0.03d;
    private static final double AIR_DRAG = 0.99d;

    private boolean floating;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CeboEntity(EntityType<? extends CeboEntity> type, Level level) {
        super(type, level);
    }

    public CeboEntity(Level level, Player owner) {
        this(ModEntities.CEBO.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1d, owner.getZ());
    }

    public Player getPlayerOwner() {
        return this.getOwner() instanceof Player player ? player : null;
    }

    public boolean isFloating() {
        return floating;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        Player owner = getPlayerOwner();
        if (!this.level().isClientSide && !isOwnerValid(owner)) {
            this.discard();
            return;
        }
        if (floating) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        tickFlying();
    }

    private boolean isOwnerValid(Player owner) {
        return owner != null && owner.isAlive() && !owner.isRemoved()
                && this.distanceToSqr(owner) <= MAX_OWNER_DISTANCE_SQR;
    }

    private void tickFlying() {
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.isInWater()) {
            land();
            return;
        }
        if (this.onGround() || this.horizontalCollision || this.verticalCollision) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(AIR_DRAG, AIR_DRAG, AIR_DRAG).subtract(0.0d, GRAVITY, 0.0d));
    }

    private void land() {
        BlockPos pos = this.blockPosition();
        FluidState fluid = this.level().getFluidState(pos);
        this.setPos(this.getX(), pos.getY() + fluid.getHeight(this.level(), pos), this.getZ());
        this.setDeltaMovement(Vec3.ZERO);
        this.floating = true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0d;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.floating = tag.getBoolean("Floating");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Floating", this.floating);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        if (getPlayerOwner() == null) {
            this.discard();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
