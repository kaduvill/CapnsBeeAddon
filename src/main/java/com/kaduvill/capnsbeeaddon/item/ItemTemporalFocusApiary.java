package com.kaduvill.capnsbeeaddon.item;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import net.bdew.gendustry.api.ApiaryModifiers;
import net.bdew.gendustry.api.items.IApiaryUpgrade;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public final class ItemTemporalFocusApiary extends Item implements IApiaryUpgrade {

    private static final long STACKING_ID = 0x4341504E42454501L;

    private static final String DETAIL_1 =
            "item.capnsbeeaddon.temporal_focus_apiary.detail.1";
    private static final String DETAIL_2 =
            "item.capnsbeeaddon.temporal_focus_apiary.detail.2";

    public ItemTemporalFocusApiary() {
        setRegistryName(CapnsBeeAddon.MODID, "temporal_focus_apiary");
        setUnlocalizedName(CapnsBeeAddon.MODID + ".temporal_focus_apiary");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(1);
        setNoRepair();
    }

    @Override
    public String getDisplayName(ItemStack stack) {
        return stack.getDisplayName();
    }

    @Override
    public List<String> getDisplayDetails(ItemStack stack) {
        return Arrays.asList(
                I18n.translateToLocal(DETAIL_1),
                I18n.translateToLocal(DETAIL_2)
        );
    }

    @Override
    public long getStackingId(ItemStack stack) {
        return STACKING_ID;
    }

    @Override
    public int getMaxNumber(ItemStack stack) {
        return 1;
    }

    @Override
    public void applyModifiers(ApiaryModifiers mods, ItemStack stack) {
        // Target filtering is behavior, not a numeric housing modifier.
    }

    @Override
    public void addInformation(
            ItemStack stack,
            @Nullable World world,
            List<String> tooltip,
            ITooltipFlag flag
    ) {
        for (String line : getDisplayDetails(stack)) {
            tooltip.add(TextFormatting.GRAY + line);
        }
    }
}