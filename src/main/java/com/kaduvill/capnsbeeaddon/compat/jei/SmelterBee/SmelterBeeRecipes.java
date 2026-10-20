package com.kaduvill.capnsbeeaddon.compat.jei.SmelterBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** JEI presentation of the furnace registry; native lookup remains authoritative. */
public final class SmelterBeeRecipes {
    public static final String UID = CapnsBeeAddon.MODID + ".smelter_bee";
    private final Function<ItemStack, ItemStack> lookup;
    private final List<Recipe> recipes;
    private final Map<Item, List<Recipe>> byInput;
    private final Map<Item, List<Recipe>> byOutput;

    public static SmelterBeeRecipes read(IStackHelper stackHelper) {
        FurnaceRecipes furnace = FurnaceRecipes.instance();
        return new SmelterBeeRecipes(furnace.getSmeltingList(), furnace::getSmeltingResult, stackHelper);
    }

    SmelterBeeRecipes(Map<ItemStack, ItemStack> entries, Function<ItemStack, ItemStack> lookup,
                      IStackHelper stackHelper) {
        this.lookup = lookup;
        Map<Item, List<Mapping>> mappingsByItem = new IdentityHashMap<>();
        Map<Item, Set<Integer>> seen = new IdentityHashMap<>();
        List<Mapping> mappings = new ArrayList<>();
        // Preserve furnace iteration order: an exact metadata entry does not necessarily beat a wildcard.
        for (Map.Entry<ItemStack, ItemStack> entry : entries.entrySet()) {
            ItemStack input = entry.getKey();
            ItemStack output = entry.getValue();
            if (input == null || input.isEmpty()) continue;
            if (!seen.computeIfAbsent(input.getItem(), item -> new HashSet<>()).add(input.getMetadata())) continue;
            if (output == null || output.isEmpty()) continue;
            try {
                Mapping mapping = new Mapping(new ItemStack(input.getItem(), 1, input.getMetadata()), output.copy());
                mappings.add(mapping);
                mappingsByItem.computeIfAbsent(input.getItem(), item -> new ArrayList<>()).add(mapping);
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot display Smelter Bee recipe for {}", input.getItem().getRegistryName(), failure);
            }
        }
        List<Recipe> completed = new ArrayList<>();
        for (Mapping mapping : mappings) {
            try {
                List<Mapping> candidates = mappingsByItem.get(mapping.input.getItem());
                List<ItemStack> inputs = new ArrayList<>();
                Set<Integer> metadata = new HashSet<>();
                for (ItemStack subtype : stackHelper.getSubtypes(mapping.input.copy())) {
                    if (subtype.isEmpty() || subtype.getItem() != mapping.input.getItem()
                            || subtype.getMetadata() == OreDictionary.WILDCARD_VALUE) continue;
                    ItemStack input = new ItemStack(subtype.getItem(), 1, subtype.getMetadata());
                    if (metadata.add(input.getMetadata()) && selected(candidates, input) == mapping) inputs.add(input);
                }
                if (inputs.isEmpty()) {
                    ItemStack input = mapping.input.copy();
                    if (input.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                        // A wildcard can accept metadata omitted from creative tabs. Choose an unshadowed example.
                        Set<Integer> exact = seen.get(input.getItem());
                        int value = 0;
                        while (value == OreDictionary.WILDCARD_VALUE || exact.contains(value)) value++;
                        input = new ItemStack(input.getItem(), 1, value);
                    }
                    if (selected(candidates, input) == mapping) inputs.add(input);
                }
                if (!inputs.isEmpty()) completed.add(new Recipe(mapping, inputs));
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot display Smelter Bee recipe for {}", mapping.input.getItem().getRegistryName(), failure);
            }
        }
        recipes = Collections.unmodifiableList(completed);
        Map<Item, List<Recipe>> inputs = new IdentityHashMap<>();
        Map<Item, List<Recipe>> outputs = new IdentityHashMap<>();
        for (Recipe recipe : recipes) {
            inputs.computeIfAbsent(recipe.mapping.input.getItem(), item -> new ArrayList<>()).add(recipe);
            outputs.computeIfAbsent(recipe.mapping.output.getItem(), item -> new ArrayList<>()).add(recipe);
        }
        byInput = freeze(inputs);
        byOutput = freeze(outputs);
    }

