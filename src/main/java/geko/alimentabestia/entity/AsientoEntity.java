package geko.alimentabestia.entity;

import geko.alimentabestia.ModEntities;
import geko.alimentabestia.block.SillaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AsientoEntity extends Entity {
    private static final double SEAT_HEIGHT = 0.3d;

    public AsientoEntity(EntityType<? extends AsientoEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static boolean sit(Level level, BlockPos pos, Player player) {
        if (player.isPassenger()) {
            return false;
        }
        AsientoEntity seat = existing(level, pos);
        if (seat != null && seat.isVehicle()) {
            return false;
        }
        if (seat == null) {
            seat = new AsientoEntity(ModEntities.ASIENTO.get(), level);
            seat.setPos(pos.getX() + 0.5d, pos.getY() + SEAT_HEIGHT, pos.getZ() + 0.5d);
            level.addFreshEntity(seat);
        }
        return player.startRiding(seat, true);
    }

    public static boolean isOccupied(Level level, BlockPos pos) {
        AsientoEntity seat = existing(level, pos);
        return seat != null && seat.isVehicle();
    }

    public static Player sitter(Level level, BlockPos pos) {
        AsientoEntity seat = existing(level, pos);
        if (seat != null && seat.getFirstPassenger() instanceof Player player) {
            return player;
        }
        return null;
    }

    private static AsientoEntity existing(Level level, BlockPos pos) {
        List<AsientoEntity> list = level.getEntitiesOfClass(AsientoEntity.class, new AABB(pos), e -> !e.isRemoved());
        return list.isEmpty() ? null : list.get(0);
    }

    
    @Override
    public double getPassengersRidingOffset() {
        return 0.35d;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return new Vec3(this.getX(), this.blockPosition().getY() + 1.0d, this.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (!(this.level().getBlockState(this.blockPosition()).getBlock() instanceof SillaBlock)) {
            this.ejectPassengers();
            this.discard();
            return;
        }
        if (!this.isVehicle()) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
