package fr.warden.ecocollector.network.handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fr.warden.ecocollector.network.CollectorSyncPacket;
import fr.warden.ecocollector.network.CollectorSyncPacketRequest;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import net.minecraft.tileentity.TileEntity;

public class CollectorSyncPacketRequestHandler implements IMessageHandler<CollectorSyncPacketRequest, IMessage> {

    @Override
    public IMessage onMessage(CollectorSyncPacketRequest message, MessageContext ctx) {
        TileEntity tileEntity = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.getX(), message.getY(), message.getZ());
        if (tileEntity instanceof TileEntityCollector) {
            TileEntityCollector collector = (TileEntityCollector) tileEntity;
            return new CollectorSyncPacket(collector);
        }
        return null;
    }
}
