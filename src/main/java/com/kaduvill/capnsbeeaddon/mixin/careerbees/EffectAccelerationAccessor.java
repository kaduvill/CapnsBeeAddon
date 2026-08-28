package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.rwtema.careerbees.effects.EffectAcceleration;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.WeakHashMap;

@Mixin(value = EffectAcceleration.class, remap = false)
public interface EffectAccelerationAccessor {

    @Accessor("posToTick")
    WeakHashMap<World, TObjectIntHashMap<BlockPos>>
    capnsbeeaddon$getRegisteredPositions();
}