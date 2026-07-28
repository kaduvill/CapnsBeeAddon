package com.kaduvill.capnsbeeaddon.client.territory;

import com.rwtema.careerbees.effects.EffectBase;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Resolves the active Career Bee effect and its current, fully modified
 * territory from a bee housing.
 */
public final class CareerTerritoryResolver {

    private CareerTerritoryResolver() {
    }

    @Nullable
    public static CareerTerritoryInfo resolve(@Nullable IBeeHousing housing) {
        if (housing == null) {
            return null;
        }

        World world = housing.getWorldObj();
        IBeeHousingInventory inventory = housing.getBeeInventory();

        if (world == null || inventory == null) {
            return null;
        }

        ItemStack queenStack = inventory.getQueen();
        if (queenStack.isEmpty()) {
            return null;
        }

        IBee bee = BeeManager.beeRoot.getMember(queenStack);
        if (bee == null) {
            return null;
        }

        IBeeGenome genome = bee.getGenome();
        IAlleleBeeEffect allele = genome.getEffect();

        if (!(allele instanceof EffectBase)) {
            return null;
        }

        EffectBase effect = (EffectBase) allele;
        if (!effect.isValidSpecies(genome)) {
            return null;
        }

        BlockPos source = housing.getCoordinates().toImmutable();
        AxisAlignedBB bounds = EffectBase.getAABB(genome, housing);

        return new CareerTerritoryInfo(
                world.provider.getDimension(),
                source,
                bounds,
                effect.getAlleleName()
        );
    }
}
