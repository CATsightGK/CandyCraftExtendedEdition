package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import com.valentin4311.candycraftmod.block.entity.AlchemyLiquidKind;
import com.valentin4311.candycraftmod.alchemy.AlchemyMixing;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AlchemyTableBlock extends BaseEntityBlock implements EntityBlock {
    private static final double LIQUID_BOTTOM_Y = 3.05D / 16.0D;
    private static final double LIQUID_TOP_Y = 12.50D / 16.0D;
    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 4.0D),
        Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D),
        Block.box(0.0D, 0.0D, 12.0D, 4.0D, 16.0D, 16.0D),
        Block.box(12.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D),
        Block.box(2.0D, 1.0D, 2.0D, 14.0D, 15.0D, 14.0D)
    );

    public AlchemyTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!(level.getBlockEntity(pos) instanceof AlchemyTableBlockEntity blockEntity)) {
            return InteractionResult.PASS;
        }

        if (heldItem.isEmpty()) {
            if (!level.isClientSide) {
                ItemStack removed = blockEntity.removeLastIngredient();
                if (!removed.isEmpty() && !player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        AlchemyLiquidKind heldLiquid = liquidForBucket(heldItem);
        if (heldLiquid != AlchemyLiquidKind.NONE && blockEntity.canAddLiquid(heldLiquid)) {
            if (!level.isClientSide && blockEntity.addLiquid(heldLiquid)) {
                replaceHeldBucket(player, hand);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (heldItem.is(Items.BUCKET)) {
            if (blockEntity.getLiquidKind() != AlchemyLiquidKind.NONE) {
                if (!level.isClientSide) {
                    ItemStack filled = blockEntity.removeLiquidBucket();
                    if (!filled.isEmpty()) {
                        fillBucketFromTable(player, hand, filled);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        if (heldItem.is(Items.SUGAR) && blockEntity.canAcceptManualMixerSugar()) {
            if (!level.isClientSide && blockEntity.addManualMixerSugar(heldItem) && !player.getAbilities().instabuild) {
                heldItem.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!heldItem.isEmpty() && blockEntity.isTopFilled() && AlchemyMixing.isValidIngredient(level, heldItem, blockEntity.getLiquidKind())) {
            if (!level.isClientSide && blockEntity.addIngredient(heldItem)) {
                if (heldItem.is(CCItems.CARAMEL_BUCKET.get())) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                } else if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AlchemyTableBlockEntity blockEntity) {
            return blockEntity.getLiquidKind().lightLevel();
        }
        return 0;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        applyLiquidHeat(level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        applyLiquidHeat(level, pos, entity);
    }

    private static void applyLiquidHeat(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)
                || !(level.getBlockEntity(pos) instanceof AlchemyTableBlockEntity blockEntity)) {
            return;
        }
        int temperature = blockEntity.getLiquidKind().temperature();
        if (temperature >= 900) {
            living.setSecondsOnFire(temperature >= 1200 ? 5 : 3);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemyTableBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTicker(type, CCBlockEntities.ALCHEMY_TABLE.get(), AlchemyTableBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof AlchemyTableBlockEntity blockEntity)) {
            return;
        }

        if (!blockEntity.isMixing() || blockEntity.getLiquidKind() == AlchemyLiquidKind.NONE) {
            return;
        }

        float renderTime = level.getGameTime();
        float mixerSpeed = blockEntity.getClientMixerSpeed(renderTime);
        float speedFactor = Math.min(2.625F, mixerSpeed / 32.0F);
        if (speedFactor < 0.05F || random.nextFloat() > Math.min(0.98F, 0.76F + speedFactor * 0.08F)) {
            return;
        }

        double surfaceY = pos.getY() + LIQUID_BOTTOM_Y
            + (LIQUID_TOP_Y - LIQUID_BOTTOM_Y) * blockEntity.getLiquidFillFraction();
        double mixerAngle = Math.toRadians(blockEntity.getClientMixerAngle(renderTime));
        int particleCount = 3 + Math.max(2, Math.round(speedFactor * 2.0F));
        for (int i = 0; i < particleCount; i++) {
            double angle = mixerAngle + random.nextInt(4) * Math.PI * 0.5D
                + (random.nextDouble() - 0.5D) * 0.55D;
            double radius = 0.11D + random.nextDouble() * Math.min(0.25D, 0.15D + speedFactor * 0.04D);
            double radialX = Math.cos(angle);
            double radialZ = -Math.sin(angle);
            double tangentX = -Math.sin(angle);
            double tangentZ = -Math.cos(angle);
            double tangentialSpeed = 0.022D + speedFactor * 0.022D + random.nextDouble() * 0.018D;
            double outwardSpeed = 0.012D + random.nextDouble() * (0.018D + speedFactor * 0.009D);
            double x = pos.getX() + 0.5D + radialX * radius;
            double y = surfaceY - 0.006D + random.nextDouble() * 0.04D;
            double z = pos.getZ() + 0.5D + radialZ * radius;
            double velocityX = tangentX * tangentialSpeed + radialX * outwardSpeed;
            double velocityY = 0.065D + random.nextDouble() * (0.065D + speedFactor * 0.025D);
            double velocityZ = tangentZ * tangentialSpeed + radialZ * outwardSpeed;
            level.addParticle(CCParticleTypes.ALCHEMY_SPLASH.get(), x, y, z, velocityX, velocityY, velocityZ);
        }
    }

    private static void replaceHeldBucket(Player player, InteractionHand hand) {
        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
        }
    }

    private static void fillBucketFromTable(Player player, InteractionHand hand, ItemStack filled) {
        if (player.getAbilities().instabuild) {
            return;
        }
        ItemStack heldItem = player.getItemInHand(hand);
        heldItem.shrink(1);
        if (heldItem.isEmpty()) {
            player.setItemInHand(hand, filled);
        } else if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }
    }

    private static AlchemyLiquidKind liquidForBucket(ItemStack stack) {
        if (stack.is(CCItems.GRENADINE_BUCKET.get())) {
            return AlchemyLiquidKind.GRENADINE;
        }
        if (stack.is(Items.WATER_BUCKET)) {
            return AlchemyLiquidKind.WATER;
        }
        if (stack.is(Items.MILK_BUCKET)) {
            return AlchemyLiquidKind.MILK;
        }
        if (stack.is(CCItems.LIQUID_CHOCOLATE_BUCKET.get())) {
            return AlchemyLiquidKind.CHOCOLATE;
        }
        if (stack.is(CCItems.LIQUID_CANDY_BUCKET.get())) {
            return AlchemyLiquidKind.LIQUID_CANDY;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return AlchemyLiquidKind.LAVA;
        }
        if (stack.is(CCItems.CARAMEL_BUCKET.get())) {
            return AlchemyLiquidKind.CARAMEL;
        }
        return AlchemyLiquidKind.NONE;
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(
            BlockEntityType<A> actualType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == actualType ? (BlockEntityTicker<A>) ticker : null;
    }
}

