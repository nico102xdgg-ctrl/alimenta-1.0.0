package geko.alimentabestia.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FocaEntity extends Mob implements GeoEntity {
    public enum AnimState {NADANDO, PEDIR, COMIENDO, MURIENDO, REPOSO}

    private static final EntityDataAccessor<Byte> DATA_ANIM_STATE =
            SynchedEntityData.defineId(FocaEntity.class, EntityDataSerializers.BYTE);

    private static final RawAnimation NADANDO = RawAnimation.begin().thenPlayAndHold("nadando");
    private static final RawAnimation PEDIR = RawAnimation.begin().thenLoop("PEDIR");
    private static final RawAnimation COMIENDO = RawAnimation.begin().thenLoop("comiendo");
    private static final RawAnimation MURIENDO = RawAnimation.begin().thenPlay("nadando_matar");
    private static final RawAnimation REPOSO = RawAnimation.begin().thenLoop("nadando");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int focaId;

    public FocaEntity(EntityType<? extends FocaEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0d);
    }

    public void setFocaId(int id) {
        this.focaId = id;
    }

    public int getFocaId() {
        return focaId;
    }

    public AnimState getAnimState() {
        return AnimState.values()[this.entityData.get(DATA_ANIM_STATE)];
    }

    public void setAnimState(AnimState state) {
        if (getAnimState() != state) {
            this.entityData.set(DATA_ANIM_STATE, (byte) state.ordinal());
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ANIM_STATE, (byte) AnimState.REPOSO.ordinal());
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(2.0d, 0.5d, 3.0d);
    }

    @Override
    public boolean removeWhenFarAway(double distanceSqr) {
        return false;
    }

    @Override
    public void travel(Vec3 travelVector) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.focaId = tag.getInt("FocaId");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FocaId", this.focaId);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "foca", 0, this::handle));
    }

    private PlayState handle(AnimationState<FocaEntity> state) {
        return switch (getAnimState()) {
            case NADANDO -> state.setAndContinue(NADANDO);
            case PEDIR -> state.setAndContinue(PEDIR);
            case COMIENDO -> state.setAndContinue(COMIENDO);
            case MURIENDO -> state.setAndContinue(MURIENDO);
            default -> state.setAndContinue(REPOSO);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
