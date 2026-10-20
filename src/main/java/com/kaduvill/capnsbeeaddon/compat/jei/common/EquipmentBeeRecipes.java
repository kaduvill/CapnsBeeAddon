package com.kaduvill.capnsbeeaddon.compat.jei.common;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.rwtema.careerbees.effects.EffectArmorer;
import com.rwtema.careerbees.effects.EffectAttributeBoost;
import com.rwtema.careerbees.effects.EffectSharpen;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Cached clean equipment -> native maximum bonus; no progress simulation during display or lookup. */
public final class EquipmentBeeRecipes {
    private static final String MODIFIERS = "AttributeModifiersCustom";
    // Both installed native curves finish below this bound (88 and 51 successful changes).
    private static final int MAX_OPERATIONS = 128;

    public static final EquipmentBeeRecipes ARMORER = new EquipmentBeeRecipes("armorer", 16, true,
            EffectArmorer.INSTANCE, UUID.fromString("65C761AF-1772-437F-903B-1CCA6371D572"), 1, 1.0);
    public static final EquipmentBeeRecipes SHARPENING = new EquipmentBeeRecipes("sharpening", 20, false,
            EffectSharpen.INSTANCE, UUID.fromString("78EE8D4A-80DC-4639-9A4E-266B7591794D"), 0, 5.0);

    private final String name;
    private final int baseTicks;
    private final boolean armor;
    private final EffectAttributeBoost effect;
    private final double maximum;
    private final Map<EntityEquipmentSlot, NBTTagCompound> finalModifiers;
    private final List<UUID> ownedIds;

