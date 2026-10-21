package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.rwtema.careerbees.effects.EffectPower;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.genetics.IEffectData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EffectPower.class, remap = false)
public abstract class EffectPowerSidedEnergyMixin {

    @Redirect(
            method = "doEffectBase(Lforestry/api/apiculture/IBeeGenome;"
                    + "Lforestry/api/genetics/IEffectData;Lforestry/api/apiculture/IBeeHousing;"
                    + "Lcom/rwtema/careerbees/effects/settings/IEffectSettingsHolder;)"
                    + "Lforestry/api/genetics/IEffectData;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;"
                    + "getCapability(Lnet/minecraftforge/common/capabilities/Capability;"
                    + "Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", remap = false),
            require = 1,
            allow = 1
    )
    private <T> T capnsbeeaddon$housingEnergyHandler(
            TileEntity tile, Capability<T> capability, EnumFacing side,
            IBeeGenome genome, IEffectData storedData, IBeeHousing housing,
            IEffectSettingsHolder settings
    ) {
        T storage = tile.getCapability(capability, side);
        if (storage != null && ((IEnergyStorage) storage).canReceive()) {
            return storage;
        }
        EnumFacing contact = capnsbeeaddon$findHousingContact(tile.getPos(), housing);
        return contact == null ? storage : tile.getCapability(capability, contact);
    }

    @Redirect(
            method = "canHandleBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                    + "Lforestry/api/apiculture/IBeeGenome;Lnet/minecraft/util/EnumFacing;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;"
                    + "hasCapability(Lnet/minecraftforge/common/capabilities/Capability;"
                    + "Lnet/minecraft/util/EnumFacing;)Z", remap = false),
            require = 1,
            allow = 1
    )
    private boolean capnsbeeaddon$gunTargetCapability(
            TileEntity tile, Capability<?> capability, EnumFacing side,
            World world, BlockPos pos, IBeeGenome genome, EnumFacing hitFace
    ) {
        return tile.hasCapability(capability, side)
                || (hitFace != null && tile.hasCapability(capability, hitFace));
    }

    @Redirect(
            method = "processingTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                    + "Lforestry/api/apiculture/IBeeGenome;Lforestry/api/apiculture/IBeeHousing;"
                    + "Lnet/minecraft/util/EnumFacing;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;"
                    + "hasCapability(Lnet/minecraftforge/common/capabilities/Capability;"
                    + "Lnet/minecraft/util/EnumFacing;)Z", remap = false),
            require = 1,
            allow = 1
    )
    private boolean capnsbeeaddon$gunProcessingCapability(
            TileEntity tile, Capability<?> capability, EnumFacing side,
            World world, BlockPos pos, IBeeGenome genome, IBeeHousing housing, EnumFacing hitFace
    ) {
        return tile.hasCapability(capability, side)
                || (hitFace != null && tile.hasCapability(capability, hitFace));
    }

    @Redirect(
            method = "processingTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                    + "Lforestry/api/apiculture/IBeeGenome;Lforestry/api/apiculture/IBeeHousing;"
                    + "Lnet/minecraft/util/EnumFacing;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;"
                    + "getCapability(Lnet/minecraftforge/common/capabilities/Capability;"
                    + "Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", remap = false),
            require = 1,
            allow = 1
    )
    private <T> T capnsbeeaddon$gunEnergyHandler(
            TileEntity tile, Capability<T> capability, EnumFacing side,
            World world, BlockPos pos, IBeeGenome genome, IBeeHousing housing, EnumFacing hitFace
    ) {
        T storage = tile.getCapability(capability, side);
        if ((storage != null && ((IEnergyStorage) storage).canReceive()) || hitFace == null) {
            return storage;
        }
        return tile.getCapability(capability, hitFace);
    }

    @Unique
    private static EnumFacing capnsbeeaddon$findHousingContact(BlockPos receiver, IBeeHousing housing) {
        BlockPos origin = housing.getCoordinates();
        int dx = origin.getX() - receiver.getX();
        int dy = origin.getY() - receiver.getY();
        int dz = origin.getZ() - receiver.getZ();
        for (EnumFacing face : EnumFacing.VALUES) {
            if (dx == face.getFrontOffsetX() && dy == face.getFrontOffsetY() && dz == face.getFrontOffsetZ()) {
                return face;
            }
        }

        // Alveary receivers may touch a part far from the housing's reference position.
        World world = housing.getWorldObj();
        IBeeHousingInventory inventory = housing.getBeeInventory();
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (EnumFacing face : EnumFacing.VALUES) {
            neighbour.setPos(receiver).move(face);
            if (!world.isBlockLoaded(neighbour)) {
                continue;
            }
            TileEntity part = world.getTileEntity(neighbour);
            if (part instanceof IBeeHousing && ((IBeeHousing) part).getBeeInventory() == inventory) {
                return face;
            }
        }
        return null;
    }
}
