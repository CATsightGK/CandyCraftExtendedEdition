package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public final class StrawberryJellyBlock extends Block {
    public static final IntegerProperty CUTS = IntegerProperty.create("cuts", 0, 3);
    private static final TagKey<Item> FORGE_KNIVES = ItemTags.create(new ResourceLocation("forge", "tools/knives"));
    private static final TagKey<Item> FARMERS_DELIGHT_KNIVES = ItemTags.create(new ResourceLocation("farmersdelight", "tools/knives"));
    private static final VoxelShape[] SHAPES = {
        box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D),
        Shapes.or(
            box(2.0D, 0.0D, 2.0D, 8.0D, 6.0D, 14.0D),
            box(8.0D, 0.0D, 2.0D, 14.0D, 6.0D, 8.0D)
        ),
        box(2.0D, 0.0D, 2.0D, 8.0D, 6.0D, 14.0D),
        box(2.0D, 0.0D, 2.0D, 8.0D, 6.0D, 8.0D)
    };
    private final Supplier<? extends Item> jellyItem;
    private final Supplier<? extends Item> sliceItem;
    private final Supplier<? extends ParticleOptions> fragmentParticle;

    public StrawberryJellyBlock(BlockBehaviour.Properties properties, Supplier<? extends Item> jellyItem,
            Supplier<? extends Item> sliceItem, Supplier<? extends ParticleOptions> fragmentParticle) {
        super(properties);
        this.jellyItem = jellyItem;
        this.sliceItem = sliceItem;
        this.fragmentParticle = fragmentParticle;
        registerDefaultState(stateDefinition.any().setValue(CUTS, 0));
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                VoxelShape shape = state.getShape(level, pos);
                for (AABB box : shape.toAabbs()) {
                    int xSteps = Math.max(1, (int) Math.ceil((box.maxX - box.minX) * 4.0D));
                    int ySteps = Math.max(1, (int) Math.ceil((box.maxY - box.minY) * 4.0D));
                    int zSteps = Math.max(1, (int) Math.ceil((box.maxZ - box.minZ) * 4.0D));
                    for (int x = 0; x < xSteps; x++) {
                        for (int y = 0; y < ySteps; y++) {
                            for (int z = 0; z < zSteps; z++) {
                                double localX = box.minX + (x + 0.5D) * (box.maxX - box.minX) / xSteps;
                                double localY = box.minY + (y + 0.5D) * (box.maxY - box.minY) / ySteps;
                                double localZ = box.minZ + (z + 0.5D) * (box.maxZ - box.minZ) / zSteps;
                                double velocityX = (localX - 0.5D) * 0.16D + level.getRandom().nextGaussian() * 0.025D;
                                double velocityY = 0.08D + level.getRandom().nextDouble() * 0.08D;
                                double velocityZ = (localZ - 0.5D) * 0.16D + level.getRandom().nextGaussian() * 0.025D;
                                level.addParticle(fragmentParticle.get(), pos.getX() + localX, pos.getY() + localY,
                                    pos.getZ() + localZ, velocityX, velocityY, velocityZ);
                            }
                        }
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CUTS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(CUTS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(CUTS)];
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasFullSupport(level, pos.below());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
            ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
            : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        ItemStack tool = player.getItemInHand(hand);
        if (!isCuttingTool(tool)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(sliceItem.get()));
            int cuts = state.getValue(CUTS);
            if (cuts >= 3) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(CUTS, cuts + 1), Block.UPDATE_ALL);
            }
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.8F, 1.15F);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            player.awardStat(Stats.ITEM_USED.get(tool.getItem()));
            if (!player.getAbilities().instabuild && tool.isDamageableItem()) {
                tool.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(hand));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        int cuts = state.getValue(CUTS);
        if (cuts == 0) {
            return List.of(new ItemStack(jellyItem.get()));
        }
        return List.of(new ItemStack(sliceItem.get(), 4 - cuts));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(jellyItem.get());
    }

    public static boolean hasFullSupport(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
    }

    private static boolean isCuttingTool(ItemStack stack) {
        return stack.is(FORGE_KNIVES)
            || stack.is(FARMERS_DELIGHT_KNIVES)
            || stack.is(CCItems.FORK.get())
            || stack.is(ItemTags.SWORDS)
            || stack.getItem() instanceof SwordItem;
    }
}
