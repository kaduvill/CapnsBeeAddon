package com.kaduvill.capnsbeeaddon.compat.jei.LumberBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catalog of logWood drop variants; never simulates a harvest or reads a world. */
public final class LumberBeeRecipes {
    public static final String UID = CapnsBeeAddon.MODID + ".lumber_bee";

    private LumberBeeRecipes() {}

    public static List<Recipe> read(Iterable<ItemStack> entries, IIngredientHelper<ItemStack> helper, ItemStack bark) {
        if (bark.isEmpty()) return Collections.emptyList();
        Map<NBTTagCompound, Recipe> recipes = new LinkedHashMap<>();
        for (ItemStack entry : entries) {
            if (entry.isEmpty()) continue;
            try {
                ItemStack target = one(entry);
                List<ItemStack> variants = helper.expandSubtypes(Collections.singletonList(target));
                if (variants.isEmpty()) variants = Collections.singletonList(new ItemStack(target.getItem()));
                for (ItemStack variant : variants) {
                    if (variant.isEmpty() || variant.getMetadata() == OreDictionary.WILDCARD_VALUE) continue;
                    ItemStack input = one(variant);
                    NBTTagCompound key = input.serializeNBT();
                    if (!recipes.containsKey(key) || target.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                        recipes.put(key, new Recipe(target, input, bark));
                    }
                }
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot display Lumber Bee bark for {}", entry.getItem().getRegistryName(), failure);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(recipes.values()));
    }

    public static ItemStack sourceOf(ItemStack output, ItemStack bark) {
        if (output.isEmpty() || output.getItem() != bark.getItem() || output.getMetadata() != bark.getMetadata()
                || !output.hasTagCompound() || !output.getTagCompound().hasKey("bark", 10)) return ItemStack.EMPTY;
        try {
            return new ItemStack(output.getTagCompound().getCompoundTag("bark"));
        } catch (RuntimeException failure) {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack barkFor(ItemStack log, ItemStack bark) {
        ItemStack output = one(bark);
        // EffectLumber stores the complete harvested log stack here, including its count/NBT.
        output.setTagInfo("bark", log.serializeNBT());
        return output;
    }

    private static ItemStack one(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    public static final class Recipe implements BeeEffectRecipe {
        private final ItemStack target;
        private final ItemStack input;
        private final ItemStack bark;

        private Recipe(ItemStack target, ItemStack input, ItemStack bark) {
            this.target = target.copy();
            this.input = input.copy();
            this.bark = one(bark);
        }

        @Override
        public ItemStack getInput() { return input.copy(); }

        @Override
        public boolean acceptsInput(ItemStack stack) {
            return !stack.isEmpty() && OreDictionary.itemMatches(target, stack, false);
        }

        private List<ItemStack> outputs(ItemStack log) {
            List<ItemStack> quantities = new ArrayList<>(8);
            for (int count = 1; count <= 8; count++) {
                ItemStack output = barkFor(log, bark);
                output.setCount(count);
                quantities.add(output);
            }
            return quantities;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInput(VanillaTypes.ITEM, input.copy());
            ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(outputs(input)));
        }

        @Override
        public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
            IFocus<?> focus = layout.getFocus();
            if (focus != null && focus.getMode() == IFocus.Mode.OUTPUT) {
                layout.getItemStacks().setOverrideDisplayFocus(null);
            }
            layout.getItemStacks().set(ingredients);
            if (focus != null && focus.getValue() instanceof ItemStack) {
                ItemStack stack = (ItemStack) focus.getValue();
                ItemStack source = focus.getMode() == IFocus.Mode.INPUT ? one(stack) : sourceOf(stack, bark);
                if (acceptsInput(source)) {
                    layout.getItemStacks().set(0, one(source));
                    layout.getItemStacks().set(1, outputs(source));
                }
            }
            layout.getItemStacks().addTooltipCallback((index, isInput, stack, tooltip) -> {
                if (index == 0) tooltip.add(I18n.format("jei.capnsbeeaddon.lumber.input"));
                if (index == 1) tooltip.add(I18n.format("jei.capnsbeeaddon.output_range", 1, 8));
            });
        }
    }
}
