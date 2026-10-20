package com.kaduvill.capnsbeeaddon.compat.jei.AssassinBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import forestry.apiculture.genetics.BeeDefinition;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** One Forest example of normal queen death, not a prediction of breeding results. */
public final class AssassinBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".assassin_bee";
    public static final int BASE_TICKS = 10;

    private final ItemStack input;
    private final ItemStack princess;
    private final ItemStack drone;
    private final IAlleleBeeSpecies assassinSpecies;
    private final IDrawable slot;

    private AssassinBeeRecipe(ItemStack input, ItemStack princess, ItemStack drone,
                              IAlleleBeeSpecies assassinSpecies, IDrawable slot) {
        if (input.isEmpty() || princess.isEmpty() || drone.isEmpty()) {
            throw new IllegalArgumentException("Missing Forest bee examples");
        }
        this.input = input.copy();
        this.princess = princess.copy();
        this.drone = drone.copy();
        this.assassinSpecies = Objects.requireNonNull(assassinSpecies, "Assassin species");
        this.slot = slot;
    }

    public static AssassinBeeRecipe read(IGuiHelper helper) {
        IBee forest = BeeDefinition.FOREST.getIndividual();
        IBee queen = (IBee) forest.copy();
        // A normal queen includes her mate. Do not simulate offspring or touch a world.
        queen.mate(forest);
        return new AssassinBeeRecipe(BeeManager.beeRoot.getMemberStack(queen, EnumBeeType.QUEEN),
                BeeManager.beeRoot.getMemberStack(forest, EnumBeeType.PRINCESS),
                BeeManager.beeRoot.getMemberStack(forest, EnumBeeType.DRONE),
                CareerBeeSpecies.ASSASSIN.get(), helper.getSlotDrawable());
    }

    @Override
    public ItemStack getInput() { return input.copy(); }

    @Override
    public boolean acceptsInput(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) return false;
        try {
            if (BeeManager.beeRoot.getType(stack) != EnumBeeType.QUEEN) return false;
            IBee bee = BeeManager.beeRoot.getMember(stack);
            // Mirrors EffectAssassin's species rule; housing is a world requirement, not an ingredient.
            return bee != null && bee.getGenome().getPrimary() != assassinSpecies;
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, input.copy());
        List<ItemStack> quantities = new ArrayList<>(4);
        for (int count = 1; count <= 4; count++) {
            ItemStack example = drone.copy();
            example.setCount(count);
            quantities.add(example);
        }
        // Separate outputs: princess and drones can occur together. Quantities illustrate built-in fertility genes.
        ingredients.setOutputLists(VanillaTypes.ITEM,
                Arrays.asList(Collections.singletonList(princess.copy()), quantities));
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(1, false, 104, 8);
        stacks.init(3, false, 124, 8);
        // Always show the cached Forest examples and keep quantity cycling when opened with R or U.
        stacks.setOverrideDisplayFocus(null);
        stacks.set(0, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        stacks.set(1, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
        stacks.set(3, ingredients.getOutputs(VanillaTypes.ITEM).get(1));
        stacks.addTooltipCallback((slotIndex, isInput, ingredient, tooltip) -> {
            if (slotIndex == 1 || slotIndex == 3) {
                tooltip.add(I18n.format(slotIndex == 1
                        ? "jei.capnsbeeaddon.assassin.princess" : "jei.capnsbeeaddon.assassin.drones"));
            }
            if (slotIndex == 0 || slotIndex == 1 || slotIndex == 3) {
                tooltip.add(TextFormatting.RED + I18n.format("jei.capnsbeeaddon.any_species"));
            }
        });
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        slot.draw(minecraft, 104, 8);
        String scope = I18n.format("jei.capnsbeeaddon.assassin.scope");
        int y = 30;
        for (String line : minecraft.fontRenderer.listFormattedStringToWidth(scope, recipeWidth)) {
            minecraft.fontRenderer.drawString(line, (recipeWidth - minecraft.fontRenderer.getStringWidth(line)) / 2,
                    y, 0x808080);
            y += minecraft.fontRenderer.FONT_HEIGHT;
        }
    }
}
