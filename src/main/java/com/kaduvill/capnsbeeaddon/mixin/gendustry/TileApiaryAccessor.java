package com.kaduvill.capnsbeeaddon.mixin.gendustry;

import net.bdew.gendustry.gui.rscontrol.DataSlotRSMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(
        targets = "net.bdew.gendustry.machines.apiary.TileApiary",
        remap = false
)
public interface TileApiaryAccessor {

    @Invoker(value = "rsmode", remap = false)
    DataSlotRSMode capnsbeeaddon$getRedstoneMode();
}