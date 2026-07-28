package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.kaduvill.capnsbeeaddon.compat.gendustry.TemporalFocusUpgradeHelper;
import com.kaduvill.capnsbeeaddon.compat.careerbees.FocusedScanState;
import com.rwtema.careerbees.effects.EffectAcceleration;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.genetics.IEffectData;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.WeakHashMap;

@Mixin(value = EffectAcceleration.class, remap = false)
public abstract class EffectAccelerationMixin {

    @Shadow
    @Final
    private WeakHashMap<World, TObjectIntHashMap<BlockPos>> posToTick;

    @Shadow
    private boolean processing;

    @Unique
    private final WeakHashMap<World, FocusedScanState>
            capnsbeeaddon$focusedScanStates = new WeakHashMap<>();

    @Inject(
            method = "doEffectBase",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void capnsbeeaddon$scanOnlyIndustrialApiaries(
            IBeeGenome genome,
            IEffectData storedData,
            IBeeHousing housing,
            IEffectSettingsHolder settings,
            CallbackInfoReturnable<IEffectData> cir
    ) {
        /*
         * Mirror Career Bees' original cheap exits before inspecting Gendustry's
         * upgrade inventory. This avoids getUpgrades() on recursive/client calls
         * and on 19 out of every 20 server ticks.
         */
        if (processing) {
            cir.setReturnValue(storedData);
            return;
        }

        World world = housing.getWorldObj();

        if (world.isRemote) {
            cir.setReturnValue(storedData);
            return;
        }

        long worldTime = world.getTotalWorldTime();

        if ((worldTime % 20L) != 0L) {
            cir.setReturnValue(storedData);
            return;
        }

        /*
         * No focus upgrade: do not cancel. Career Bees executes its original,
         * completely unchanged scan.
         */
        if (!TemporalFocusUpgradeHelper.hasFocusUpgrade(housing)) {
            return;
        }

        /*
         * Real server worlds are WorldServer. Fail closed for focused mode rather
         * than falling back to Career Bees' unfiltered scan in an unusual World
         * implementation.
         */
        if (!(world instanceof WorldServer)) {
            cir.setReturnValue(storedData);
            return;
        }

        BlockPos source = housing.getCoordinates().toImmutable();

        TObjectIntHashMap<BlockPos> targets =
                posToTick.computeIfAbsent(
                        world,
                        ignored -> new TObjectIntHashMap<>()
                );

        /*
         * Preserve Career Bees' anti-cascade rule. If another Temporal source is
         * currently accelerating this source, it does not register its own area.
         *
         * This may also suppress it briefly while a stale TTL entry expires,
         * exactly like Career Bees' original behavior.
         */
        if (targets.containsKey(source)) {
            cir.setReturnValue(storedData);
            return;
        }

        /*
         * Career Bees normally uses the source position as a map marker to prevent
         * duplicate scans. Focused mode deliberately does not add the source to
         * posToTick because the source must not accelerate itself.
         *
         * Use a separate bounded per-world scan state instead.
         */
        FocusedScanState scanState =
                capnsbeeaddon$focusedScanStates.computeIfAbsent(
                        world,
                        ignored -> new FocusedScanState()
                );

        if (!scanState.markScanned(worldTime, source)) {
            cir.setReturnValue(storedData);
            return;
        }

        try {
            processing = true;

            capnsbeeaddon$registerFocusedTargets(
                    (WorldServer) world,
                    source,
                    genome,
                    housing,
                    targets
            );
        } finally {
            processing = false;
        }

        cir.setReturnValue(storedData);
    }

    @Inject(
            method = "handleBlock",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void capnsbeeaddon$filterDirectBlockTarget(
            World world,
            BlockPos pos,
            EnumFacing facing,
            IBeeGenome genome,
            IBeeHousing housing,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!TemporalFocusUpgradeHelper.hasFocusUpgrade(housing)) {
            return;
        }

        if (!world.isRemote && world.isBlockLoaded(pos, false)) {
            TileEntity tile = world.getTileEntity(pos);
            if (capnsbeeaddon$isEligibleTarget(
                    tile,
                    pos,
                    housing.getCoordinates()
            )) {
                TObjectIntHashMap<BlockPos> targets =
                        posToTick.computeIfAbsent(
                                world,
                                ignored -> new TObjectIntHashMap<>()
                        );
                targets.put(pos.toImmutable(), 40);
            }
        }

        // Focused mode intentionally does not schedule random block updates
        // and never loads a chunk just to inspect a direct target.
        cir.setReturnValue(true);
    }

    @Unique
    private void capnsbeeaddon$registerFocusedTargets(
            WorldServer world,
            BlockPos source,
            IBeeGenome genome,
            IBeeHousing housing,
            TObjectIntHashMap<BlockPos> targets
    ) {
        Vec3d territory = EffectBase.getTerritory(genome, housing);

        int radiusX = MathHelper.floor(territory.x);
        int radiusY = MathHelper.floor(territory.y);
        int radiusZ = MathHelper.floor(territory.z);

        int minX = source.getX() - radiusX;
        int minY = source.getY() - radiusY;
        int minZ = source.getZ() - radiusZ;
        int maxX = source.getX() + radiusX;
        int maxY = source.getY() + radiusY;
        int maxZ = source.getZ() + radiusZ;

        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk chunk = world.getChunkProvider().getLoadedChunk(
                        chunkX,
                        chunkZ
                );
                if (chunk == null) {
                    continue;
                }

                for (TileEntity tile : chunk.getTileEntityMap().values()) {
                    if (tile == null || tile.isInvalid()) {
                        continue;
                    }

                    BlockPos targetPos = tile.getPos();
                    if (targetPos.getX() < minX || targetPos.getX() > maxX
                            || targetPos.getY() < minY || targetPos.getY() > maxY
                            || targetPos.getZ() < minZ || targetPos.getZ() > maxZ) {
                        continue;
                    }

                    if (capnsbeeaddon$isEligibleTarget(
                            tile,
                            targetPos,
                            source
                    )) {
                        targets.put(targetPos.toImmutable(), 40);
                    }
                }
            }
        }
    }

    @Unique
    private static boolean capnsbeeaddon$isEligibleTarget(
            TileEntity tile,
            BlockPos targetPos,
            BlockPos sourcePos
    ) {
        return tile instanceof IIndustrialApiary
                && tile instanceof ITickable
                && !targetPos.equals(sourcePos);
    }
}
