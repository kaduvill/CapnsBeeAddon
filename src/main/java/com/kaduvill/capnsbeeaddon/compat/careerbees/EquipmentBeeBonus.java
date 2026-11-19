package com.kaduvill.capnsbeeaddon.compat.careerbees;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

/** Native Career Bees modifier identities and read-only checks for custom caps. */
public enum EquipmentBeeBonus {
    ARMORER("65C761AF-1772-437F-903B-1CCA6371D572"),
    SHARPENING("78EE8D4A-80DC-4639-9A4E-266B7591794D");

    private static final String MODIFIERS = "AttributeModifiersCustom";

    private final Map<EntityEquipmentSlot, UUID> modifierIds = new HashMap<>();

    EquipmentBeeBonus(String nativeId) {
        UUID baseId = UUID.fromString(nativeId);
        // Career Bees 0.4.0: EffectAttributeBoost's slot UUID derivation.
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            modifierIds.put(slot, slot.ordinal() == 0 ? baseId
                    : new UUID(baseId.getLeastSignificantBits(),
                            baseId.getMostSignificantBits() + slot.ordinal()));
        }
        modifierIds.put(null, new UUID(baseId.getLeastSignificantBits(),
                baseId.getMostSignificantBits() - 1));
    }

    public UUID getModifierId(EntityEquipmentSlot slot) {
        return modifierIds.get(slot);
    }

    /** Never create, normalize or replace item data while testing a limit. */
    public boolean canImprove(ItemStack stack, EntityEquipmentSlot slot, double maximum) {
        if (maximum <= 0.0 || stack.isEmpty()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(MODIFIERS)) return true;
        if (!tag.hasKey(MODIFIERS, 9)) return false;

        NBTTagList modifiers = (NBTTagList) tag.getTag(MODIFIERS);
        if (!modifiers.hasNoTags() && modifiers.getTagType() != 10) return false;

        UUID id = getModifierId(slot);
        long most = id.getMostSignificantBits();
        long least = id.getLeastSignificantBits();
        boolean found = false;
        double amount = 0.0;
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound modifier = modifiers.getCompoundTagAt(i);
            if (!modifier.hasUniqueId("UUID")
                    || modifier.getLong("UUIDMost") != most
                    || modifier.getLong("UUIDLeast") != least) continue;
            if (found || !modifier.hasKey("Amount", 99)) return false;
            amount = modifier.getDouble("Amount");
            if (!Double.isFinite(amount) || amount < 0.0) return false;
            found = true;
        }
        return !found || amount < maximum;
    }

    public static DoubleUnaryOperator boundedCurve(DoubleUnaryOperator nativeCurve, double maximum) {
        return amount -> Math.max(amount, Math.min(maximum, nativeCurve.applyAsDouble(amount)));
    }
}
