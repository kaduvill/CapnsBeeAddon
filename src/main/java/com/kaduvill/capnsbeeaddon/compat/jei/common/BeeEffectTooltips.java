package com.kaduvill.capnsbeeaddon.compat.jei.common;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Formats arrow details only on hover; translates afresh so language changes remain visible. */
public final class BeeEffectTooltips {
    private BeeEffectTooltips() {}

    public static List<String> withModifiers(BeeEffectScaling scaling, String... details) {
        List<String> tooltip = new ArrayList<>(details.length + (scaling.explanationKey == null ? 4 : 5));
        Collections.addAll(tooltip, details);
        tooltip.add("");
        tooltip.add(TextFormatting.WHITE + I18n.format("jei.capnsbeeaddon.effect_modifiers"));
        tooltip.add(TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.effect_modifiers.speed_gene",
                answer(scaling.speedAffected)));
        tooltip.add(TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.effect_modifiers.production",
                answer(scaling.productionAffected)));
        if (scaling.explanationKey != null) {
            tooltip.add(TextFormatting.GRAY + I18n.format(scaling.explanationKey));
        }
        return tooltip;
    }

    private static String answer(boolean affected) {
        return (affected ? TextFormatting.GREEN : TextFormatting.RED)
                + I18n.format(affected ? "jei.capnsbeeaddon.effect_modifiers.yes"
                        : "jei.capnsbeeaddon.effect_modifiers.no")
                + TextFormatting.GRAY;
    }
}
