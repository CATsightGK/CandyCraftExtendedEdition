package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.SugarFactoryBlock;
import com.valentin4311.candycraftmod.menu.SugarFactoryMenu;
import com.valentin4311.candycraftmod.recipe.SugarFactoryRecipe;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItemTags;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
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
import net.minecraft.world.Containers;
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
    private static final int DEFAULT_PROCESS_TIME = 240;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : processTime;
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
    private int processTime = DEFAULT_PROCESS_TIME;
    private int pendingGlassBottles;
    private List<SugarFactoryRecipe> cachedRecipes = List.of();
    private ItemStack cachedInput = ItemStack.EMPTY;
    private ProcessRecipe cachedProcessRecipe;
    private boolean cachedAdvanced;
    private boolean hasCachedProcessRecipe;

    public SugarFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.SUGAR_FACTORY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SugarFactoryBlockEntity blockEntity) {
        ItemStack input = blockEntity.items.get(0);
        List<SugarFactoryRecipe> recipes = level.getRecipeManager().getAllRecipesFor(CCRecipeTypes.SUGAR_FACTORY_TYPE.get());
        ProcessRecipe recipe = blockEntity.getProcessRecipe(recipes, input);
        ItemStack result = recipe == null ? ItemStack.EMPTY : recipe.result();
        blockEntity.processTime = recipe == null ? DEFAULT_PROCESS_TIME : recipe.processingTime();
        boolean changed = false;

        if (recipe != null && blockEntity.canPlaceResult(result)) {
            blockEntity.progress += blockEntity.isAdvanced() ? 2 : 1;
            if (blockEntity.progress >= blockEntity.processTime) {
                blockEntity.craft(result, recipe.inputCount());
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
            if (slot == 0) {
                releasePendingGlassBottles();
            }
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = ContainerHelper.takeItem(items, slot);
        if (slot == 0) {
            releasePendingGlassBottles();
        }
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (slot == 0 && stack.isEmpty()) {
            releasePendingGlassBottles();
        }
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
        return slot == 0 && (acceptsCustomInput(stack) || !getFallbackResult(stack, isAdvanced()).isEmpty());
    }

    @Override
    public void clearContent() {
        items.clear();
        pendingGlassBottles = 0;
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
        pendingGlassBottles = tag.getInt("PendingGlassBottles");
        releasePendingGlassBottles();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        tag.putInt("PendingGlassBottles", pendingGlassBottles);
    }

    private boolean isAdvanced() {
        Block block = getBlockState().getBlock();
        return block instanceof SugarFactoryBlock sugarFactoryBlock && sugarFactoryBlock.isAdvanced();
    }

    public boolean isAdvancedFactory() {
        return isAdvanced();
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return processTime;
    }

    public ItemStack getExpectedResult() {
        if (level == null) {
            return ItemStack.EMPTY;
        }
        List<SugarFactoryRecipe> recipes = level.getRecipeManager()
            .getAllRecipesFor(CCRecipeTypes.SUGAR_FACTORY_TYPE.get());
        ProcessRecipe recipe = getProcessRecipe(recipes, items.get(0));
        return recipe == null ? ItemStack.EMPTY : recipe.result().copy();
    }

    public static List<DisplayRecipe> getDisplayRecipes(Level level) {
        List<DisplayRecipe> recipes = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        List<SugarFactoryRecipe> customRecipes = level == null ? List.of()
            : level.getRecipeManager().getAllRecipesFor(CCRecipeTypes.SUGAR_FACTORY_TYPE.get());
        if (level != null) {
            for (SugarFactoryRecipe recipe : customRecipes) {
                ItemStack[] candidates = recipe.ingredient().getItems();
                if (candidates.length == 0) {
                    continue;
                }
                ItemStack input = candidates[0].copy();
                input.setCount(recipe.ingredientCount());
                ItemStack output = recipe.resultForNetwork();
                String key = idText(ForgeRegistries.ITEMS.getKey(input.getItem()), input) + "->"
                    + idText(ForgeRegistries.ITEMS.getKey(output.getItem()), output) + ":"
                    + recipe.normalFactory() + ":" + recipe.advancedFactory();
                if (seen.add(key)) {
                    recipes.add(new DisplayRecipe(input, output, recipe.normalFactory(), recipe.advancedFactory(), recipe.getId()));
                }
            }
        }
        Set<Item> inputs = new LinkedHashSet<>();
        inputs.add(Items.STICK);
        addVanillaSugarInputs(inputs);
        ForgeRegistries.ITEMS.getValues().stream()
            .filter(SugarFactoryBlockEntity::isCandyCraftItem)
            .forEach(inputs::add);

        for (Item item : inputs) {
            ItemStack input = new ItemStack(item);
            ItemStack normalResult = customRecipes.stream().anyMatch(recipe -> recipe.normalFactory() && recipe.acceptsItem(input))
                ? ItemStack.EMPTY : getFallbackResult(input, false);
            ItemStack advancedResult = customRecipes.stream().anyMatch(recipe -> recipe.advancedFactory() && recipe.acceptsItem(input))
                ? ItemStack.EMPTY : getFallbackResult(input, true);
            if (!normalResult.isEmpty() && !advancedResult.isEmpty() && ItemStack.isSameItemSameTags(normalResult, advancedResult)) {
                addDisplayRecipe(recipes, seen, input, normalResult, true, true);
            } else {
                if (!normalResult.isEmpty()) {
                    addDisplayRecipe(recipes, seen, input, normalResult, true, false);
                }
                if (!advancedResult.isEmpty()) {
                    addDisplayRecipe(recipes, seen, input, advancedResult, false, true);
                }
            }
        }
        return List.copyOf(recipes);
    }

    private static void addDisplayRecipe(List<DisplayRecipe> recipes, Set<String> seen, ItemStack input, ItemStack output, boolean normalFactory, boolean advancedFactory) {
        ResourceLocation inputId = ForgeRegistries.ITEMS.getKey(input.getItem());
        ResourceLocation outputId = ForgeRegistries.ITEMS.getKey(output.getItem());
        String key = idText(inputId, input) + "->" + idText(outputId, output) + ":" + normalFactory + ":" + advancedFactory;
        if (seen.add(key)) {
            String path = sanitizeId((inputId == null ? input.getDescriptionId() : inputId.getPath()) + "_to_"
                + (outputId == null ? output.getDescriptionId() : outputId.getPath())
                + (advancedFactory ? "_advanced" : ""));
            recipes.add(new DisplayRecipe(input.copy(), output.copy(), normalFactory, advancedFactory, new ResourceLocation(CandyCraft.MODID, "sugar_factory/" + path)));
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

    private void craft(ItemStack result, int inputCount) {
        ItemStack output = items.get(1);
        if (output.isEmpty()) {
            items.set(1, result.copy());
        } else {
            output.grow(result.getCount());
        }

        ItemStack input = items.get(0);
        if (inputCount == 1 && input.is(Items.HONEY_BOTTLE)) {
            input.shrink(1);
            pendingGlassBottles++;
            if (input.isEmpty()) {
                items.set(0, ItemStack.EMPTY);
                releasePendingGlassBottles();
            }
        } else if (inputCount == 1 && (input.is(CCItems.CARAMEL_BUCKET.get()) || input.is(CCItems.GRENADINE_BUCKET.get()))) {
            items.set(0, new ItemStack(Items.BUCKET));
        } else {
            input.shrink(inputCount);
            if (input.isEmpty()) {
                items.set(0, ItemStack.EMPTY);
            }
        }
    }

    public void releasePendingGlassBottles() {
        if (pendingGlassBottles > 0 && items.get(0).isEmpty()) {
            items.set(0, new ItemStack(Items.GLASS_BOTTLE, pendingGlassBottles));
            pendingGlassBottles = 0;
            setChanged();
        }
    }

    public void dropPendingGlassBottles() {
        if (pendingGlassBottles > 0 && level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                new ItemStack(Items.GLASS_BOTTLE, pendingGlassBottles));
            pendingGlassBottles = 0;
            setChanged();
        }
    }

    private ProcessRecipe getProcessRecipe(List<SugarFactoryRecipe> recipes, ItemStack input) {
        boolean advanced = isAdvanced();
        if (hasCachedProcessRecipe && cachedAdvanced == advanced
                && cachedRecipes == recipes
                && cachedInput.getCount() == input.getCount()
                && ItemStack.isSameItemSameTags(cachedInput, input)) {
            return cachedProcessRecipe;
        }

        ProcessRecipe processRecipe = null;
        for (SugarFactoryRecipe recipe : recipes) {
            if (recipe.supportsFactory(advanced) && recipe.accepts(input)) {
                processRecipe = new ProcessRecipe(recipe.resultForNetwork(), recipe.ingredientCount(), recipe.processingTime());
                break;
            }
        }
        if (processRecipe == null) {
            ItemStack fallback = getFallbackResult(input, advanced);
            processRecipe = fallback.isEmpty() ? null : new ProcessRecipe(fallback, 1, DEFAULT_PROCESS_TIME);
        }

        cachedRecipes = recipes;
        cachedInput = input.copy();
        cachedAdvanced = advanced;
        cachedProcessRecipe = processRecipe;
        hasCachedProcessRecipe = true;
        return processRecipe;
    }

    private boolean acceptsCustomInput(ItemStack input) {
        return level != null && level.getRecipeManager().getAllRecipesFor(CCRecipeTypes.SUGAR_FACTORY_TYPE.get()).stream()
            .anyMatch(recipe -> recipe.supportsFactory(isAdvanced()) && recipe.acceptsItem(input));
    }

    private static ItemStack getFallbackResult(ItemStack input, boolean advanced) {
        if (input.isEmpty() || input.is(Items.SUGAR)) {
            return ItemStack.EMPTY;
        }

        Item item = input.getItem();
        if (item == Blocks.BEE_NEST.asItem() || item == Blocks.BEEHIVE.asItem()) {
            return ItemStack.EMPTY;
        }
        if (item == Items.STICK) {
            return new ItemStack(CCItems.MARSHMALLOW_STICK.get());
        }
        if (item == CCBlocks.FRAISE_TAGADA_FLOWER.get().asItem()) {
            return new ItemStack(CCItems.HONEY_SHARD.get());
        }
        if (item == CCBlocks.CHOCOLATE_STONE.get().asItem()) {
            return new ItemStack(CCItems.CHOCOLATE_COIN.get());
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

        return isCandyCraftItem(item) || input.is(CCItemTags.SWEET_FOODS) || isVanillaHoneyItem(item)
            || item == Items.SUGAR_CANE || item == Items.PAPER
            ? new ItemStack(Items.SUGAR) : ItemStack.EMPTY;
    }

    private static boolean isVanillaHoneyItem(Item item) {
        return item == Items.HONEY_BOTTLE || item == Items.HONEYCOMB
            || item == Blocks.HONEY_BLOCK.asItem() || item == Blocks.HONEYCOMB_BLOCK.asItem();
    }

    private static void addVanillaSugarInputs(Set<Item> inputs) {
        inputs.add(Items.COOKIE);
        inputs.add(Items.CAKE);
        inputs.add(Items.PUMPKIN_PIE);
        inputs.add(Items.HONEY_BOTTLE);
        inputs.add(Items.HONEYCOMB);
        inputs.add(Blocks.HONEY_BLOCK.asItem());
        inputs.add(Blocks.HONEYCOMB_BLOCK.asItem());
        inputs.add(Items.SWEET_BERRIES);
        inputs.add(Items.GLOW_BERRIES);
        inputs.add(Items.SUGAR_CANE);
        inputs.add(Items.PAPER);
    }

    private static boolean isCandyCraftItem(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id != null && CandyCraft.MODID.equals(id.getNamespace());
    }

    public record DisplayRecipe(ItemStack input, ItemStack output, boolean normalFactory, boolean advancedFactory, ResourceLocation id) {
    }

    private record ProcessRecipe(ItemStack result, int inputCount, int processingTime) {
    }
}
