package com.kaduvill.capnsbeeaddon.client;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.List;

/** Always-visible guidance for selected Career Bees items. */
@Mod.EventBusSubscriber(modid = CapnsBeeAddon.MODID, value = Side.CLIENT)
public final class TooltipInjector {
    private static final ResourceLocation BEE_GUN = new ResourceLocation("careerbees", "beegun");

    private TooltipInjector() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation itemId = stack.getItem().getRegistryName();
        if (itemId == null || !"careerbees".equals(itemId.getResourceDomain())) return;

        List<String> tooltip = event.getToolTip();
        // Keep the description below the name and ahead of advanced tooltip details.
        int index = Math.min(1, tooltip.size());
        if (BEE_GUN.equals(itemId)) {
            tooltip.add(index++, TextFormatting.RED + I18n.format("tooltip.capnsbeeaddon.bee_gun.description"));
            tooltip.add(index++, TextFormatting.GRAY + I18n.format("tooltip.capnsbeeaddon.bee_gun.select_chamber"));
            tooltip.add(index, TextFormatting.GRAY + I18n.format("tooltip.capnsbeeaddon.bee_gun.open_chambers"));
            return;
        }

        String path = itemId.getResourcePath();
        String descriptionKey = getDescriptionKey(stack, path);
        if (descriptionKey == null) return;

        tooltip.add(index++, TextFormatting.GRAY + I18n.format(descriptionKey));
        if ("mutation_frame".equals(path)) {
            tooltip.add(index, TextFormatting.GRAY + I18n.format("tooltip.capnsbeeaddon.mutation_frame.genetic_decay"));
        } else if ("poison_frame".equals(path)) {
            tooltip.add(index++, TextFormatting.GRAY + I18n.format("tooltip.capnsbeeaddon.poison_frame.genetic_decay"));
            tooltip.add(index, TextFormatting.RED + I18n.format("tooltip.capnsbeeaddon.poison_frame.warning"));
        } else if ("settings_frame".equals(path)) {
            tooltip.add(index, TextFormatting.RED + I18n.format("tooltip.capnsbeeaddon.settings_frame.apiary_only"));
        }
    }

    private static String getDescriptionKey(ItemStack stack, String path) {
        switch (path) {
            case "alveary_frame":
                return "tooltip.capnsbeeaddon.alveary_frame.description";
            case "mutation_frame":
                return "tooltip.capnsbeeaddon.mutation_frame.description";
            case "poison_frame":
                return "tooltip.capnsbeeaddon.poison_frame.description";
            case "pheremone_frame":
                return "tooltip.capnsbeeaddon.pheremone_frame.description";
            case "settings_frame":
                return "tooltip.capnsbeeaddon.settings_frame.description";
            case "flower_pedastal":
                return "tooltip.capnsbeeaddon.flower_pedestal.description";
            case "bee.armor.head":
            case "bee.armor.chest":
            case "bee.armor.legs":
            case "bee.armor.feet":
                return "tooltip.capnsbeeaddon.bee_armor.description";
            case "ingredients":
                // Triquetra shares this item ID with bark, pheromones and other ingredients.
                return stack.getMetadata() == 13 ? "tooltip.capnsbeeaddon.triquetra.description" : null;
            default:
                return null;
        }
    }
}
