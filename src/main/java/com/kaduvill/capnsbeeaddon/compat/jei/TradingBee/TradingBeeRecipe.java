package com.kaduvill.capnsbeeaddon.compat.jei.TradingBee;

import com.kaduvill.capnsbeeaddon.compat.jei.TradingBee.TradingBeeRecipes.Entry;
import com.kaduvill.capnsbeeaddon.compat.jei.TradingBee.TradingBeeRecipes.Mode;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One overview per bee; all grid entries remain indexed while only the current page renders. */
public final class TradingBeeRecipe implements BeeEffectRecipe {
    private static final int HIDDEN_POSITION = -10000;
    private static final int BUTTON_WIDTH = 18;
    private static final IIngredientRenderer<ItemStack> HIDDEN_RENDERER = (minecraft, x, y, stack) -> { };
    private final Mode mode;
    private final List<Entry> entries;
    private final TradingBeeRecipeLayout layout;
    private IGuiItemStackGroup items;
    private int page;

    public TradingBeeRecipe(Mode mode, List<Entry> entries) {
        this.mode = mode;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        layout = new TradingBeeRecipeLayout(entries.size());
    }

    @Override public ItemStack getInput() { return mode.isBusiness() ? ItemStack.EMPTY : new ItemStack(Items.EMERALD); }

    @Override
    public boolean acceptsInput(ItemStack stack) {
        return mode.isBusiness() ? findEntry(stack) >= 0 : isEmerald(stack);
    }

    public boolean acceptsOutput(ItemStack stack) {
        return mode.isBusiness() ? !stack.isEmpty() && stack.getItem() == Items.EMERALD : findEntry(stack) >= 0;
    }

    private static boolean isEmerald(ItemStack stack) {
        return !stack.isEmpty() && ItemHandlerHelper.canItemStacksStack(new ItemStack(Items.EMERALD), stack);
    }

    private int findEntry(ItemStack stack) {
        for (int i = 0; i < entries.size(); i++) if (entries.get(i).matches(stack, mode)) return i;
        return -1;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> goods = new ArrayList<>();
        for (Entry entry : entries) goods.add(entry.getStack(mode));
        if (mode.isBusiness()) {
            ingredients.setInputs(VanillaTypes.ITEM, goods);
            ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(Items.EMERALD));
        } else {
            ingredients.setInput(VanillaTypes.ITEM, new ItemStack(Items.EMERALD));
            ingredients.setOutputs(VanillaTypes.ITEM, goods);
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients) {
        items = recipeLayout.getItemStacks();
        items.setOverrideDisplayFocus(null);
        IFocus<?> focus = recipeLayout.getFocus();
        if (focus != null && focus.getValue() instanceof ItemStack
                && focus.getMode() == (mode.isBusiness() ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT)) {
            int focused = findEntry((ItemStack) focus.getValue());
            if (focused >= 0) page = focused / layout.pageSize;
        }
        items.init(0, !mode.isBusiness(), TradingBeeRecipeLayout.EMERALD_X, 0);
        items.set(0, new ItemStack(Items.EMERALD));
        int first = page * layout.pageSize;
        for (int i = 0; i < entries.size(); i++) initSlot(i, i >= first && i < first + layout.pageSize ? i - first : -1);
        items.addTooltipCallback((slot, input, ingredient, tooltip) -> {
            if (slot > 0 && slot <= entries.size()) addTradeTooltip(entries.get(slot - 1), tooltip);
        });
    }

    private void addTradeTooltip(Entry entry, List<String> tooltip) {
        if (entry.amountsMayVary()) tooltip.add(I18n.format("jei.capnsbeeaddon.trading.amounts_vary"));
        boolean mayHaveEnchantments = !mode.isBusiness() && entry.getOffers().stream()
                .anyMatch(offer -> offer.enchanted || offer.output.isItemEnchanted());
        if (mayHaveEnchantments)
            tooltip.add(TextFormatting.RED + I18n.format("jei.capnsbeeaddon.trading.random_enchants"));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (layout.footerY < 0) return;
        FontRenderer font = minecraft.fontRenderer;
        int y = layout.footerY + (TradingBeeRecipeLayout.FOOTER_HEIGHT - font.FONT_HEIGHT) / 2;
        String label = I18n.format("jei.capnsbeeaddon.trading.page", page + 1, layout.pages(entries.size()));
        font.drawString(label, (recipeWidth - font.getStringWidth(label)) / 2, y, 0x606060);
        font.drawString("<", (BUTTON_WIDTH - font.getStringWidth("<")) / 2, y, page > 0 ? 0x404040 : 0xA0A0A0);
        font.drawString(">", TradingBeeRecipeLayout.WIDTH - (BUTTON_WIDTH + font.getStringWidth(">")) / 2,
                y, page + 1 < layout.pages(entries.size()) ? 0x404040 : 0xA0A0A0);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 0 && mouseX < TradingBeeRecipeLayout.WIDTH
                && mouseY >= TradingBeeRecipeLayout.LABEL_Y && mouseY < TradingBeeRecipeLayout.GRID_Y)
            return Arrays.asList(I18n.format(mode.translation + ".pool_hint"),
                    TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.trading.adjacent"));
        return Collections.emptyList();
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 || items == null || layout.footerY < 0
                || mouseY < layout.footerY || mouseY >= layout.footerY + TradingBeeRecipeLayout.FOOTER_HEIGHT) return false;
        int target;
        if (mouseX >= 0 && mouseX < BUTTON_WIDTH) target = page - 1;
        else if (mouseX >= TradingBeeRecipeLayout.WIDTH - BUTTON_WIDTH && mouseX < TradingBeeRecipeLayout.WIDTH) target = page + 1;
        else return false;
        if (target < 0 || target >= layout.pages(entries.size())) return true;
        int oldPage = page;
        page = target;
        positionPage(oldPage, false);
        positionPage(page, true);
        return true;
    }

    private void positionPage(int target, boolean visible) {
        int first = target * layout.pageSize;
        for (int i = first; i < Math.min(first + layout.pageSize, entries.size()); i++)
            initSlot(i, visible ? i - first : -1);
    }

    private void initSlot(int entry, int visible) {
        int slot = entry + 1;
        if (visible < 0) items.init(slot, mode.isBusiness(), HIDDEN_RENDERER,
                HIDDEN_POSITION, HIDDEN_POSITION, 18, 18, 1, 1);
        else items.init(slot, mode.isBusiness(), layout.gridX(visible), layout.gridY(visible));
        items.set(slot, entries.get(entry).getStack(mode));
    }
}
