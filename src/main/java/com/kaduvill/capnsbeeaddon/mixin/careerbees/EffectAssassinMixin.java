package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.rwtema.careerbees.effects.EffectAssassin;
import com.rwtema.careerbees.effects.settings.IEffectSettingsHolder;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.genetics.IEffectData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Mixin(value = EffectAssassin.class, remap = false)
public abstract class EffectAssassinMixin {

    /*
     * Add an override of EffectWorldInteraction's inherited method, rather than
     * injecting into a method Assassin does not declare. The inherited throttle
     * still decides when to call this. The Bee Gun uses handleBlock and
     * performPosEffect instead; those and the shared kill method stay untouched.
     */
    public void performEffect(
            IBeeGenome genome,
            IEffectData storedData,
            IBeeHousing housing,
            Random random,
            World world,
            BlockPos source,
            IBeeModifier housingModifier,
            IBeeModifier modeModifier,
            IEffectSettingsHolder settings
    ) {
        if (!(world instanceof WorldServer) || world.isRemote) {
            return;
        }

        IBeeHousingInventory sourceInventory = housing.getBeeInventory();
        if (sourceInventory == null) {
            return;
        }

        // Match EffectBase.getTerritory's float arithmetic using the supplied
        // modifiers. Calculate once per activation; upgrades can change mid-tick.
        Vec3i territory = genome.getTerritory();
        float modifier = housingModifier.getTerritoryModifier(genome, 1.0F)
                * modeModifier.getTerritoryModifier(genome, 1.0F);
        AxisAlignedBB bounds = new AxisAlignedBB(source).grow(
                territory.getX() * modifier,
                territory.getY() * modifier,
                territory.getZ() * modifier
        );
        int minX = MathHelper.floor(bounds.minX);
        int minY = Math.max(0, MathHelper.floor(bounds.minY));
        int minZ = MathHelper.floor(bounds.minZ);
        // WorldInteraction random X/Z maxima are exclusive, but Y is inclusive.
        int maxX = MathHelper.ceil(bounds.maxX);
        int maxY = Math.min(255, MathHelper.ceil(bounds.maxY));
        int maxZ = MathHelper.ceil(bounds.maxZ);

        Set<IBeeHousingInventory> seen = Collections.newSetFromMap(
                new IdentityHashMap<IBeeHousingInventory, Boolean>());
        TileEntity selected = null;
        BlockPos selectedPos = null;
        IBeeHousingInventory selectedInventory = null;
        int candidates = 0;
        WorldServer server = (WorldServer) world;
        for (int chunkX = minX >> 4; chunkX <= (maxX - 1) >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= (maxZ - 1) >> 4; chunkZ++) {
                Chunk chunk = server.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (Map.Entry<BlockPos, TileEntity> entry : chunk.getTileEntityMap().entrySet()) {
                    TileEntity tile = entry.getValue();
                    BlockPos pos = entry.getKey();
                    if (!(tile instanceof IBeeHousing) || tile.isInvalid()
                            || tile.getWorld() != world || tile == housing || pos.equals(source)
                            || pos.getX() < minX || pos.getX() >= maxX
                            || pos.getY() < minY || pos.getY() > maxY
                            || pos.getZ() < minZ || pos.getZ() >= maxZ) {
                        continue;
                    }

                    IBeeHousingInventory inventory = ((IBeeHousing) tile).getBeeInventory();
                    // Alveary parts share one inventory: protect the source and
                    // give each target queen one chance, regardless of part count.
                    if (inventory == null || inventory == sourceInventory || !seen.add(inventory)
                            || capnsbeeaddon$livingVictim(genome, inventory.getQueen()) == null) {
                        continue;
                    }
                    // Reservoir sampling avoids fixed map-order preference.
                    if (random.nextInt(++candidates) == 0) {
                        selected = tile;
                        selectedPos = pos.toImmutable();
                        selectedInventory = inventory;
                    }
                }
            }
        }

        // Finish every tile-map traversal before setQueen can trigger inventory
        // listeners or other world-changing callbacks.
        if (selected == null || selected.isInvalid() || selected.getWorld() != world
                || !selectedPos.equals(selected.getPos())) {
            return;
        }
        Chunk selectedChunk = server.getChunkProvider().getLoadedChunk(
                selectedPos.getX() >> 4, selectedPos.getZ() >> 4);
        if (selectedChunk == null || selectedChunk.getTileEntityMap().get(selectedPos) != selected
                || selectedPos.equals(source)) {
            return;
        }
        IBeeHousing target = (IBeeHousing) selected;
        if (target.getBeeInventory() != selectedInventory
                || selectedInventory == housing.getBeeInventory()) {
            return;
        }
        ItemStack queenStack = selectedInventory.getQueen();
        IBee queen = capnsbeeaddon$livingVictim(genome, queenStack);
        if (queen == null) {
            return;
        }

        ItemStack killedQueen = queenStack.copy();
        NBTTagCompound tag = killedQueen.getTagCompound();
        queen.setHealth(0);
        killedQueen.setTagCompound(queen.writeToNBT(tag == null ? new NBTTagCompound() : tag));
        // The target's next normal update owns death, offspring and pending
        // products. Keep Gendustry's death listener and princess reinsertion
        // within its own update instead of calling canWork here.
        selectedInventory.setQueen(killedQueen);
    }

    @Unique
    @Nullable
    private IBee capnsbeeaddon$livingVictim(IBeeGenome sourceGenome, ItemStack stack) {
        if (stack.isEmpty() || !BeeManager.beeRoot.isMember(stack, EnumBeeType.QUEEN)) {
            return null;
        }
        IBee queen = BeeManager.beeRoot.getMember(stack);
        // Preserve Assassin.canHandleBlock's primary-species identity check.
        return queen != null && queen.isAlive()
                && queen.getGenome().getPrimary() != sourceGenome.getPrimary() ? queen : null;
    }
}
