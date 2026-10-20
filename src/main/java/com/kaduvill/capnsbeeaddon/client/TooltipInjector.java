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

/** Always-visible guidance for the Bee-Bee Gun. */
@Mod.EventBusSubscriber(modid = CapnsBeeAddon.MODID, value = Side.CLIENT)
public final class TooltipInjector {
    private static final ResourceLocation BEE_GUN = new ResourceLocation("careerbees", "beegun");

    private TooltipInjector() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !BEE_GUN.equals(stack.getItem().getRegistryName())) return;

        List<String> tooltip = event.getToolTip();
        // Keep the description below the name and ahead of advanced tooltip details.
        int index = Math.min(1, tooltip.size());
        tooltip.add(index++, TextFormatting.RED + I18n.format("tooltip.capnsbeeaddon.bee_gun.description"));
        tooltip.add(index++, TextFormatting.GRAY + I18n.format("tooltip.capnsbeeaddon.bee_gun.select_chamber"));
        tooltip.add(index, TextFormatting.GRAY + I18n.format("tooltip.capnsbeeaddon.bee_gun.open_chambers"));
    }
}
