package tterrag.wailaplugins.plugins;

import com.enderio.core.common.util.BlockCoord;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicMachine;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Transformer;
import gregtech.common.covers.GT_Cover_Fluidfilter;
import gregtech.common.tileentities.boilers.GT_MetaTileEntity_Boiler_Solar;
import gregtech.common.tileentities.machines.long_distance.GT_MetaTileEntity_LongDistancePipelineBase;
import lombok.SneakyThrows;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tterrag.wailaplugins.api.Plugin;
import static mcp.mobius.waila.api.SpecialChars.RESET;
import static mcp.mobius.waila.api.SpecialChars.GOLD;
import static mcp.mobius.waila.api.SpecialChars.BLUE;
import static mcp.mobius.waila.api.SpecialChars.GREEN;
import static mcp.mobius.waila.api.SpecialChars.RED;

import java.util.List;

@Plugin(name = "Gregtech5U", deps = "gregtech")
public class PluginGregtech5U extends PluginBase
{

    @Override
    public void load(IWailaRegistrar registrar)
    {
        super.load(registrar);

        addConfig("machineFacing");
        addConfig("transformer");
        addConfig("solar");
        addConfig("LDP");
        addConfig("basicmachine");
        addConfig("multiblock");
        addConfig("fluidFilter");
        registerBody(BaseTileEntity.class);
        registerNBT(BaseTileEntity.class);
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("unused")
    protected void getBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor)
    {
        final TileEntity tile = accessor.getTileEntity();
        MovingObjectPosition pos = accessor.getPosition();
        NBTTagCompound tag = accessor.getNBTData();
        final int side = (byte)accessor.getSide().ordinal();
        if (tile instanceof IGregTechTileEntity) {
            ((IGregTechTileEntity) tile).getMetaTileEntity().getWailaBody(stack,currenttip,pos,tag,side);
        }
    }


    @Override
    @SneakyThrows
    protected void getNBTData(TileEntity tile, NBTTagCompound tag, World world, BlockCoord pos) {
        if (tile instanceof IGregTechTileEntity) {
            ((IGregTechTileEntity) tile).getMetaTileEntity().getWailaNBT(tag, world, pos);
        }
    }
}
