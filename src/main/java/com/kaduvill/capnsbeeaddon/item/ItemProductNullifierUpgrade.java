package com.kaduvill.capnsbeeaddon.item;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import net.bdew.gendustry.api.ApiaryModifiers;
import net.bdew.gendustry.api.items.IApiaryUpgrade;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public final class ItemProductNullifierUpgrade
        extends Item
        implements IApiaryUpgrade {

    private static final long STACKING_ID = 0x4341504E42454503L;
    public static final int MAX_INSTALLED = 1;
    private static final String LABEL_MAX_INSTALLED = "gendustry.label.maxinstall";
    private static final String DETAIL_1 = "item.capnsbeeaddon.product_nullifier.detail.1";
    private static final String DETAIL_2 = "item.capnsbeeaddon.product_nullifier.detail.2";

    public ItemProductNullifierUpgrade() {
        setRegistryName(
                CapnsBeeAddon.MODID,
                "product_nullifier"
        );
        setUnlocalizedName(
                CapnsBeeAddon.MODID
                        + ".product_nullifier"
        );

        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(MAX_INSTALLED);
        setNoRepair();
    }

    @Override
    public String getDisplayName(ItemStack stack) {
        return stack.getDisplayName();
    }

    @Override
    public List<String> getDisplayDetails(ItemStack stack) {
        return Arrays.asList(
                I18n.translateToLocal(
                        LABEL_MAX_INSTALLED
                ) + " " + getMaxNumber(stack),

                I18n.translateToLocal(DETAIL_1),
                I18n.translateToLocal(DETAIL_2)
        );
    }

    @Override
    public long getStackingId(ItemStack stack) {return STACKING_ID;}
    @Override
    public int getMaxNumber(ItemStack stack) {return MAX_INSTALLED;}

    @Override
    public void applyModifiers(
            ApiaryModifiers modifiers,
            ItemStack stack
    ) {// Product suppression is behavior, not a numeric housing modifier.
    }

    @Override
    public void addInformation(
            ItemStack stack,
            @Nullable World world,
            List<String> tooltip,
            ITooltipFlag flag
    ) {
        tooltip.addAll(getDisplayDetails(stack));
    }
}