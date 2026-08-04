package com.kaduvill.capnsbeeaddon.mixin.gendustry;

import net.bdew.gendustry.machines.apiary.TileApiary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses the owning TileApiary retained by Gendustry's generated
 * server-tick closure.
 */
@Mixin(
        targets =
                "net.bdew.gendustry.machines.apiary."
                        + "TileApiary$$anonfun$2",
        remap = false
)
public interface TileApiaryServerTickClosureAccessor {

    @Accessor(value = "$outer", remap = false)
    TileApiary capnsbeeaddon$getApiary();
}