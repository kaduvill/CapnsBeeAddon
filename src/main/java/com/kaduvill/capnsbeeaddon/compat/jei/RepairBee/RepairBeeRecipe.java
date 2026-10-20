package com.kaduvill.capnsbeeaddon.compat.jei.RepairBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import com.rwtema.careerbees.effects.EffectRepair;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One cached overview of vanilla repair examples; never simulates a complete repair during display. */
public final class RepairBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".repair_bee";
    public static final int BASE_TICKS = 20;

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;

    public RepairBeeRecipe() {
        List<ItemStack> damagedExamples = new ArrayList<>(5);
        List<ItemStack> repairedExamples = new ArrayList<>(5);
        for (Item item : new Item[] {Items.IRON_PICKAXE, Items.DIAMOND_SWORD, Items.IRON_CHESTPLATE,
                Items.BOW, Items.SHIELD}) {
            try {
                ItemStack damaged = new ItemStack(item);
                damaged.setItemDamage(Math.max(1, damaged.getMaxDamage() * 3 / 4));
                if (!EffectRepair.INSTANCE.acceptItemStack(damaged)) continue;
                ItemStack step = EffectRepair.INSTANCE.modifyStack(null, damaged, null);
                if (step == null || step.isEmpty() || step.getItem() != item
                        || step.getMetadata() != damaged.getMetadata() - 1
                        || step.getItemDamage() != damaged.getItemDamage() - 1) continue;

                // The native final operation supplies the repaired product without a durability-sized loop.
                ItemStack lastStep = new ItemStack(item);
                lastStep.setItemDamage(1);
                ItemStack repaired = EffectRepair.INSTANCE.modifyStack(null, lastStep, null);
                if (repaired == null || repaired.isEmpty() || repaired.getItem() != item
                        || repaired.getMetadata() != 0 || repaired.getItemDamage() != 0) continue;
                damagedExamples.add(damaged);
                repairedExamples.add(repaired);
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot display Repair Bee example for {}", item.getRegistryName(), failure);
            }
        }
        inputs = Collections.unmodifiableList(damagedExamples);
        outputs = Collections.unmodifiableList(repairedExamples);
    }

    @Override
    public ItemStack getInput() { return inputs.isEmpty() ? ItemStack.EMPTY : inputs.get(0).copy(); }

    @Override
    public boolean acceptsInput(ItemStack input) {
        if (input.isEmpty()) return false;
        for (ItemStack example : inputs) {
            if (input.getItem() == example.getItem()) return EffectRepair.INSTANCE.acceptItemStack(input);
        }
        return false;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Paired alternatives, not five simultaneous inputs or outputs.
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(copies(inputs)));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(copies(outputs)));
    }

    private static List<ItemStack> copies(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) copies.add(stack.copy());
        return copies;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        // Both slots use HEI's shared cycle offset and retain the same example order on R/U navigation.
        stacks.setOverrideDisplayFocus(null);
        stacks.set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        // Keep the caption beside the pedestal, inside the shared 160x50 layout.
        int y = 30;
        for (String line : minecraft.fontRenderer.listFormattedStringToWidth(
                I18n.format("jei.capnsbeeaddon.repair.per_operation"), 80)) {
            minecraft.fontRenderer.drawString(line, (recipeWidth - minecraft.fontRenderer.getStringWidth(line)) / 2,
                    y, 0x808080);
            y += minecraft.fontRenderer.FONT_HEIGHT;
        }
    }
}
