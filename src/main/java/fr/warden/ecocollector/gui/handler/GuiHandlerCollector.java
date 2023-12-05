package fr.warden.ecocollector.gui.handler;

import fr.warden.ecocollector.container.ContainerCollector;
import fr.warden.ecocollector.gui.GUICollector;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiHandlerCollector implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntityCollector tileEntity = (TileEntityCollector) world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            return new ContainerCollector(player.inventory, tileEntity);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntityCollector tileEntity = (TileEntityCollector) world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            return new GUICollector(player.inventory, tileEntity);
        }
        return null;
    }
}