    public List<Recipe> getRecipes() { return recipes; }

    /** One native lookup per input query, then only that item's cached mappings are examined. */
    Recipe findInput(ItemStack input) {
        if (input.isEmpty()) return null;
        List<Recipe> candidates = byInput.get(input.getItem());
        if (candidates == null) return null;
        ItemStack result = result(input);
        if (result.isEmpty()) return null;
        for (Recipe candidate : candidates) {
            if (matches(candidate.mapping.input, input)
                    && ItemStack.areItemStacksEqual(candidate.mapping.output, result)) return candidate;
        }
        return null;
    }

    List<Recipe> findOutputs(ItemStack output, IStackHelper stackHelper) {
        if (output.isEmpty()) return Collections.emptyList();
        List<Recipe> candidates = byOutput.get(output.getItem());
        if (candidates == null) return Collections.emptyList();
        List<Recipe> found = new ArrayList<>();
        for (Recipe candidate : candidates) {
            if (stackHelper.isEquivalent(candidate.mapping.output, output)) found.add(candidate);
        }
        return found;
    }

    private Mapping selected(List<Mapping> candidates, ItemStack input) {
        ItemStack result = result(input);
        if (result.isEmpty()) return null;
        for (Mapping candidate : candidates) {
            if (matches(candidate.input, input) && ItemStack.areItemStacksEqual(candidate.output, result)) return candidate;
        }
        return null;
    }

    private ItemStack result(ItemStack input) {
        try {
            ItemStack result = lookup.apply(input);
            return result == null ? ItemStack.EMPTY : result;
        } catch (RuntimeException | LinkageError failure) {
            return ItemStack.EMPTY;
        }
    }

    private static boolean matches(ItemStack pattern, ItemStack input) {
        return pattern.getItem() == input.getItem() && (pattern.getMetadata() == OreDictionary.WILDCARD_VALUE
                || pattern.getMetadata() == input.getMetadata());
    }

    private static <T> Map<Item, List<T>> freeze(Map<Item, List<T>> index) {
        index.replaceAll((item, entries) -> Collections.unmodifiableList(entries));
        return Collections.unmodifiableMap(index);
    }

    private static ItemStack one(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static final class Mapping {
        private final ItemStack input;
        private final ItemStack output;

        private Mapping(ItemStack input, ItemStack output) {
            this.input = input;
            this.output = output;
        }
    }

    public final class Recipe implements BeeEffectRecipe {
        private final Mapping mapping;
        private final List<ItemStack> inputs;

        private Recipe(Mapping mapping, List<ItemStack> inputs) {
            this.mapping = mapping;
            this.inputs = Collections.unmodifiableList(inputs);
        }

        @Override
        public ItemStack getInput() { return inputs.get(0).copy(); }

        @Override
        public boolean acceptsInput(ItemStack input) { return findInput(input) == this; }

        @Override
        public void getIngredients(IIngredients ingredients) {
            List<ItemStack> copies = new ArrayList<>(inputs.size());
            for (ItemStack input : inputs) copies.add(input.copy());
            ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(copies));
            ingredients.setOutput(VanillaTypes.ITEM, mapping.output.copy());
        }

        @Override
        public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
            // An output focus must not replace the registered result's quantity or NBT.
            layout.getItemStacks().setOverrideDisplayFocus(null);
            layout.getItemStacks().set(ingredients);
            IFocus<?> focus = layout.getFocus();
            if (focus != null && focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof ItemStack) {
                ItemStack input = (ItemStack) focus.getValue();
                if (acceptsInput(input)) layout.getItemStacks().set(0, one(input));
            }
            layout.getItemStacks().set(1, mapping.output.copy());
        }
    }
}
