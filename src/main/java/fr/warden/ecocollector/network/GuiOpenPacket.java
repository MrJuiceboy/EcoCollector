package fr.warden.ecocollector.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiOpenPacket implements IMessage {

    private int x, y, z;
    private boolean isOpen;

    public GuiOpenPacket() {}

    public GuiOpenPacket(int x, int y, int z, boolean isOpen) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isOpen = isOpen;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        isOpen = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(isOpen);
    }

    public static class Handler implements IMessageHandler<GuiOpenPacket, IMessage> {
        @Override
        public IMessage onMessage(GuiOpenPacket message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileEntityCollector) {
                ((TileEntityCollector) te).setGuiOpen(message.isOpen);
            }
            return null;
        }
    }
}
