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

public final class ItemTerritoryRestrictor
        extends Item
        implements IApiaryUpgrade {

    public static final float TERRITORY_MULTIPLIER_PER_UPGRADE = 0.70F;
    public static final int MAX_INSTALLED = 6;
    public static final float ENERGY_MULTIPLIER_PER_UPGRADE = 1.05F; //+5% energy consumption per installed upgrade.
    private static final long STACKING_ID = 0x4341504E42454502L;
    private static final String LABEL_MAX_INSTALLED = "gendustry.label.maxinstall";
    private static final String LABEL_TERRITORY = "gendustry.label.mod.territory";
    private static final String LABEL_ENERGY = "gendustry.label.mod.energy";

    private static String formatModifier(float value, float base) {
        int percent = Math.round(
                (value - base) * 100.0F
        );

        return (percent > 0 ? "+" : "")
                + percent
                + "%";
    }

    public ItemTerritoryRestrictor() {
        setRegistryName(
                CapnsBeeAddon.MODID,
                "territory_restrictor"
        );

        setUnlocalizedName(
                CapnsBeeAddon.MODID
                        + ".territory_restrictor"
        );

        /*
         * Allows all installed restrictors to share one upgrade slot.
         * Gendustry still enforces getMaxNumber() across every slot.
         */
        setMaxStackSize(MAX_INSTALLED);

        setCreativeTab(CreativeTabs.MISC);
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

                I18n.translateToLocal(
                        LABEL_TERRITORY
                ) + " " + formatModifier(
                        TERRITORY_MULTIPLIER_PER_UPGRADE,
                        1.0F
                ),

                I18n.translateToLocal(
                        LABEL_ENERGY
                ) + " " + formatModifier(
                        ENERGY_MULTIPLIER_PER_UPGRADE,
                        1.0F
                )
        );
    }

    @Override
    public long getStackingId(ItemStack stack) {
        return STACKING_ID;
    }

    @Override
    public int getMaxNumber(ItemStack stack) {
        return MAX_INSTALLED;
    }

    @Override
    public void applyModifiers(
            ApiaryModifiers modifiers,
            ItemStack stack
    ) {
        /*
         * Gendustry calls this once per occupied upgrade stack,
         * not once per item. Apply the effects stack.getCount()
         * times to match its built-in stackable upgrades.
         *
         * Clamp malformed stacks defensively. Normal apiaries already
         * reject more than MAX_INSTALLED.
         */
        int count = Math.max(
                0,
                Math.min(stack.getCount(), MAX_INSTALLED)
        );

        for (int i = 0; i < count; i++) {
            modifiers.territory *=
                    TERRITORY_MULTIPLIER_PER_UPGRADE;

            modifiers.energy *=
                    ENERGY_MULTIPLIER_PER_UPGRADE;
        }
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