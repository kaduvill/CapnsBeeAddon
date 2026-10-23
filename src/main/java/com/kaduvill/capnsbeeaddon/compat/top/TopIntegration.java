package com.kaduvill.capnsbeeaddon.compat.top;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.entity.EntityChunkData;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IBeeHousing;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;
import java.util.function.Function;

/** Loaded by TOP through IMC only when the optional integration is enabled. */
public final class TopIntegration implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe top) {
        top.registerProvider(new HelperBeeProvider());
        return null;
    }

    private static final class HelperBeeProvider implements IProbeInfoProvider {

        private final boolean showYente = CapnsBeeAddonConfig.get().topYenteBee;
        private final boolean showStudent = CapnsBeeAddonConfig.get().topStudentBee;

        // Shared display templates; never deserialize the attached bees just to show their icons.
        private final ItemStack yenteIcon = showYente ? BeeManager.beeRoot.getMemberStack(
                CareerBeeSpecies.YENTE.get(), EnumBeeType.PRINCESS) : ItemStack.EMPTY;
        private final ItemStack studentIcon = showStudent ? BeeManager.beeRoot.getMemberStack(
                CareerBeeSpecies.STUDENT.get(), EnumBeeType.DRONE) : ItemStack.EMPTY;

        @Override
        public String getID() {
            return CapnsBeeAddon.MODID + ":helper_bees";
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo info, EntityPlayer player,
                                 World world, IBlockState state, IProbeHitData hitData) {
            if (mode != ProbeMode.EXTENDED || world.isRemote) return;
            BlockPos pos = hitData.getPos();

            Chunk chunk = world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null) return;
            if (!(chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) instanceof IBeeHousing)) return;

            // Read the existing chunk entity once. Probing must not create helper data.
            EntityChunkData data = EntityChunkData.getChunkDataEntity(chunk);
            if (data == null) return;

            if (showYente && hasHelper(data, "bee_yente", pos)) {
                addLine(info, yenteIcon, "top.capnsbeeaddon.yente");
            }
            if (showStudent && hasHelper(data, "bee_student", pos)) {
                addLine(info, studentIcon, "top.capnsbeeaddon.student");
            }
        }

        private static boolean hasHelper(EntityChunkData data, String managerId, BlockPos pos) {
            Object helpers = data.objectHashMap.get(EntityChunkData.managers.get(managerId));
            return helpers instanceof Map && ((Map<?, ?>) helpers).containsKey(pos);
        }

        private static void addLine(IProbeInfo info, ItemStack icon, String translationKey) {
            info.horizontal(info.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER).spacing(3))
                    .item(icon)
                    .text(IProbeInfo.STARTLOC + translationKey + IProbeInfo.ENDLOC);
        }
    }
}
