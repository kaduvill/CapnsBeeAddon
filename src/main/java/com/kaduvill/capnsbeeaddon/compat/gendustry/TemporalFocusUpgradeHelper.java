package com.kaduvill.capnsbeeaddon.compat.gendustry;

import com.kaduvill.capnsbeeaddon.registry.ModItems;
import forestry.api.apiculture.IBeeHousing;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class TemporalFocusUpgradeHelper {

    public enum TemporalFocusMode {
        NONE,
        APIARY,
        TILE_ENTITY,
        GROWTH
    }

    private TemporalFocusUpgradeHelper() {
    }

    public static TemporalFocusMode getFocusMode(IBeeHousing housing) {
        if (!(housing instanceof IIndustrialApiary)) {
            return TemporalFocusMode.NONE;
        }

        List<ItemStack> upgrades =
                ((IIndustrialApiary) housing).getUpgrades();

        if (upgrades == null || upgrades.isEmpty()) {
            return TemporalFocusMode.NONE;
        }

        boolean tileEntityFound = false;
        boolean growthFound = false;

        for (ItemStack stack : upgrades) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == ModItems.TEMPORAL_FOCUS_APIARY) {
                return TemporalFocusMode.APIARY;
            }

            if (stack.getItem() == ModItems.TEMPORAL_FOCUS_TILEENTITY) {
                tileEntityFound = true;
            } else if (stack.getItem() == ModItems.TEMPORAL_FOCUS_GROWTH) {
                growthFound = true;
            }
        }

        if (tileEntityFound) {
            return TemporalFocusMode.TILE_ENTITY;
        }

        return growthFound
                ? TemporalFocusMode.GROWTH
                : TemporalFocusMode.NONE;
    }

    public static boolean isTileFocus(TemporalFocusMode mode) {
        return mode == TemporalFocusMode.APIARY
                || mode == TemporalFocusMode.TILE_ENTITY;
    }

    public static boolean isEligibleTileTarget(
            TemporalFocusMode mode,
            TileEntity tile,
            BlockPos targetPos,
            BlockPos source
    ) {
        if (tile == null
                || tile.isInvalid()
                || targetPos.equals(source)) {
            return false;
        }

        if (mode == TemporalFocusMode.APIARY) {
            if (!(tile instanceof IIndustrialApiary)) {
                return false;
            }
        } else if (mode != TemporalFocusMode.TILE_ENTITY) {
            return false;
        }

        return tile instanceof ITickable
                && targetPos.equals(tile.getPos());
    }

    public static boolean isRandomTickTarget(IBlockState state) {
        return state.getBlock().getTickRandomly();
    }


}