package fr.warden.ecocollector.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class CollectorSyncPacketRequest implements IMessage {
    private int x, y, z;

    public CollectorSyncPacketRequest() {}

    public CollectorSyncPacketRequest(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}
