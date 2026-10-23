package com.kaduvill.capnsbeeaddon.compat.jei.AttachedBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IBee;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One cached attachment overview per helper; no world access or simulated bee behavior. */
public final class AttachedBeeRecipe implements BeeEffectRecipe {
    public enum Kind {
        YENTE("yente", CareerBeeSpecies.YENTE, EnumBeeType.PRINCESS, "princess"),
        STUDENT("student", CareerBeeSpecies.STUDENT, EnumBeeType.DRONE, "drone");

        public final String uid;
        public final String titleKey;
        public final String descriptionKey;
        public final String requirementKey;
        private final CareerBeeEntry species;
        private final EnumBeeType beeType;

        Kind(String name, CareerBeeEntry species, EnumBeeType beeType, String requirement) {
            uid = CapnsBeeAddon.MODID + "." + name + "_bee";
            titleKey = "jei.capnsbeeaddon." + name + ".title";
            descriptionKey = "jei.capnsbeeaddon." + name + ".description";
            requirementKey = "jei.capnsbeeaddon.attached." + requirement;
            this.species = species;
            this.beeType = beeType;
        }
    }

    private final Kind kind;
    private final IAlleleBeeSpecies species;
    private final ItemStack bee;

    public AttachedBeeRecipe(Kind kind) {
        this.kind = kind;
        species = kind.species.get();
        bee = BeeManager.beeRoot.getMemberStack(species, kind.beeType);
        if (bee.isEmpty()) throw new IllegalArgumentException("Missing " + kind + " helper bee");
    }

    public Kind getKind() { return kind; }

    @Override
    public ItemStack getInput() { return bee.copy(); }

    @Override
    public boolean acceptsInput(ItemStack stack) {
        if (stack.isEmpty() || BeeManager.beeRoot.getType(stack) != kind.beeType) return false;
        // Navigation accepts differing genetic NBT instead of requiring the exact display template.
        IBee member = BeeManager.beeRoot.getMember(stack);
        return member != null && member.getGenome().getPrimary() == species;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // The bee and apiary illustrate an interaction; no crafted item outputs are indexed.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        layout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int y = 32;
        for (String line : minecraft.fontRenderer.listFormattedStringToWidth(
                I18n.format(kind.descriptionKey), recipeWidth)) {
            minecraft.fontRenderer.drawString(line,
                    (recipeWidth - minecraft.fontRenderer.getStringWidth(line)) / 2, y, 0x808080);
            y += minecraft.fontRenderer.FONT_HEIGHT;
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (kind == Kind.YENTE || mouseX < 0 || mouseX >= 160 || mouseY < 30 || mouseY >= 50) {
            return Collections.emptyList();
        }
        return Arrays.asList(I18n.format("jei.capnsbeeaddon.student.flowers"),
                I18n.format("jei.capnsbeeaddon.student.queen"),
                I18n.format("jei.capnsbeeaddon.student.drone"),
                I18n.format("jei.capnsbeeaddon.student.space"));
    }
}
