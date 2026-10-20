package com.kaduvill.capnsbeeaddon.compat.jei.PriestBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import com.rwtema.careerbees.effects.EffectPriest;
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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** One overview with five vanilla examples; native enchanting runs only during registration. */
public final class PriestBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".priest_bee";
    public static final int BASE_TICKS = 100;
    private static final long CYCLE_NANOS = 1_000_000_000L;

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;
    private final IIngredientRenderer<ItemStack> itemRenderer;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;
    private int pairIndex;

    public PriestBeeRecipe(IIngredientRenderer<ItemStack> itemRenderer) {
        this.itemRenderer = Objects.requireNonNull(itemRenderer, "Item renderer");
        List<ItemStack> cleanExamples = new ArrayList<>(5);
        List<ItemStack> enchantedExamples = new ArrayList<>(5);
        for (Item item : new Item[] {Items.IRON_PICKAXE, Items.DIAMOND_SWORD, Items.IRON_CHESTPLATE,
                Items.BOW, Items.FISHING_ROD}) {
            try {
                ItemStack input = new ItemStack(item);
                if (!EffectPriest.INSTANCE.acceptItemStack(input) || item.getItemEnchantability(input) <= 0) continue;
                // Priest can roll base level 30 with full bookshelf power and allows treasure enchantments.
                // A private RNG gives genuine, repeatable previews without touching a housing/world RNG.
                ItemStack output = EnchantmentHelper.addRandomEnchantment(new Random(0L), input.copy(), 30, true);
                if (output == null || output.isEmpty() || output.getItem() != item
                        || output.getMetadata() != input.getMetadata() || output.getCount() != 1
                        || !output.isItemEnchanted()) continue;
                cleanExamples.add(input);
                enchantedExamples.add(output);
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot display Priest Bee example for {}", item.getRegistryName(), failure);
            }
        }
        inputs = Collections.unmodifiableList(cleanExamples);
        outputs = Collections.unmodifiableList(enchantedExamples);
    }

    @Override
    public ItemStack getInput() { return inputs.isEmpty() ? ItemStack.EMPTY : inputs.get(0).copy(); }

    @Override
    public boolean acceptsInput(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (ItemStack example : inputs) {
            if (stack.getItem() == example.getItem()) return EffectPriest.INSTANCE.acceptItemStack(stack);
        }
        return false;
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
            if (slotIndex == 1) tooltip.add(I18n.format("jei.capnsbeeaddon.priest.random_enchantments"));
            if (slotIndex == 0 || slotIndex == 1) {
                tooltip.add(TextFormatting.RED + I18n.format("jei.capnsbeeaddon.priest.any_equipment"));
            }
        });
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        advanceCycle(System.nanoTime(), GuiScreen.isShiftKeyDown());
        int y = 30;
        for (String line : minecraft.fontRenderer.listFormattedStringToWidth(
                I18n.format("jei.capnsbeeaddon.priest.bookshelves"), 80)) {
            minecraft.fontRenderer.drawString(line, (recipeWidth - minecraft.fontRenderer.getStringWidth(line)) / 2,
                    y, 0x808080);
            y += minecraft.fontRenderer.FONT_HEIGHT;
        }
    }

    private void advanceCycle(long now, boolean paused) {
        if (!paused) phaseNanos = (phaseNanos + now - lastDrawNanos) % (inputs.size() * CYCLE_NANOS);
        lastDrawNanos = now;
        pairIndex = (int) (phaseNanos / CYCLE_NANOS);
    }

    /** Per-layout slot bindings keep main pages and preview layouts independent. */
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
            // Bind both real slots together; independent native cycles can drift or filter differently.
            stacks.set(0, displayedInputs.get(pairIndex).copy());
            stacks.set(1, displayedOutputs.get(pairIndex).copy());
            displayedPair = pairIndex;
        }

        private IIngredientRenderer<ItemStack> renderer(int slot) {
            return new IIngredientRenderer<ItemStack>() {
                @Override
                public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
                    update();
                    ItemStack displayed = stacks.getGuiIngredients().get(slot).getDisplayedIngredient();
                    itemRenderer.render(minecraft, xPosition, yPosition, displayed);
                }

                @Override
                public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag tooltipFlag) {
                    // Keep the usual equipment tooltip, without promising the preview's particular enchantments.
                    ItemStack tooltipStack = slot == 1 ? displayedInputs.get(displayedPair).copy() : ingredient;
                    return itemRenderer.getTooltip(minecraft, tooltipStack, tooltipFlag);
                }

                @Override
                public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
                    return itemRenderer.getFontRenderer(minecraft, ingredient);
                }
            };
        }
    }
}
