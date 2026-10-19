package com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter;

import com.rwtema.careerbees.items.ItemIngredients;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Snapshots the final ore group when JEI registers or rebuilds its recipes. */
public final class HoneySmelterRecipes {

    private HoneySmelterRecipes() {
    }

    public static List<Recipe> read() {
        return read(OreDictionary.getOres("ingotIron", false),
                ItemIngredients.IngredientType.INGOTHONEYCOLM.get());
    }

    static List<Recipe> read(Iterable<ItemStack> entries, ItemStack output) {
        if (output.isEmpty()) return Collections.emptyList();
        List<ItemStack> inputs = new ArrayList<>();
        Map<Item, Set<Integer>> seen = new IdentityHashMap<>();
        for (ItemStack entry : entries) {
            if (entry.isEmpty()) continue;
            if (seen.computeIfAbsent(entry.getItem(), item -> new HashSet<>()).add(entry.getMetadata())) {
                // Honey-Smelter ignores input count/NBT, but honors registered wildcard metadata.
                inputs.add(new ItemStack(entry.getItem(), 1, entry.getMetadata()));
            }
        }
        if (inputs.isEmpty()) return Collections.emptyList();
        return Collections.singletonList(new Recipe(inputs, output));
    }

    public static final class Recipe {
        private final List<ItemStack> inputs;
        private final ItemStack output;

        private Recipe(List<ItemStack> inputs, ItemStack output) {
            this.inputs = Collections.unmodifiableList(inputs);
            this.output = output.copy();
            this.output.setCount(1);
        }

        public boolean acceptsInput(ItemStack input) {
            if (input.isEmpty()) return false;
            for (ItemStack entry : inputs) {
                // Exactly the predicate used by EffectHoneyCombIngotConvert.
                if (OreDictionary.itemMatches(entry, input, false)) return true;
            }
            return false;
        }

        public List<ItemStack> getInputs() {
            List<ItemStack> copies = new ArrayList<>(inputs.size());
            for (ItemStack input : inputs) copies.add(input.copy());
            return copies;
        }

        public ItemStack getOutput() {
            return output.copy();
        }
    }
}
