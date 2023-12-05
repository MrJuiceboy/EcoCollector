package fr.warden.ecocollector.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

public class OpenChestPacket implements IMessage {
    private int x, y, z;

    public OpenChestPacket() {}

    public OpenChestPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<OpenChestPacket, IMessage> {
        @Override
        public IMessage onMessage(OpenChestPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            TileEntity tileEntity = world.getTileEntity(message.x, message.y, message.z);
            if (tileEntity instanceof TileEntityChest) {
                player.displayGUIChest((TileEntityChest) tileEntity);
            }
            return null;
        }
    }
}
