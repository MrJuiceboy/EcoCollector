package fr.warden.ecocollector.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class UpgradeCollectorPacket implements IMessage {
    private int x, y, z, expNeeded;

    public UpgradeCollectorPacket() {}

    public UpgradeCollectorPacket(int x, int y, int z, int expNeeded) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.expNeeded = expNeeded;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.expNeeded = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(expNeeded);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getExpNeeded() { return expNeeded; }
}
