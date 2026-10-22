package com.kaduvill.capnsbeeaddon.compat.jei.ElectricianBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One damage overview and three entity transformations; no entities or worlds are created. */
public final class ElectricianBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".electrician_bee";
    public static final int BASE_TICKS = 5;
    private static final long CYCLE_NANOS = 1_000_000_000L;
    private static final String[] TARGET_KEYS = {
            "jei.capnsbeeaddon.medical.players", "jei.capnsbeeaddon.medical.hostile_mobs",
            "jei.capnsbeeaddon.medical.animals"
    };

    public enum Page {
        DAMAGE(null, null, null, null, "damage"),
        PIG("pig", "zombie_pigman", "entity.Pig.name", "entity.PigZombie.name", "pig"),
        VILLAGER("villager", "witch", "entity.Villager.name", "entity.Witch.name", "villager"),
        CREEPER("creeper", "creeper", "entity.Creeper.name", "jei.capnsbeeaddon.electrician.charged", "creeper");

        @Nullable private final ResourceLocation inputId;
        @Nullable private final ResourceLocation outputId;
        @Nullable private final String inputName;
        @Nullable private final String outputName;
        private final String captionKey;

        Page(@Nullable String input, @Nullable String output, @Nullable String inputName,
             @Nullable String outputName, String caption) {
            inputId = input == null ? null : new ResourceLocation("minecraft", input);
            outputId = output == null ? null : new ResourceLocation("minecraft", output);
            this.inputName = inputName;
            this.outputName = outputName;
            captionKey = "jei.capnsbeeaddon.electrician." + caption;
        }
    }

    private final Page page;
    private final ItemStack input;
    private final ItemStack output;
    private final Face[] targets;
    @Nullable private final Face result;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;
    private int targetIndex;

    public ElectricianBeeRecipe(IGuiHelper helper, Page page) {
        this.page = page;
        input = egg(page.inputId);
        output = egg(page.outputId);
        if (page == Page.CREEPER) {
            // HEI indexes eggs by entity ID, so the ordinary Creeper egg still finds this result.
            output.getTagCompound().getCompoundTag("EntityTag").setBoolean("powered", true);
        }
        switch (page) {
            case DAMAGE:
                targets = new Face[] {
                        face(helper, "steve", 64, 8, 8, 8, 8),
                        face(helper, "zombie/zombie", 64, 8, 8, 8, 8),
                        face(helper, "cow/cow", 32, 6, 6, 8, 8)
                };
                result = null;
                break;
            case PIG:
                targets = new Face[] {new Face(texture(helper, "pig/pig", 32, 8, 8, 8, 8),
                        texture(helper, "pig/pig", 32, 17, 17, 4, 3), 2, 4, 8, 8)};
                result = face(helper, "zombie_pigman", 64, 8, 8, 8, 8);
                break;
            case VILLAGER:
                targets = new Face[] {villagerFace(helper, "villager/villager", 64)};
                result = villagerFace(helper, "witch", 128);
                break;
            case CREEPER:
                targets = new Face[] {face(helper, "creeper/creeper", 32, 8, 8, 8, 8)};
                result = targets[0];
                break;
            default:
                throw new IllegalArgumentException("Unknown Electrician page: " + page);
        }
    }

    private static ItemStack egg(@Nullable ResourceLocation id) {
        if (id == null) return ItemStack.EMPTY;
        ItemStack egg = new ItemStack(Items.SPAWN_EGG);
        ItemMonsterPlacer.applyEntityIdToItemStack(egg, id);
        return egg;
    }

    @Nullable
    private static ResourceLocation eggId(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.SPAWN_EGG
                ? ItemMonsterPlacer.getNamedIdFrom(stack) : null;
    }

    @Override
    public ItemStack getInput() { return input.copy(); }

    @Override
    public boolean acceptsInput(ItemStack stack) {
        if (page == Page.DAMAGE) return false;
        ResourceLocation id = eggId(stack);
        return id != null && id.equals(page.inputId);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        if (page != Page.DAMAGE) {
            // The eggs supply JEI navigation; custom slot renderers show the living mobs.
            ingredients.setInput(VanillaTypes.ITEM, input.copy());
            ingredients.setOutput(VanillaTypes.ITEM, output.copy());
        }
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        if (page != Page.DAMAGE) {
            stacks.init(0, true, renderer(false), 18, 8, 18, 18, 1, 1);
            stacks.init(1, false, renderer(true), 124, 8, 18, 18, 1, 1);
            // Keep the charged result's NBT when JEI focuses an ordinary Creeper egg.
            stacks.setOverrideDisplayFocus(null);
        }
        stacks.set(ingredients);
    }

    private IIngredientRenderer<ItemStack> renderer(boolean outputSlot) {
        return new IIngredientRenderer<ItemStack>() {
            @Override
            public void render(Minecraft minecraft, int x, int y, @Nullable ItemStack ingredient) {
                if (ingredient == null || ingredient.isEmpty()) return;
                Face face = outputSlot ? result : targets[0];
                if (outputSlot && page == Page.CREEPER) {
                    Gui.drawRect(x - 1, y - 1, x + 17, y + 17, 0xFF78D9FF);
                }
                if (face != null) face.draw(minecraft, x, y, false);
            }

            @Override
            public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag flag) {
                String name = I18n.format(outputSlot ? page.outputName : page.inputName);
                if (outputSlot && page == Page.CREEPER) {
                    return Arrays.asList(name, I18n.format("jei.capnsbeeaddon.electrician.amount"));
                }
                return Collections.singletonList(name);
            }

            @Override
            public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
                return minecraft.fontRenderer;
            }
        };
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (page == Page.DAMAGE) {
            advanceCycle(System.nanoTime(), GuiScreen.isShiftKeyDown());
            targets[targetIndex].draw(minecraft, 19, 9, false);
            targets[targetIndex].draw(minecraft, 125, 9, true);
        }
        int y = 32;
        for (String line : minecraft.fontRenderer.listFormattedStringToWidth(I18n.format(page.captionKey), recipeWidth)) {
            minecraft.fontRenderer.drawString(line, (recipeWidth - minecraft.fontRenderer.getStringWidth(line)) / 2,
                    y, 0x808080);
            y += minecraft.fontRenderer.FONT_HEIGHT;
        }
    }

    private void advanceCycle(long now, boolean paused) {
        if (!paused) phaseNanos = (phaseNanos + now - lastDrawNanos) % (targets.length * CYCLE_NANOS);
        lastDrawNanos = now;
        targetIndex = (int) (phaseNanos / CYCLE_NANOS);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (page != Page.DAMAGE || mouseY < 8 || mouseY >= 26) return Collections.emptyList();
        if (mouseX >= 18 && mouseX < 36) {
            return Arrays.asList(I18n.format("jei.capnsbeeaddon.medical.target"),
                    TextFormatting.RED + I18n.format(TARGET_KEYS[targetIndex]));
        }
        if (mouseX >= 124 && mouseX < 142) {
            return Collections.singletonList(I18n.format("jei.capnsbeeaddon.electrician.amount"));
        }
        return Collections.emptyList();
    }

    private static Face face(IGuiHelper helper, String texture, int height, int u, int v, int width, int cropHeight) {
        return new Face(texture(helper, texture, height, u, v, width, cropHeight), null, 0, 0, width, cropHeight);
    }

    private static Face villagerFace(IGuiHelper helper, String texture, int height) {
        return new Face(texture(helper, texture, height, 8, 8, 8, 10),
                texture(helper, texture, height, 26, 2, 2, 4), 3, 7, 8, 11);
    }

    private static IDrawable texture(IGuiHelper helper, String texture, int height, int u, int v, int width, int cropHeight) {
        return helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/" + texture + ".png"),
                u, v, width, cropHeight).setTextureSize(64, height).build();
    }

    private static final class Face {
        private final IDrawable base;
        @Nullable private final IDrawable detail;
        private final int detailX;
        private final int detailY;
        private final int width;
        private final int height;

        private Face(IDrawable base, @Nullable IDrawable detail, int detailX, int detailY, int width, int height) {
            this.base = base;
            this.detail = detail;
            this.detailX = detailX;
            this.detailY = detailY;
            this.width = width;
            this.height = height;
        }

        private void draw(Minecraft minecraft, int x, int y, boolean hurt) {
            float scale = 16F / Math.max(width, height);
            GlStateManager.disableLighting();
            GlStateManager.color(1, hurt ? 0.4F : 1, hurt ? 0.4F : 1, 1);
            GlStateManager.pushMatrix();
            try {
                GlStateManager.translate(x + (16 - width * scale) / 2, y + (16 - height * scale) / 2, 0);
                GlStateManager.scale(scale, scale, 1);
                base.draw(minecraft);
                if (detail != null) detail.draw(minecraft, detailX, detailY);
            } finally {
                GlStateManager.popMatrix();
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }
}
