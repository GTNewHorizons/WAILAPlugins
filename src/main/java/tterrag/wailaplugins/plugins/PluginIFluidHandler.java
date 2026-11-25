package tterrag.wailaplugins.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.enderio.core.common.util.BlockCoord;

import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.SpecialChars;
import tterrag.wailaplugins.api.Plugin;
import tterrag.wailaplugins.config.WPConfigHandler;

@Plugin(name = "IFluidHandler")
public class PluginIFluidHandler extends PluginBase {

    @Override
    public void load(IWailaRegistrar registrar) {
        super.load(registrar);

        registerBody(IFluidHandler.class);
        registerNBT(IFluidHandler.class);
    }

    @Override
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        FluidTankInfo[] infos = readFluidInfosFromNBT(accessor.getNBTData());
        final int max = WPConfigHandler.maxFluidTanks;

        if (max < 0 || infos.length <= max) {
            addTankTooltip(currenttip, infos);
            return;
        }

        // too many tanks. here we summarize the tanks into one or two lines so it does not overflow the screen
        FluidTankInfo[] hidden = Arrays.copyOfRange(infos, max, infos.length);
        String contents = summarizeContents(hidden);

        if (max == 0) {
            final boolean single = infos.length == 1;
            if (contents == null) {
                currenttip.add(lang.localize(single ? "fluidTankAllEmpty" : "fluidTanksAllEmpty", infos.length));
            } else {
                currenttip.add(
                        lang.localize(single ? "fluidTankContaining" : "fluidTanksContaining", infos.length, contents));
            }
        } else {
            addTankTooltip(currenttip, Arrays.copyOf(infos, max));
            if (contents == null) {
                currenttip.add(lang.localize("moreTanksAllEmpty", hidden.length));
            } else {
                currenttip.add(lang.localize("moreTanksContaining", hidden.length, contents));
            }
        }
    }

    /**
     * Aggregates the hidden tanks that actually hold fluid, preserving first-seen tank order.
     *
     * @return the fluid content list as a human-readable string, or null if every hidden tank is empty
     */
    private static String summarizeContents(FluidTankInfo[] tanks) {
        // key = fluid registry name; LinkedHashMap keeps tank order stable between ticks
        Map<String, Integer> amounts = new LinkedHashMap<>();
        Map<String, String> names = new LinkedHashMap<>();
        for (FluidTankInfo tank : tanks) {
            // empty or zero sized tanks count towards the limit but have nothing to report
            if (tank == null || tank.fluid == null || tank.fluid.amount <= 0) {
                continue;
            }
            String key = tank.fluid.getFluid().getName();
            amounts.merge(key, tank.fluid.amount, Integer::sum);
            if (!names.containsKey(key)) {
                names.put(key, tank.fluid.getLocalizedName());
            }
        }
        if (amounts.isEmpty()) {
            return null;
        }

        List<String> parts = new ArrayList<>(amounts.size());
        for (Map.Entry<String, Integer> entry : amounts.entrySet()) {
            parts.add(lang.localize("tankContents", entry.getValue(), names.get(entry.getKey())));
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(i == parts.size() - 1 ? " " + lang.localize("listAnd") + " " : ", ");
            }
            sb.append(parts.get(i));
        }
        return sb.toString();
    }

    public static void addTankTooltip(List<String> currenttip, FluidTankInfo... tanks) {
        for (FluidTankInfo tank : tanks) {
            if (tank != null) {
                final boolean isEmpty = tank.fluid == null;
                currenttip.add(
                        SpecialChars.getRenderString(
                                "waila.fluid",
                                isEmpty ? "EMPTYFLUID" : tank.fluid.getFluid().getName(),
                                isEmpty ? "EMPTYFLUID" : tank.fluid.getLocalizedName(),
                                (isEmpty ? 0 : tank.fluid.amount) + "",
                                tank.capacity + ""));
            }
        }
    }

    @Override
    protected void getNBTData(TileEntity te, NBTTagCompound tag, World world, BlockCoord pos) {
        writeFluidInfoToNBT((IFluidHandler) te, tag);
    }

    public static void writeFluidInfoToNBT(IFluidHandler te, NBTTagCompound tag) {
        FluidTankInfo[] infos = ((IFluidHandler) te).getTankInfo(ForgeDirection.UNKNOWN);
        if (infos != null && infos.length > 0) {
            NBTTagList infoList = new NBTTagList();
            for (FluidTankInfo info : infos) {
                NBTTagCompound infoTag = new NBTTagCompound();
                writeFluidInfoToNBT(info, infoTag);
                infoList.appendTag(infoTag);
            }
            tag.setTag("fluidInfo", infoList);
        }
    }

    public static FluidTankInfo[] readFluidInfosFromNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("fluidInfo", NBT.TAG_COMPOUND);
        FluidTankInfo[] ret = new FluidTankInfo[list.tagCount()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = readFluidInfoFromNBT(list.getCompoundTagAt(i));
        }
        return ret;
    }

    public static void writeFluidInfoToNBT(FluidTankInfo info, NBTTagCompound tag) {
        if (info.fluid != null) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            info.fluid.writeToNBT(fluidTag);
            tag.setTag("fluid", fluidTag);
        }
        tag.setInteger("capacity", info.capacity);
    }

    public static FluidTankInfo readFluidInfoFromNBT(NBTTagCompound tag) {
        FluidStack fluid = tag.hasKey("fluid") ? FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid")) : null;
        int capacity = tag.getInteger("capacity");
        return new FluidTankInfo(fluid, capacity);
    }
}
