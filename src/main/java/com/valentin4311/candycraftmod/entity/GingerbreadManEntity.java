package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import net.minecraft.network.chat.Component;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class GingerbreadManEntity extends Villager {
    public static final int BLACKSMITH = 0;
    public static final int FARMER = 1;
    public static final int CITIZEN = 2;
    public static final int ELDER = 3;
    private static final int MAX_TRADE_USES = 999999;
    private static final EntityDataAccessor<Integer> PROFESSION_VARIANT = SynchedEntityData.defineId(GingerbreadManEntity.class, EntityDataSerializers.INT);

    public GingerbreadManEntity(EntityType<? extends GingerbreadManEntity> type, Level level) {
        super(type, level);
        setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.FARMER, 1));
    }

    @Override
    protected void registerGoals() {
        goalSelector.removeAllGoals(goal -> true);
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(2, new GingerbreadAvoidPlayerGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.25D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, GingerbreadManEntity.class, 5.0F, 0.02F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PROFESSION_VARIANT, CITIZEN);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        if (getGingerProfession() != ELDER) {
            setGingerProfession(getRandom().nextInt(3));
        }
        updateTrades();
        return data;
    }

    public int getSkinVariant() {
        return getGingerProfession();
    }

    public void setSkinVariant(int variant) {
        setGingerProfession(variant);
    }

    public int getGingerProfession() {
        return entityData.get(PROFESSION_VARIANT);
    }

    public void setGingerProfession(int profession) {
        int clamped = Math.max(BLACKSMITH, Math.min(ELDER, profession));
        entityData.set(PROFESSION_VARIANT, clamped);
        getOffers().clear();
        updateTrades();
    }

    private boolean shouldAvoidPlayer(Player player) {
        return getGingerProfession() != ELDER && !isTrading() && !player.isSpectator()
            && !EmblemHelper.has(player, CCItems.GINGERBREAD_EMBLEM.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isAlive() || isTrading() || isBaby()) {
            return super.mobInteract(player, hand);
        }
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            getNavigation().stop();
            updateTrades();
            if (!getOffers().isEmpty()) {
                setTradingPlayer(player);
                openTradingScreen(player, getDisplayName(), 1);
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = getOffers();
        if (!offers.isEmpty()) {
            return;
        }
        RandomSource random = getRandom();
        switch (getGingerProfession()) {
            case BLACKSMITH -> addBlacksmithTrades(offers, random);
            case FARMER -> addFarmerTrades(offers, random);
            case ELDER -> addElderTrades(offers);
            default -> addCitizenTrades(offers, random);
        }
    }

    private static void addBlacksmithTrades(MerchantOffers offers, RandomSource random) {
        addChance(offers, random, 2, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 4, 13), result(CCItems.HONEY_ARROW.get(), random, 1, 4));
        addChance(offers, random, 2, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 4, 43), result(CCItems.HONEY_BOLT.get(), random, 1, 4));
        addChance(offers, random, 1, 5, cost(CCItems.CHOCOLATE_COIN.get(), 64), cost(CCItems.CHOCOLATE_COIN.get(), random, 32, 51), new ItemStack(CCItems.CARAMEL_BOW.get()));
        addChance(offers, random, 2, 4, cost(CCItems.CHOCOLATE_COIN.get(), random, 50, 59), new ItemStack(CCItems.HONEY_SWORD.get()));
        addChance(offers, random, 1, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 40, 49), new ItemStack(CCItems.HONEY_HELMET.get()));
        addChance(offers, random, 2, 6, cost(CCItems.CHOCOLATE_COIN.get(), random, 55, 62), new ItemStack(CCItems.HONEY_PLATE.get()));
        addChance(offers, random, 2, 4, cost(CCItems.CHOCOLATE_COIN.get(), random, 50, 57), new ItemStack(CCItems.HONEY_LEGGINGS.get()));
        addChance(offers, random, 2, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 35, 49), new ItemStack(CCItems.HONEY_BOOTS.get()));
        addChance(offers, random, 1, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 15, 24), new ItemStack(CCItems.HONEYCOMB.get()));
        addChance(offers, random, 1, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 35, 54), new ItemStack(CCItems.PEZ.get()));
        ensureTrade(offers, cost(CCItems.CHOCOLATE_COIN.get(), 8), new ItemStack(CCItems.HONEY_ARROW.get(), 2));
    }

    private static void addFarmerTrades(MerchantOffers offers, RandomSource random) {
        addChance(offers, random, 2, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 40, 49), result(CCItems.FORK.get(), random, 1, 2));
        addChance(offers, random, 2, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 24, 33), result(CCItems.LOLLIPOP_SEEDS.get(), random, 1, 2));
        addTrade(offers, cost(CCItems.CHOCOLATE_COIN.get(), random, 2, 11), result(CCItems.DRAGIBUS.get(), random, 1, 2));
        addChance(offers, random, 1, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 4, 13), result(CCBlocks.FRAISE_TAGADA_FLOWER.get(), random, 1, 4));
        if (random.nextInt(5) < 2) {
            if (random.nextBoolean()) {
                addTrade(offers, cost(CCItems.CHOCOLATE_COIN.get(), random, 4, 15), result(CCBlocks.MINT.get(), random, 1, 6));
            } else {
                addTrade(offers, cost(CCItems.CHOCOLATE_COIN.get(), random, 4, 15), result(CCBlocks.BANANA_SEAWEED.get(), random, 1, 6));
            }
        }
        addChance(offers, random, 2, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 8, 17), result(CCItems.LOLLIPOP.get(), random, 3, 10));
        addChance(offers, random, 1, 8, cost(CCItems.CHOCOLATE_COIN.get(), random, 15, 34), new ItemStack(CCBlocks.LOLLIPOP_BLOCK.get()));
        ensureTrade(offers, cost(CCItems.CHOCOLATE_COIN.get(), 28), new ItemStack(CCItems.LOLLIPOP_SEEDS.get()));
    }

    private static void addCitizenTrades(MerchantOffers offers, RandomSource random) {
        addChance(offers, random, 2, 4, cost(CCItems.WAFFLE.get(), random, 5, 10), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 19));
        addChance(offers, random, 2, 3, cost(CCItems.CANDY_CANE.get(), random, 5, 10), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 19));
        addChance(offers, random, 1, 3, cost(CCItems.LICORICE.get(), random, 5, 10), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
        addChance(offers, random, 2, 5, cost(CCItems.HONEY_SHARD.get(), random, 30, 44), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
        if (random.nextInt(5) < 1) {
            if (random.nextBoolean()) {
                addTrade(offers, cost(CCBlocks.CARAMEL_BLOCK.get(), random, 10, 14), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
            } else {
                addTrade(offers, cost(CCBlocks.TRAMPOJELLY.get(), random, 10, 14), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
            }
        }
        addChance(offers, random, 3, 5, cost(CCItems.HONEY_ARROW.get(), random, 20, 24), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
        addChance(offers, random, 2, 5, cost(CCBlocks.MARSHMALLOW_LOG.get(), random, 20, 24), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
        addChance(offers, random, 2, 10, cost(CCItems.CHOCOLATE_COIN.get(), 64), cost(CCItems.CHOCOLATE_COIN.get(), 64), new ItemStack(CCItems.GINGERBREAD_EMBLEM.get()));
        ensureTrade(offers, cost(CCItems.CANDY_CANE.get(), 8), new ItemStack(CCItems.CHOCOLATE_COIN.get(), 8));
    }

    private static void addElderTrades(MerchantOffers offers) {
        addTrade(offers, cost(CCItems.PEZ.get(), 5), new ItemStack(CCItems.SKY_KEY.get()));
        addTrade(offers, cost(CCItems.PEZ.get(), 10), new ItemStack(CCItems.SKY_EMBLEM.get()));
        addTrade(offers, cost(CCItems.PEZ.get(), 20), new ItemStack(CCItems.RECORD_3.get()));
    }

    private static ItemStack cost(ItemLike item, int count) {
        return new ItemStack(item, count);
    }

    private static ItemStack cost(ItemLike item, RandomSource random, int min, int max) {
        return new ItemStack(item, min + random.nextInt(max - min + 1));
    }

    private static ItemStack result(ItemLike item, RandomSource random, int min, int max) {
        return cost(item, random, min, max);
    }

    private static void addChance(MerchantOffers offers, RandomSource random, int pass, int outOf, ItemStack cost, ItemStack result) {
        if (random.nextInt(outOf) < pass) {
            addTrade(offers, cost, result);
        }
    }

    private static void addChance(MerchantOffers offers, RandomSource random, int pass, int outOf, ItemStack firstCost, ItemStack secondCost, ItemStack result) {
        if (random.nextInt(outOf) < pass) {
            offers.add(new MerchantOffer(firstCost, secondCost, result, MAX_TRADE_USES, 2, 0.05F));
        }
    }

    private static void addTrade(MerchantOffers offers, ItemStack cost, ItemStack result) {
        offers.add(new MerchantOffer(cost, result, MAX_TRADE_USES, 2, 0.05F));
    }

    private static void ensureTrade(MerchantOffers offers, ItemStack cost, ItemStack result) {
        if (offers.isEmpty()) {
            addTrade(offers, cost, result);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GingerProfession", getGingerProfession());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GingerProfession")) {
            setGingerProfession(tag.getInt("GingerProfession"));
        } else if (tag.contains("SkinVariant")) {
            setGingerProfession(tag.getInt("SkinVariant"));
        }
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    public Component getDisplayName() {
        String key = switch (getGingerProfession()) {
            case BLACKSMITH -> "gingerbread.job.blacksmith";
            case FARMER -> "gingerbread.job.farmer";
            case ELDER -> "gingerbread.job.elder";
            default -> "gingerbread.job.citizen";
        };
        return Component.translatable(key);
    }

    private static final class GingerbreadAvoidPlayerGoal extends AvoidEntityGoal<Player> {
        private final GingerbreadManEntity gingerbread;

        private GingerbreadAvoidPlayerGoal(GingerbreadManEntity gingerbread) {
            super(gingerbread, Player.class, player -> player instanceof Player target && gingerbread.shouldAvoidPlayer(target), 16.0F, 0.8D, 1.33D, player -> true);
            this.gingerbread = gingerbread;
        }

        @Override
        public void tick() {
            super.tick();
            Player threat = getNearestPlayer();
            if (threat != null && gingerbread.distanceToSqr(threat) < 49.0D) {
                gingerbread.getNavigation().setSpeedModifier(1.33D);
            } else {
                gingerbread.getNavigation().setSpeedModifier(0.8D);
            }
        }

        @Nullable
        private Player getNearestPlayer() {
            return gingerbread.level().getNearestPlayer(gingerbread, 16.0D);
        }
    }
}
