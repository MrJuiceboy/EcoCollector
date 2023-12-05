package fr.warden.ecocollector;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import fr.warden.ecocollector.block.BlockCollector;
import fr.warden.ecocollector.gui.handler.GuiHandlerCollector;
import fr.warden.ecocollector.network.*;
import fr.warden.ecocollector.network.handler.CollectorSyncPacketHandler;
import fr.warden.ecocollector.network.handler.CollectorSyncPacketRequestHandler;
import fr.warden.ecocollector.network.handler.UpgradeCollectorPacketHandler;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

@Mod(modid = EcoCollectorMod.MOD_ID, version = EcoCollectorMod.VERSION)
public class EcoCollectorMod {
    public static final String MOD_ID = "ecocollector";
    public static final String VERSION = "1.0";

    public static final int COLLECTOR_GUI_ID = 1;

    public static Block blockCollector;

    public static SimpleNetworkWrapper network;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        blockCollector = new BlockCollector(Material.iron, "blockcollector").setBlockName("blockcollector");

        GameRegistry.registerBlock(blockCollector, "blockcollector");

        GameRegistry.registerTileEntity(TileEntityCollector.class, "tileEntityCollector");

        NetworkRegistry.INSTANCE.registerGuiHandler(EcoCollectorMod.MOD_ID, new GuiHandlerCollector());

        network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
        network.registerMessage(GuiOpenPacket.Handler.class, GuiOpenPacket.class, 0, Side.SERVER);
        network.registerMessage(OpenChestPacket.Handler.class, OpenChestPacket.class, 1, Side.SERVER);
        network.registerMessage(UpgradeCollectorPacketHandler.class, UpgradeCollectorPacket.class, 2, Side.SERVER);
        network.registerMessage(CollectorSyncPacketHandler.class, CollectorSyncPacket.class, 3, Side.CLIENT);
        network.registerMessage(CollectorSyncPacketRequestHandler.class, CollectorSyncPacketRequest.class, 4, Side.SERVER);
    }
}
