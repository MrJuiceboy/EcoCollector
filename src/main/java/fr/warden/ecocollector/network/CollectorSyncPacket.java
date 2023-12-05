package fr.warden.ecocollector.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import io.netty.buffer.ByteBuf;

public class CollectorSyncPacket implements IMessage {
    public int x, y, z;
    public int level;
    public int experience;
    public int maxExperience;

    public CollectorSyncPacket() {}

    public CollectorSyncPacket(TileEntityCollector collector) {
        this.x = collector.xCoord;
        this.y = collector.yCoord;
        this.z = collector.zCoord;
        this.level = collector.getLevel();
        this.experience = collector.getExperience();
        this.maxExperience = collector.getMaxExperience();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(level);
        buf.writeInt(experience);
        buf.writeInt(maxExperience);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        level = buf.readInt();
        experience = buf.readInt();
        maxExperience = buf.readInt();
    }
}
