package fr.warden.ecocollector.network.handler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fr.warden.ecocollector.network.UpgradeCollectorPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import fr.warden.ecocollector.tileentity.TileEntityCollector;

public class UpgradeCollectorPacketHandler implements IMessageHandler<UpgradeCollectorPacket, IMessage> {

    @Override
    public IMessage onMessage(UpgradeCollectorPacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;

        int x = message.getX();
        int y = message.getY();
        int z = message.getZ();
        int expToAdd = message.getExpNeeded();

        if (world.blockExists(x, y, z)) {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityCollector) {
                TileEntityCollector collector = (TileEntityCollector) tileEntity;

                if (player.experienceLevel >= expToAdd) {
                    player.addExperienceLevel(-expToAdd);
                    collector.addExperience(expToAdd, player);
                } else {
                    String errorMessage = String.format("Vous avez besoin de %d niveaux supplémentaires pour effectuer une mise à niveau.", expToAdd - player.experienceLevel);
                    player.addChatMessage(new ChatComponentText(errorMessage));
                    player.closeScreen();
                }
            }
        }

        return null;
    }
}