    private EquipmentBeeRecipes(String name, int baseTicks, boolean armor, EffectAttributeBoost effect,
                                UUID baseId, int operation, double maximum) {
        this.name = name;
        this.baseTicks = baseTicks;
        this.armor = armor;
        this.effect = effect;
        this.maximum = maximum;
        Map<EntityEquipmentSlot, NBTTagCompound> modifiers = new EnumMap<>(EntityEquipmentSlot.class);
        List<UUID> ids = new ArrayList<>();
        // EffectAttributeBoost's slot UUID derivation and NBT format in the installed Career Bees version.
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            UUID id = slot.ordinal() == 0 ? baseId
                    : new UUID(baseId.getLeastSignificantBits(), baseId.getMostSignificantBits() + slot.ordinal());
            ids.add(id);
            NBTTagCompound modifier = SharedMonsterAttributes.writeAttributeModifierToNBT(
                    new AttributeModifier(id, "Bee Modification", maximum, operation));
            modifier.setString("AttributeName", (armor ? SharedMonsterAttributes.ARMOR
                    : SharedMonsterAttributes.ATTACK_DAMAGE).getName());
            modifier.setString("Slot", slot.getName());
            modifiers.put(slot, modifier);
        }
        // The base class also owns a UUID for a null slot, though these two effects use concrete slots.
        ids.add(new UUID(baseId.getLeastSignificantBits(), baseId.getMostSignificantBits() - 1));
        this.finalModifiers = Collections.unmodifiableMap(modifiers);
        this.ownedIds = Collections.unmodifiableList(ids);
    }

    public String getUid() { return CapnsBeeAddon.MODID + "." + name + "_bee"; }

    public String getTitleKey() { return "jei.capnsbeeaddon." + name + ".title"; }

    public int getBaseTicks() { return baseTicks; }

    public List<Recipe> read(Iterable<ItemStack> ingredients) {
        Map<NBTTagCompound, Recipe> recipes = new LinkedHashMap<>();
        Set<NBTTagCompound> seen = new HashSet<>();
        for (ItemStack stack : ingredients) {
            try {
                ItemStack input = withoutBonus(stack);
                if (input.isEmpty() || !usableModifierList(input) || slotFor(input) == null) continue;
                NBTTagCompound key = input.serializeNBT();
                // JEI can expose both ordinary and bee-modified prototypes: finish each clean variant once.
                if (!seen.add(key)) continue;
                Completion completion = finish(input);
                if (completion != null) recipes.put(key, new Recipe(input, completion));
            } catch (RuntimeException | LinkageError failure) {
                // A broken custom equipment item must not hide recipes for other items.
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(recipes.values()));
    }

    /** Lookup-only copy, removing this effect's UUIDs while retaining other bee and equipment data. */
    public ItemStack withoutBonus(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack input = one(stack);
        NBTTagCompound tag = input.getTagCompound();
        if (tag != null) {
            if (usableModifierList(input) && tag.hasKey(MODIFIERS, 9)) {
                NBTTagList modifiers = tag.getTagList(MODIFIERS, 10);
                for (int i = modifiers.tagCount() - 1; i >= 0; i--) {
                    if (owns(modifiers.getCompoundTagAt(i))) modifiers.removeTag(i);
                }
                if (modifiers.hasNoTags()) tag.removeTag(MODIFIERS);
            }
            if (tag.hasNoTags()) input.setTagCompound(null);
        }
        return input;
    }

    /** Only exact native maximum products are outputs. This check never simulates the upgrade sequence. */
    public ItemStack sourceOf(ItemStack output) {
        try {
            if (output.isEmpty() || !usableModifierList(output)) return ItemStack.EMPTY;
            NBTTagCompound modifier = singleOwnedModifier(output);
            if (modifier == null) return ItemStack.EMPTY;
            ItemStack source = withoutBonus(output);
            EntityEquipmentSlot slot = slotFor(source);
            return slot != null && finalModifiers.get(slot).equals(modifier) ? source : ItemStack.EMPTY;
        } catch (RuntimeException | LinkageError failure) {
            return ItemStack.EMPTY;
        }
    }

    private Completion finish(ItemStack input) {
        EntityEquipmentSlot slot = slotFor(input);
        if (slot == null) return null;
        ItemStack current = one(input);
        int operations = 0;
        // Native boostStat edits its argument in place. One private copy is sufficient for the whole sequence.
        for (int attempt = 0; attempt <= MAX_OPERATIONS; attempt++) {
            NBTTagCompound previous = singleOwnedModifier(current);
            ItemStack result = effect.modifyStack(null, current, null);
            if (result == null) {
                return operations > 0 && finalModifiers.get(slot).equals(singleOwnedModifier(current))
                        ? new Completion(current, operations) : null;
            }
            if (result.isEmpty() || !ItemStack.areItemsEqual(input, result)) return null;
            NBTTagCompound next = singleOwnedModifier(result);
            if (!validProgress(next, slot) || next.equals(previous)) return null;
            if (previous != null && next.getDouble("Amount") <= previous.getDouble("Amount")) return null;
            current = result;
            operations++;
        }
        // Fail closed if a modified dependency or malformed item does not reach a genuine native terminal state.
        return null;
    }

    private boolean canImprove(ItemStack stack) {
        try {
            if (stack.isEmpty() || !usableModifierList(stack)) return false;
            ItemStack copy = one(stack);
            EntityEquipmentSlot slot = slotFor(copy);
            if (slot == null) return false;
            NBTTagCompound previous = singleOwnedModifier(copy);
            if (previous != null && !validProgress(previous, slot)) return false;
            // Do not call EffectSharpen.acceptItemStack: it mutates and rejects its own argument.
            ItemStack result = effect.modifyStack(null, copy, null);
            if (result == null || result.isEmpty()) return false;
            NBTTagCompound next = singleOwnedModifier(result);
            return validProgress(next, slot) && !next.equals(previous);
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    private EntityEquipmentSlot slotFor(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (armor) {
            EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
            return slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? slot : null;
        }
        return stack.getItem() instanceof ItemSword || stack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND)
                .containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName()) ? EntityEquipmentSlot.MAINHAND : null;
    }

    private boolean validProgress(NBTTagCompound modifier, EntityEquipmentSlot slot) {
        if (modifier == null) return false;
        double amount = modifier.getDouble("Amount");
        if (!Double.isFinite(amount) || amount <= 0 || amount > maximum) return false;
        NBTTagCompound expected = finalModifiers.get(slot).copy();
        expected.setDouble("Amount", amount);
        return expected.equals(modifier);
    }

    private NBTTagCompound singleOwnedModifier(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) return null;
        NBTTagList modifiers = tag.getTagList(MODIFIERS, 10);
        NBTTagCompound found = null;
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound modifier = modifiers.getCompoundTagAt(i);
            if (!owns(modifier)) continue;
            if (found != null) return null;
            found = modifier;
        }
        return found;
    }

    private boolean owns(NBTTagCompound modifier) {
        return modifier.hasUniqueId("UUID") && ownedIds.contains(modifier.getUniqueId("UUID"));
    }

    private static boolean usableModifierList(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(MODIFIERS)) return true;
        if (!tag.hasKey(MODIFIERS, 9)) return false;
        NBTTagList list = (NBTTagList) tag.getTag(MODIFIERS);
        return list.hasNoTags() || list.getTagType() == 10;
    }

    private static ItemStack one(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static final class Completion {
        private final ItemStack output;
        private final int operations;

        private Completion(ItemStack output, int operations) {
            this.output = output;
            this.operations = operations;
        }
    }

    private static final class OperationsLabel implements IDrawable {
        private final int operations;

        private OperationsLabel(int operations) { this.operations = operations; }

        @Override
        public int getWidth() { return 18; }

        @Override
        public int getHeight() { return 31; }

        @Override
        public void draw(Minecraft minecraft, int xOffset, int yOffset) {
            String text = I18n.format("jei.capnsbeeaddon.operations", operations);
            minecraft.fontRenderer.drawString(text, xOffset + 18 - minecraft.fontRenderer.getStringWidth(text),
                    yOffset + 22, 0x808080);
        }
    }

    public final class Recipe implements BeeEffectRecipe {
        private final ItemStack input;
        private final Completion completion;

        private Recipe(ItemStack input, Completion completion) {
            this.input = input.copy();
            this.completion = completion;
        }

        public int getOperations() { return completion.operations; }

        @Override
        public ItemStack getInput() { return input.copy(); }

        @Override
        public boolean acceptsInput(ItemStack stack) {
            return stack.getItem() == input.getItem() && canImprove(stack);
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInput(VanillaTypes.ITEM, input.copy());
            ingredients.setOutput(VanillaTypes.ITEM, completion.output.copy());
        }

        @Override
        public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
            // Display the cached clean -> maximum pair regardless of the clicked item's upgrade progress.
            layout.getItemStacks().setOverrideDisplayFocus(null);
            layout.getItemStacks().set(ingredients);
            layout.getItemStacks().set(0, input.copy());
            layout.getItemStacks().set(1, completion.output.copy());
            layout.getItemStacks().setBackground(1, new OperationsLabel(completion.operations));
        }
    }
}
