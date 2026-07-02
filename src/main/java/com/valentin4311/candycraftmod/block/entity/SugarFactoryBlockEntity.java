package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.SugarFactoryBlock;
import com.valentin4311.candycraftmod.menu.SugarFactoryMenu;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class SugarFactoryBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int[] SLOTS = new int[] { 0, 1 };
    private static final int PROCESS_TIME = 240;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : PROCESS_TIME;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress;

    public SugarFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.SUGAR_FACTORY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SugarFactoryBlockEntity blockEntity) {
        ItemStack input = blockEntity.items.get(0);
        ItemStack result = blockEntity.getRecipeResult(input);
        boolean changed = false;

        if (!input.isEmpty() && !result.isEmpty() && blockEntity.canPlaceResult(result)) {
            blockEntity.progress += blockEntity.isAdvanced() ? 2 : 1;
            if (blockEntity.progress >= PROCESS_TIME) {
                blockEntity.craft(result);
                blockEntity.progress = 0;
                changed = true;
            }
        } else if (blockEntity.progress != 0) {
            blockEntity.progress = 0;
            changed = true;
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(isAdvanced() ? "container.candycraftmod.advanced_sugar_factory" : "container.candycraftmod.sugar_factory");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SugarFactoryMenu(id, inventory, this, data, isAdvanced());
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && !getRecipeResult(stack).isEmpty();
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 1;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
    }

    private boolean isAdvanced() {
        Block block = getBlockState().getBlock();
        return block instanceof SugarFactoryBlock sugarFactoryBlock && sugarFactoryBlock.isAdvanced();
    }

    public boolean isAdvancedFactory() {
        return isAdvanced();
    }

    public static List<DisplayRecipe> getDisplayRecipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        Set<Item> inputs = new LinkedHashSet<>();
        inputs.add(Items.STICK);
        inputs.add(CCBlocks.FRAISE_TAGADA_FLOWER.get().asItem());
        inputs.add(CCBlocks.CHOCOLATE_STONE.get().asItem());
        inputs.add(CCBlocks.HONEYCOMB_BLOCK.get().asItem());
        inputs.add(CCBlocks.NOUGAT_BLOCK.get().asItem());
        inputs.add(CCBlocks.SUGAR_ESSENCE_FLOWER.get().asItem());
        CCItems.PORT_ITEMS.forEach(registryObject -> inputs.add(registryObject.get()));
        CCItems.BLOCK_ITEMS.forEach(registryObject -> inputs.add(registryObject.get()));
        CCSweetscapeItems.SIMPLE_ITEMS.forEach(registryObject -> inputs.add(registryObject.get()));
        CCSweetscapeItems.BLOCK_ITEMS.forEach(registryObject -> inputs.add(registryObject.get()));

        Set<String> seen = new LinkedHashSet<>();
        for (Item item : inputs) {
            ItemStack input = new ItemStack(item);
            ItemStack normalResult = getRecipeResult(input, false);
            if (!normalResult.isEmpty()) {
                addDisplayRecipe(recipes, seen, input, normalResult, false);
            }

            ItemStack advancedResult = getRecipeResult(input, true);
            if (!advancedResult.isEmpty() && (!ItemStack.isSameItemSameTags(normalResult, advancedResult) || advancedResult.is(Items.SUGAR))) {
                addDisplayRecipe(recipes, seen, input, advancedResult, true);
            }
        }
        return List.copyOf(recipes);
    }

    private static void addDisplayRecipe(List<DisplayRecipe> recipes, Set<String> seen, ItemStack input, ItemStack output, boolean advancedFactory) {
        ResourceLocation inputId = ForgeRegistries.ITEMS.getKey(input.getItem());
        ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(output.getItem());
        String key = idText(inputId, input) + "->" + idText(outputId, output) + ":" + advancedFactory;
        if (seen.add(key)) {
            String path = sanitizeId((inputId == null ? input.getDescriptionId() : inputId.getPath()) + "_to_"
                + (outputId == null ? output.getDescriptionId() : outputId.getPath())
                + (advancedFactory ? "_advanced" : ""));
            recipes.add(new DisplayRecipe(input.copy(), output.copy(), advancedFactory, new ResourceLocation(CandyCraft.MODID, "sugar_factory/" + path)));
        }
    }

    private static String idText(ResourceLocation id, ItemStack stack) {
        return id == null ? stack.getDescriptionId() : id.toString();
    }

    private static String sanitizeId(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
    }

    private boolean canPlaceResult(ItemStack result) {
        ItemStack output = items.get(1);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameTags(output, result) && output.getCount() + result.getCount() <= Math.min(output.getMaxStackSize(), getMaxStackSize());
    }

    private void craft(ItemStack result) {
        ItemStack output = items.get(1);
        if (output.isEmpty()) {
            items.set(1, result.copy());
        } else {
            output.grow(result.getCount());
        }

        ItemStack input = items.get(0);
        if (input.is(CCItems.CARAMEL_BUCKET.get()) || input.is(CCItems.GRENADINE_BUCKET.get())) {
            items.set(0, new ItemStack(Items.BUCKET));
        } else {
            input.shrink(1);
            if (input.isEmpty()) {
                items.set(0, ItemStack.EMPTY);
            }
        }
    }

    private ItemStack getRecipeResult(ItemStack input) {
        return getRecipeResult(input, isAdvanced());
    }

    private static ItemStack getRecipeResult(ItemStack input, boolean advanced) {
        if (input.isEmpty() || input.is(Items.SUGAR)) {
            return ItemStack.EMPTY;
        }

        Item item = input.getItem();
        if (item == Items.STICK) {
            return new ItemStack(CCItems.MARSHMALLOW_STICK.get());
        }
        if (item == CCBlocks.FRAISE_TAGADA_FLOWER.get().asItem()) {
            return new ItemStack(CCItems.HONEY_SHARD.get());
        }
        if (item == CCBlocks.CHOCOLATE_STONE.get().asItem()) {
            return new ItemStack(CCItems.CHOCOLATE_COIN.get());
        }
        if (item == CCBlocks.HONEYCOMB_BLOCK.get().asItem()) {
            return new ItemStack(CCItems.HONEYCOMB.get());
        }
        if (advanced && item == CCBlocks.NOUGAT_BLOCK.get().asItem()) {
            return new ItemStack(CCBlocks.NOUGAT_HEAD.get());
        }
        if (advanced && item == CCBlocks.SUGAR_ESSENCE_FLOWER.get().asItem()) {
            return new ItemStack(Items.GOLD_NUGGET);
        }

        if (item == Blocks.AIR.asItem()) {
            return ItemStack.EMPTY;
        }

        return isCandyCraftItem(item) ? new ItemStack(Items.SUGAR) : ItemStack.EMPTY;
    }

    private static boolean isCandyCraftItem(Item item) {
        return CCItems.PORT_ITEMS.stream().anyMatch(registryObject -> registryObject.get() == item)
            || CCItems.BLOCK_ITEMS.stream().anyMatch(registryObject -> registryObject.get() == item)
            || CCSweetscapeItems.SIMPLE_ITEMS.stream().anyMatch(registryObject -> registryObject.get() == item)
            || CCSweetscapeItems.BLOCK_ITEMS.stream().anyMatch(registryObject -> registryObject.get() == item);
    }

    public record DisplayRecipe(ItemStack input, ItemStack output, boolean advancedFactory, ResourceLocation id) {
    }
}
