package fr.warden.ecocollector.network.handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fr.warden.ecocollector.gui.GUICollector;
import fr.warden.ecocollector.network.CollectorSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import fr.warden.ecocollector.tileentity.TileEntityCollector;

public class CollectorSyncPacketHandler implements IMessageHandler<CollectorSyncPacket, IMessage> {
    @Override
    public IMessage onMessage(final CollectorSyncPacket message, MessageContext ctx) {
        if (Minecraft.getMinecraft().theWorld != null) {
            TileEntity tileEntity = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
            if (tileEntity instanceof TileEntityCollector) {
                final TileEntityCollector collector = (TileEntityCollector) tileEntity;
                Minecraft.getMinecraft().theWorld.scheduleBlockUpdate(message.x, message.y, message.z, collector.getBlockType(), 0);
                collector.setLevel(message.level);
                collector.setExperience(message.experience);
                collector.setMaxExperience(message.maxExperience);

                if (Minecraft.getMinecraft().currentScreen instanceof GUICollector) {
                    ((GUICollector) Minecraft.getMinecraft().currentScreen).updateCollectorData(collector);
                }
            }
        }
        return null;
    }
}
