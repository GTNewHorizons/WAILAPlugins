package tterrag.wailaplugins.plugins;

import static mcp.mobius.waila.api.SpecialChars.GREEN;
import static mcp.mobius.waila.api.SpecialChars.RED;
import static mcp.mobius.waila.api.SpecialChars.RESET;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.enderio.core.common.util.BlockCoord;

import ic2.api.crops.CropCard;
import ic2.api.crops.ICropTile;
import ic2.core.crop.TileEntityCrop;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import tterrag.wailaplugins.api.Plugin;

@Plugin(name = "IC2Crops", deps = "IC2")
public class PluginIC2Crops extends PluginBase {

    @Override
    public void load(IWailaRegistrar registrar) {
        super.load(registrar);

        addConfig("cropName");
        addConfig("cropStats");
        addConfig("plantInfo");
        addConfig("envInfo");

        registerBody(ICropTile.class);
        registerNBT(ICropTile.class);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unused")
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor) {
        final TileEntity tile = accessor.getTileEntity();
        MovingObjectPosition pos = accessor.getPosition();
        NBTTagCompound tag = accessor.getNBTData();
        final byte scanLevel = tag.getByte("scanLevel");

        final ICropTile tCrop = tile instanceof ICropTile ? (ICropTile) tile : null;
        final CropCard crop = tCrop != null ? tCrop.getCrop() : null;
        if (tCrop != null && crop != null) {
            final int cropTier = crop.tier();
            if (getConfig("cropName")) {
                if (scanLevel > 0) {

                    double growth = (tag.getInteger("growthPoints") / (double) crop.growthDuration(tCrop)) * 100;
                    final int size = tCrop.getSize();
                    final int optimalSize = tag.getInteger("optimalHarvest");
                    final int maxSize = crop.maxSize();

                    final String canGrow = size == maxSize ? (GREEN + "Max Size" + RESET)
                            : ("Can grow: " + (tag.getBoolean("canGrow") ? (GREEN + "Yes") : (RED + "No")) + RESET);
                    final String canHarvest = size == optimalSize ? (GREEN + "Optimal Harvest" + RESET)
                            : ("Can Harvest: " + (tag.getBoolean("canHarvest") ? (GREEN + "Yes") : (RED + "No"))
                                    + RESET);

                    currenttip.add(
                            String.format("%s (Tier %d)", crop.name(), cropTier) + "  "
                                    + canGrow
                                    + (cropTier > 0 ? ("  " + canHarvest) : ""));
                    currenttip.add(
                            "Stage -- Size: " + tCrop.getSize()
                                    + "/"
                                    + maxSize
                                    + ""
                                    + (size < maxSize ? ("  Growth: " + (int) growth + "%") : ""));
                } else if (cropTier == 0) {
                    currenttip.add(RED + "WEED - [420 blaze it]" + RESET);
                }
            }
            if (scanLevel >= 4) {
                if (getConfig("cropStats")) {
                    if (tag.getByte("growth") >= 24) currenttip.add(
                            "Stats -- Growth: " + RED
                                    + tag.getByte("growth")
                                    + RESET
                                    + "  Gain: "
                                    + tag.getByte("gain")
                                    + "  Resistance: "
                                    + tag.getByte("resistance"));
                    else currenttip.add(
                            "Stats -- Growth: " + tag.getByte("growth")
                                    + "  Gain: "
                                    + tag.getByte("gain")
                                    + "  Resistance: "
                                    + tag.getByte("resistance"));
                }
                if (getConfig("plantInfo")) {
                    currenttip.add(
                            "Plant -- Fertilizer: " + tag.getInteger("fertilizer")
                                    + "  Water: "
                                    + tag.getInteger("water")
                                    + "  Weed-Ex: "
                                    + tag.getInteger("weedex"));
                }
                if (getConfig("envInfo")) {
                    currenttip.add(
                            "Environ -- Nutrients: " + tag.getInteger("nutrients")
                                    + "  Humidity: "
                                    + tag.getInteger("humidity")
                                    + "  Air-Quality: "
                                    + tag.getInteger("airQuality"));
                }
            }
        }
    }

    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        final ICropTile tCrop = tile instanceof ICropTile ? (ICropTile) tile : null;
        final TileEntityCrop teCrop = tile instanceof TileEntityCrop ? (TileEntityCrop) tile : null;

        if (tCrop != null) {
            final CropCard crop = tCrop.getCrop();
            final boolean canGrow = crop != null && crop.canGrow(tCrop);
            final boolean canHarvest = crop != null && crop.canBeHarvested(tCrop);
            final int optimalHarvest = crop != null ? crop.getOptimalHavestSize(tCrop) : -1;
            tag.setBoolean("canGrow", canGrow);
            tag.setBoolean("canHarvest", canHarvest);
            tag.setInteger("optimalHarvest", optimalHarvest);

            tag.setByte("scanLevel", tCrop.getScanLevel());
            tag.setByte("growth", tCrop.getGrowth());
            tag.setByte("gain", tCrop.getGain());
            tag.setByte("resistance", tCrop.getResistance());

            // Plant Info
            tag.setInteger("fertilizer", tCrop.getNutrientStorage());
            tag.setInteger("water", tCrop.getHydrationStorage());
            tag.setInteger("weedex", tCrop.getWeedExStorage());

            // Environment Info
            tag.setInteger("nutrients", tCrop.getNutrients());
            tag.setInteger("humidity", tCrop.getHumidity());
            tag.setInteger("airQuality", tCrop.getAirQuality());

            tag.setInteger("growthPoints", teCrop != null ? teCrop.growthPoints : -1);

        }
        tile.writeToNBT(tag);
    }
}
