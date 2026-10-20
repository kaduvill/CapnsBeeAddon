package com.kaduvill.capnsbeeaddon.compat.jei.RetrainingBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeOverviewRecipeRegistryPlugin;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.effects.EffectEffection;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.IAllele;
import forestry.apiculture.genetics.alleles.AlleleEffects;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** One Temporal example for each eligible bee type; native restoration runs only at registration. */
public final class RetrainingBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".retraining_bee";
    public static final int BASE_TICKS = 16;
    private static final long CYCLE_NANOS = 1_000_000_000L;

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;
    private final IIngredientRenderer<ItemStack> itemRenderer;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;
    private int pairIndex;

    private RetrainingBeeRecipe(List<ItemStack> inputs, List<ItemStack> outputs,
                                 IIngredientRenderer<ItemStack> itemRenderer) {
        if (inputs.size() != 2 || outputs.size() != 2) {
            throw new IllegalArgumentException("Missing Temporal princess/drone examples");
        }
        for (int i = 0; i < inputs.size(); i++) {
            ItemStack input = inputs.get(i);
            ItemStack output = outputs.get(i);
            if (input.isEmpty() || output == null || output.isEmpty()
                    || input.getItem() != output.getItem() || input.getMetadata() != output.getMetadata()
                    || input.getCount() != output.getCount()) {
                throw new IllegalArgumentException("Invalid Temporal retraining example");
            }
        }
        this.inputs = Collections.unmodifiableList(copies(inputs));
        this.outputs = Collections.unmodifiableList(copies(outputs));
        this.itemRenderer = Objects.requireNonNull(itemRenderer, "Item renderer");
    }

    public static RetrainingBeeRecipe read(IIngredientRenderer<ItemStack> itemRenderer) {
        return create(BeeManager.beeRoot.getTemplate(CareerBeeSpecies.ACCELERATION.get()), itemRenderer);
    }

    private static RetrainingBeeRecipe create(IAllele[] speciesTemplate,
                                              IIngredientRenderer<ItemStack> itemRenderer) {
        IAllele[] template = speciesTemplate.clone();
        template[EnumBeeChromosome.EFFECT.ordinal()] = AlleleEffects.effectNone;
        IBee example = BeeManager.beeRoot.templateAsIndividual(template);
        example.analyze();
        List<ItemStack> inputs = new ArrayList<>(2);
        List<ItemStack> outputs = new ArrayList<>(2);
        for (EnumBeeType type : new EnumBeeType[] {EnumBeeType.PRINCESS, EnumBeeType.DRONE}) {
            ItemStack input = BeeManager.beeRoot.getMemberStack(example, type);
            // This native overload only reads the target bee; it does not use a housing, world or RNG.
            ItemStack output = EffectEffection.INSTANCE.modifyStack(null, input.copy(), null);
            inputs.add(input);
            outputs.add(output);
        }
        return new RetrainingBeeRecipe(inputs, outputs, itemRenderer);
    }

    @Override
    public ItemStack getInput() { return inputs.get(0).copy(); }

    @Override
    public boolean acceptsInput(ItemStack stack) {
        // Overview navigation includes bees whose effect already matches their species.
        return BeeOverviewRecipeRegistryPlugin.isDroneOrPrincess(stack);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
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
        stacks.setOverrideDisplayFocus(null);
        PairDisplay display = new PairDisplay(stacks,
                ingredients.getInputs(VanillaTypes.ITEM).get(0),
                ingredients.getOutputs(VanillaTypes.ITEM).get(0));
        stacks.init(0, true, display.renderer(0), 18, 8, 18, 18, 1, 1);
        stacks.init(1, false, display.renderer(1), 124, 8, 18, 18, 1, 1);
        display.update();
        stacks.addTooltipCallback((slotIndex, isInput, ingredient, tooltip) -> {
            if (slotIndex == 0 || slotIndex == 1) {
                tooltip.add(TextFormatting.RED + I18n.format("jei.capnsbeeaddon.any_species"));
            }
        });
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        advanceCycle(System.nanoTime(), GuiScreen.isShiftKeyDown());
        int y = 30;
        for (String line : minecraft.fontRenderer.listFormattedStringToWidth(
                I18n.format("jei.capnsbeeaddon.retraining.description"), 80)) {
            minecraft.fontRenderer.drawString(line, (recipeWidth - minecraft.fontRenderer.getStringWidth(line)) / 2,
                    y, 0x808080);
            y += minecraft.fontRenderer.FONT_HEIGHT;
        }
    }

    private void advanceCycle(long now, boolean paused) {
        if (!paused) phaseNanos = (phaseNanos + now - lastDrawNanos) % (2 * CYCLE_NANOS);
        lastDrawNanos = now;
        pairIndex = (int) (phaseNanos / CYCLE_NANOS);
    }

    /** Per-layout state: previews cannot replace the main page's slot references. */
    private final class PairDisplay {
        private final IGuiItemStackGroup stacks;
        private final List<ItemStack> displayedInputs;
        private final List<ItemStack> displayedOutputs;
        private int displayedPair = -1;

        private PairDisplay(IGuiItemStackGroup stacks, List<ItemStack> inputs, List<ItemStack> outputs) {
            this.stacks = stacks;
            this.displayedInputs = inputs;
            this.displayedOutputs = outputs;
        }

        private void update() {
            if (displayedPair == pairIndex) return;
            // Native slots have independent timers. Singletons switch together only at presentation changes.
            stacks.set(0, displayedInputs.get(pairIndex).copy());
            stacks.set(1, displayedOutputs.get(pairIndex).copy());
            displayedPair = pairIndex;
        }

        private IIngredientRenderer<ItemStack> renderer(int slot) {
            return new IIngredientRenderer<ItemStack>() {
                @Override
                public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
                    update();
                    // A transition may replace the value passed into render. Read the actual native slot again.
                    ItemStack displayed = stacks.getGuiIngredients().get(slot).getDisplayedIngredient();
                    itemRenderer.render(minecraft, xPosition, yPosition, displayed);
                }

                @Override
                public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag tooltipFlag) {
                    return itemRenderer.getTooltip(minecraft, ingredient, tooltipFlag);
                }

                @Override
                public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
                    return itemRenderer.getFontRenderer(minecraft, ingredient);
                }
            };
        }
    }
}
