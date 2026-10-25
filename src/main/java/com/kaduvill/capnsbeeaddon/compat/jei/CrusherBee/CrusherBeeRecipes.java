package com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.rwtema.careerbees.recipes.OreConversion;
import com.rwtema.careerbees.recipes.OreRecipes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reads Career Bees 0.4.0's selected outputs without consuming world RNG. */
public final class CrusherBeeRecipes {

    private CrusherBeeRecipes() {
    }

    public static List<Recipe> read() {
        return read(ForgeRegistries.ITEMS);
    }

    static List<Recipe> read(Iterable<Item> items) {
        OreConversion conversion = OreRecipes.ORE_TO_DUST;
        Map<NBTTagCompound, Recipe> grouped = new LinkedHashMap<>();
        for (Item item : items) {
            try {
                for (int metadata : getMetadata(item, conversion)) {
                    ItemStack input = new ItemStack(item, 1, metadata);
                    if (input.isEmpty() || !conversion.isValid(input)) continue;
                    // EffectOreCrushing.acceptItemStack delegates to this same resolver.
                    ItemStack output = conversion.get(input);
                    if (output.isEmpty()) continue;
                    output.setCount(1);
                    // Include metadata, NBT and capabilities, not just the Ore Dictionary name.
                    NBTTagCompound key = output.serializeNBT();
                    Recipe recipe = grouped.get(key);
                    if (recipe == null) {
                        recipe = new Recipe(output);
                        grouped.put(key, recipe);
                    }
                    recipe.inputs.add(input);
                }
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot display Crusher Bee recipes for {}", item.getRegistryName(), failure);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(grouped.values()));
    }

    private static Set<Integer> getMetadata(Item item, OreConversion conversion) {
        Set<Integer> metadata = new LinkedHashSet<>();
        if (item.getHasSubtypes()) {
            // Mirror OreConversion's cache loader, including its NBT-free subtype filter
            // and metadata-zero fallback. Wildcard ore entries do not admit hidden subtypes.
            for (ItemStack stack : conversion.getStacks(item)) {
                if (!stack.hasTagCompound()) metadata.add(stack.getMetadata());
            }
        }
        if (metadata.isEmpty()) metadata.add(0);
        return metadata;
    }

    public static final class Recipe {
        private final List<ItemStack> inputs = new ArrayList<>();
        private final ItemStack output;

        private Recipe(ItemStack output) {
            this.output = output.copy();
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
