package com.kaduvill.capnsbeeaddon.compat.careerbees;

import gnu.trove.set.hash.THashSet;
import net.minecraft.util.math.BlockPos;

/**
 * Prevents the same focused source from scanning twice during one world tick.
 *
 * Cleared whenever the world time changes, keeping retained state bounded by
 * the number of focused Temporal sources active during one scan tick.
 */
public final class FocusedScanState {

    private long worldTime = Long.MIN_VALUE;
    private final THashSet<BlockPos> scannedSources = new THashSet<>();

    /**
     * @return true when this source has not already scanned this world tick
     */
    public boolean markScanned(long currentWorldTime, BlockPos source) {
        if (worldTime != currentWorldTime) {
            worldTime = currentWorldTime;
            scannedSources.clear();
        }

        return scannedSources.add(source);
    }
}