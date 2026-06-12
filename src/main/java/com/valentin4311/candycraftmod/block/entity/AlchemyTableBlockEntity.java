package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.alchemy.AlchemyMixing;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AlchemyTableBlockEntity extends BlockEntity {
    private static final int MAX_LIQUID_UNITS = 6;
    private static final int BREW_TIME_TICKS = 20 * 10;
    private static final int ADVANCED_BREW_TIME_TICKS = 20 * 5;
    private AlchemyLiquidKind liquidKind = AlchemyLiquidKind.NONE;
    private boolean topFilled;
    private int liquidAmount;
    private boolean hasMixerPower;
    private boolean hasMixerSugar;
    private boolean hasAdvancedMixerSugar;
    private int mixerSugarCharges;
    private int brewTicks;
    private int sugarScanTicks;
    private NonNullList<ItemStack> ingredients = NonNullList.withSize(4, ItemStack.EMPTY);

    public AlchemyTableBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.ALCHEMY_TABLE.get(), pos, state);
    }

    public boolean isTopFilled() {
        return liquidKind != AlchemyLiquidKind.NONE && topFilled;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlchemyTableBlockEntity blockEntity) {
        if (++blockEntity.sugarScanTicks >= 10) {
            blockEntity.sugarScanTicks = 0;
            blockEntity.updateMixerState();
        }

        blockEntity.tickBrewing();
    }

    private void tickBrewing() {
        if (level == null) {
            return;
        }

        if (liquidKind != AlchemyLiquidKind.GRENADINE || !isTopFilled() || getIngredientCount() < AlchemyMixing.INPUT_SLOTS || !isMixing() || !hasCraftableRecipe()) {
            resetBrewTicks();
            return;
        }

        if (!hasMixerCatalyst()) {
            resetBrewTicks();
            return;
        }

        int requiredTicks = hasAdvancedMixerSugar ? ADVANCED_BREW_TIME_TICKS : BREW_TIME_TICKS;
        brewTicks++;
        if (brewTicks >= requiredTicks) {
            finishBrew();
        }
    }

    public void setTopFilled(boolean topFilled) {
        this.topFilled = topFilled;
        if (topFilled && liquidKind == AlchemyLiquidKind.NONE) {
            liquidKind = AlchemyLiquidKind.GRENADINE;
        } else if (!topFilled && liquidAmount <= 0) {
            liquidKind = AlchemyLiquidKind.NONE;
        }
        sync();
    }

    public AlchemyLiquidKind getLiquidKind() {
        return liquidKind;
    }

    public int getLiquidAmount() {
        return liquidAmount;
    }

    public int getDisplayedSyrupUnits() {
        if (liquidKind == AlchemyLiquidKind.NONE) {
            return 0;
        }
        return Math.max(0, Math.min(MAX_LIQUID_UNITS, liquidAmount + (topFilled ? 1 : 0)));
    }

    public void setLiquidAmount(int liquidAmount) {
        this.liquidAmount = Math.max(0, Math.min(MAX_LIQUID_UNITS - 1, liquidAmount));
        if (this.liquidAmount <= 0 && !topFilled) {
            liquidKind = AlchemyLiquidKind.NONE;
        }
        sync();
    }

    public boolean canAddLiquid(AlchemyLiquidKind kind) {
        return kind != AlchemyLiquidKind.NONE
            && getIngredientCount() == 0
            && brewTicks == 0
            && mixerSugarCharges == 0
            && (liquidKind == AlchemyLiquidKind.NONE || liquidKind == kind)
            && getDisplayedSyrupUnits() < MAX_LIQUID_UNITS;
    }

    public boolean addLiquid(AlchemyLiquidKind kind) {
        if (level == null || level.isClientSide || !canAddLiquid(kind)) {
            return false;
        }

        if (liquidKind == AlchemyLiquidKind.NONE) {
            liquidKind = kind;
            topFilled = true;
            liquidAmount = 0;
        } else if (!topFilled) {
            topFilled = true;
        } else {
            liquidAmount = Math.min(MAX_LIQUID_UNITS - 1, liquidAmount + 1);
        }
        sync();
        return true;
    }

    public ItemStack removeLiquidBucket() {
        if (level == null || level.isClientSide || liquidKind == AlchemyLiquidKind.NONE || getIngredientCount() > 0 || brewTicks > 0 || mixerSugarCharges > 0) {
            return ItemStack.EMPTY;
        }

        ItemStack bucket = liquidKind.bucket();
        if (liquidAmount > 0) {
            liquidAmount--;
        } else {
            topFilled = false;
            liquidKind = AlchemyLiquidKind.NONE;
        }
        sync();
        return bucket;
    }

    public int getIngredientCount() {
        int count = 0;
        for (ItemStack ingredient : ingredients) {
            if (!ingredient.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasMixerSugar() {
        return hasMixerSugar;
    }

    public int getBrewTicks() {
        return brewTicks;
    }

    public int getRequiredBrewTicks() {
        return hasAdvancedMixerSugar ? ADVANCED_BREW_TIME_TICKS : BREW_TIME_TICKS;
    }

    public boolean isMixing() {
        return hasMixerPower && !isRedstonePaused();
    }

    public boolean isFastMixing() {
        return isMixing() && mixerSugarCharges > 0;
    }

    private boolean isRedstonePaused() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    public List<ItemStack> getIngredientsForRender() {
        List<ItemStack> items = new ArrayList<>(ingredients.size());
        for (ItemStack ingredient : ingredients) {
            items.add(ingredient.copy());
        }
        return Collections.unmodifiableList(items);
    }

    public ItemStack removeLastIngredient() {
        if (level == null || level.isClientSide || brewTicks > 0 || mixerSugarCharges > 0) {
            return ItemStack.EMPTY;
        }

        for (int i = ingredients.size() - 1; i >= 0; i--) {
            ItemStack ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                ItemStack result = ingredient.copy();
                ingredients.set(i, ItemStack.EMPTY);
                sync();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean addManualMixerSugar(ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty() || !stack.is(Items.SUGAR)
            || !canAcceptManualMixerSugar()) {
            return false;
        }

        mixerSugarCharges = 1;
        hasMixerSugar = true;
        hasAdvancedMixerSugar = false;
        sync();
        return true;
    }

    public boolean canAcceptManualMixerSugar() {
        if (level == null) {
            return false;
        }
        updateMixerState();
        return isTopFilled()
            && liquidKind == AlchemyLiquidKind.GRENADINE
            && getIngredientCount() >= AlchemyMixing.INPUT_SLOTS
            && mixerSugarCharges <= 0
            && isMixing()
            && hasCraftableRecipe();
    }

    public boolean addIngredient(ItemStack stack) {
        if (level == null || level.isClientSide || stack.isEmpty() || liquidKind != AlchemyLiquidKind.GRENADINE || !isTopFilled() || !AlchemyMixing.isValidIngredient(stack)) {
            return false;
        }

        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i).isEmpty()) {
                ingredients.set(i, stack.copyWithCount(1));
                sync();
                return true;
            }
        }
        return false;
    }

    private boolean hasCraftableRecipe() {
        if (getIngredientCount() < AlchemyMixing.INPUT_SLOTS) {
            return false;
        }

        List<ItemStack> inputs = new ArrayList<>(4);
        for (ItemStack ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                return false;
            }
            inputs.add(ingredient);
        }
        return !AlchemyMixing.craft(inputs).isEmpty();
    }

    private void finishBrew() {
        if (level == null) {
            return;
        }

        List<ItemStack> inputs = new ArrayList<>(4);
        for (ItemStack ingredient : ingredients) {
            if (ingredient.isEmpty()) {
                return;
            }
            inputs.add(ingredient);
        }

        ItemStack result = AlchemyMixing.craft(inputs);
        if (result.isEmpty()) {
            resetBrewTicks();
            return;
        }
        if (mixerSugarCharges <= 0) {
            resetBrewTicks();
            return;
        }

        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.85D;
        double z = worldPosition.getZ() + 0.5D;
        ItemEntity itemEntity = new ItemEntity(level, x, y, z, result);
        itemEntity.setPickUpDelay(10);
        level.addFreshEntity(itemEntity);

        clearIngredients();
        mixerSugarCharges = 0;
        hasAdvancedMixerSugar = false;
        brewTicks = 0;
        setTopFilled(false);
        if (liquidAmount > 0) {
            liquidAmount--;
            setTopFilled(true);
        } else {
            liquidKind = AlchemyLiquidKind.NONE;
        }
        sync();
    }

    private void resetBrewTicks() {
        if (brewTicks != 0) {
            brewTicks = 0;
            sync();
        }
    }

    private boolean hasMixerCatalyst() {
        if (mixerSugarCharges > 0) {
            hasMixerSugar = true;
            return true;
        }
        return pullMixerSugarFromFactory();
    }

    private boolean pullMixerSugarFromFactory() {
        if (level == null) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor instanceof SugarFactoryBlockEntity factory) {
                ItemStack output = factory.getItem(1);
                if (output.is(Items.SUGAR)) {
                    factory.removeItem(1, 1);
                    factory.setChanged();
                    mixerSugarCharges = 1;
                    hasMixerSugar = true;
                    hasAdvancedMixerSugar = factory.isAdvancedFactory();
                    updateMixerState();
                    return true;
                }
            }
        }
        hasMixerSugar = false;
        hasAdvancedMixerSugar = false;
        return false;
    }

    private void updateMixerState() {
        MixerState state = scanMixerState();
        boolean sugarAvailable = mixerSugarCharges > 0 || state.hasSugar();
        boolean advancedSugar = mixerSugarCharges > 0 ? hasAdvancedMixerSugar : state.hasAdvancedSugar();
        if (state.hasPower() != hasMixerPower || sugarAvailable != hasMixerSugar || advancedSugar != hasAdvancedMixerSugar) {
            hasMixerPower = state.hasPower();
            hasMixerSugar = sugarAvailable;
            hasAdvancedMixerSugar = advancedSugar;
            sync();
        }
    }

    private MixerState scanMixerState() {
        if (level == null) {
            return new MixerState(false, false, false);
        }

        boolean power = false;
        boolean sugar = false;
        boolean advancedSugar = false;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (isCandyCaneBlock(neighborState)) {
                power = true;
            }

            BlockEntity neighbor = level.getBlockEntity(neighborPos);
            if (neighbor instanceof SugarFactoryBlockEntity factory) {
                power = true;
                if (factory.getItem(1).is(Items.SUGAR)) {
                    sugar = true;
                    if (factory.isAdvancedFactory()) {
                        advancedSugar = true;
                    }
                }
            }
        }
        return new MixerState(power, sugar, advancedSugar);
    }

    private static boolean isCandyCaneBlock(BlockState state) {
        return state.is(CCBlocks.CANDY_CANE_BLOCK.get())
            || state.is(CCSweetscapeBlocks.WHITE_CANDY_CANE_BLOCK.get())
            || state.is(CCSweetscapeBlocks.RED_CANDY_CANE_BLOCK.get())
            || state.is(CCSweetscapeBlocks.GREEN_CANDY_CANE_BLOCK.get())
            || state.is(CCSweetscapeBlocks.WHITE_RED_CANDY_CANE_BLOCK.get())
            || state.is(CCSweetscapeBlocks.WHITE_GREEN_CANDY_CANE_BLOCK.get())
            || state.is(CCSweetscapeBlocks.RED_GREEN_CANDY_CANE_BLOCK.get());
    }

    public void clearIngredients() {
        for (int i = 0; i < ingredients.size(); i++) {
            ingredients.set(i, ItemStack.EMPTY);
        }
        sync();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        topFilled = tag.getBoolean("TopFilled");
        liquidAmount = tag.getInt("LiquidAmount");
        if (tag.contains("LiquidKind")) {
            liquidKind = AlchemyLiquidKind.byId(tag.getString("LiquidKind"));
        } else {
            liquidKind = topFilled || liquidAmount > 0 ? AlchemyLiquidKind.GRENADINE : AlchemyLiquidKind.NONE;
        }
        if (liquidKind == AlchemyLiquidKind.NONE) {
            topFilled = false;
            liquidAmount = 0;
        }
        hasMixerPower = tag.getBoolean("HasMixerPower");
        hasMixerSugar = tag.getBoolean("HasMixerSugar");
        hasAdvancedMixerSugar = tag.getBoolean("HasAdvancedMixerSugar");
        mixerSugarCharges = tag.getInt("MixerSugarCharges");
        brewTicks = tag.getInt("BrewTicks");
        ingredients = NonNullList.withSize(4, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, ingredients);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("LiquidKind", liquidKind.id());
        tag.putBoolean("TopFilled", topFilled);
        tag.putInt("LiquidAmount", liquidAmount);
        tag.putBoolean("HasMixerPower", hasMixerPower);
        tag.putBoolean("HasMixerSugar", hasMixerSugar);
        tag.putBoolean("HasAdvancedMixerSugar", hasAdvancedMixerSugar);
        tag.putInt("MixerSugarCharges", mixerSugarCharges);
        tag.putInt("BrewTicks", brewTicks);
        ContainerHelper.saveAllItems(tag, ingredients);
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private record MixerState(boolean hasPower, boolean hasSugar, boolean hasAdvancedSugar) {
    }
}
