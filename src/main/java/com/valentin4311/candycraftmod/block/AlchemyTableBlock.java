package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import com.valentin4311.candycraftmod.block.entity.AlchemyLiquidKind;
import com.valentin4311.candycraftmod.alchemy.AlchemyMixing;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class AlchemyTableBlock extends BaseEntityBlock implements EntityBlock {
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

        if (!heldItem.isEmpty() && blockEntity.isTopFilled() && AlchemyMixing.isValidIngredient(heldItem)) {
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

        boolean fast = blockEntity.isFastMixing();
        if (random.nextInt(fast ? 2 : 5) != 0) {
            return;
        }

        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = 0.08D + random.nextDouble() * (fast ? 0.32D : 0.23D);
        double motion = (fast ? 0.03D : 0.015D) + random.nextDouble() * 0.025D;
        double x = pos.getX() + 0.5D + Math.cos(angle) * radius;
        double y = pos.getY() + 0.58D + random.nextDouble() * 0.12D;
        double z = pos.getZ() + 0.5D + Math.sin(angle) * radius;
        float[] color = particleColor(blockEntity.getLiquidKind());
        level.addParticle(new DustParticleOptions(new Vector3f(color[0], color[1], color[2]), fast ? 0.9F : 0.65F),
            x, y, z, Math.cos(angle) * motion, 0.025D + random.nextDouble() * 0.025D, Math.sin(angle) * motion);
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
        if (stack.is(CCSweetscapeItems.LIQUID_CHOCOLATE_BUCKET.get())) {
            return AlchemyLiquidKind.CHOCOLATE;
        }
        if (stack.is(CCSweetscapeItems.LIQUID_CANDY_BUCKET.get())) {
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

    private static float[] particleColor(AlchemyLiquidKind kind) {
        return switch (kind) {
            case GRENADINE -> new float[] { 1.0F, 0.13F, 0.26F };
            case WATER -> new float[] { 0.25F, 0.55F, 1.0F };
            case MILK -> new float[] { 0.95F, 0.95F, 0.88F };
            case CHOCOLATE -> new float[] { 0.45F, 0.20F, 0.08F };
            case LIQUID_CANDY -> new float[] { 1.0F, 0.32F, 0.66F };
            case LAVA -> new float[] { 1.0F, 0.32F, 0.02F };
            case CARAMEL -> new float[] { 0.92F, 0.46F, 0.10F };
            case NONE -> new float[] { 1.0F, 1.0F, 1.0F };
        };
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(
            BlockEntityType<A> actualType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == actualType ? (BlockEntityTicker<A>) ticker : null;
    }
}
