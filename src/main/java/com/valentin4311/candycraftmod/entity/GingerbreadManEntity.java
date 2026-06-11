package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
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
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class GingerbreadManEntity extends Villager {
    private static final EntityDataAccessor<Integer> SKIN_VARIANT = SynchedEntityData.defineId(GingerbreadManEntity.class, EntityDataSerializers.INT);

    public GingerbreadManEntity(EntityType<? extends GingerbreadManEntity> type, Level level) {
        super(type, level);
        setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new AvoidEntityGoal<Player>(this, Player.class,
            entity -> entity instanceof Player player && shouldAvoidPlayer(player), 10.0F, 1.65D, 2.1D, entity -> true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN_VARIANT, 0);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        setSkinVariant(getRandom().nextInt(4));
        updateTrades();
        return data;
    }

    public int getSkinVariant() {
        return entityData.get(SKIN_VARIANT);
    }

    public void setSkinVariant(int variant) {
        entityData.set(SKIN_VARIANT, Math.max(0, Math.min(3, variant)));
    }

    private boolean shouldAvoidPlayer(Player player) {
        return !isTrading() && !player.isCreative() && !player.isSpectator()
            && !player.getInventory().contains(CCItems.GINGERBREAD_EMBLEM.get().getDefaultInstance());
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = getOffers();
        if (!offers.isEmpty()) {
            return;
        }
        RandomSource random = getRandom();
        addBlacksmithTrades(offers, random);
        addFarmerTrades(offers, random);
        addCitizenTrades(offers, random);
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
        addChance(offers, random, 2, 3, cost(CCItems.CHOCOLATE_COIN.get(), random, 8, 17), result(CCItems.LOLLIPOP.get(), random, 3, 10));
        addChance(offers, random, 1, 8, cost(CCItems.CHOCOLATE_COIN.get(), random, 15, 34), new ItemStack(CCBlocks.LOLLIPOP_BLOCK.get()));
        ensureTrade(offers, cost(CCItems.CHOCOLATE_COIN.get(), 28), new ItemStack(CCItems.LOLLIPOP_SEEDS.get()));
    }

    private static void addCitizenTrades(MerchantOffers offers, RandomSource random) {
        addChance(offers, random, 2, 4, cost(CCItems.WAFFLE.get(), random, 5, 10), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 19));
        addChance(offers, random, 2, 3, cost(CCItems.CANDY_CANE.get(), random, 5, 10), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 19));
        addChance(offers, random, 1, 3, cost(CCItems.LICORICE.get(), random, 5, 10), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
        addChance(offers, random, 2, 5, cost(CCItems.HONEY_SHARD.get(), random, 30, 44), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
        addChance(offers, random, 3, 5, cost(CCItems.HONEY_ARROW.get(), random, 20, 24), result(CCItems.CHOCOLATE_COIN.get(), random, 5, 24));
        addChance(offers, random, 2, 10, cost(CCItems.CHOCOLATE_COIN.get(), 64), cost(CCItems.CHOCOLATE_COIN.get(), 64), new ItemStack(CCItems.GINGERBREAD_EMBLEM.get()));
        ensureTrade(offers, cost(CCItems.CANDY_CANE.get(), 8), new ItemStack(CCItems.CHOCOLATE_COIN.get(), 8));
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
            offers.add(new MerchantOffer(firstCost, secondCost, result, 12, 2, 0.05F));
        }
    }

    private static void addTrade(MerchantOffers offers, ItemStack cost, ItemStack result) {
        offers.add(new MerchantOffer(cost, result, 12, 2, 0.05F));
    }

    private static void ensureTrade(MerchantOffers offers, ItemStack cost, ItemStack result) {
        if (offers.isEmpty()) {
            addTrade(offers, cost, result);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinVariant", getSkinVariant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SkinVariant")) {
            setSkinVariant(tag.getInt("SkinVariant"));
        }
    }
}
