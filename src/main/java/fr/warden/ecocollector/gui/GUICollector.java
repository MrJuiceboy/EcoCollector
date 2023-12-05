package fr.warden.ecocollector.gui;

import fr.warden.ecocollector.EcoCollectorMod;
import fr.warden.ecocollector.container.ContainerCollector;
import fr.warden.ecocollector.network.CollectorSyncPacketRequest;
import fr.warden.ecocollector.network.OpenChestPacket;
import fr.warden.ecocollector.network.UpgradeCollectorPacket;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

public class GUICollector extends GuiContainer {
    private static final int UPGRADE_BUTTON_ID = 1;
    private static final int CHEST_BUTTON_ID = 2;
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation(EcoCollectorMod.MOD_ID, "textures/gui/widgets.png");
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(EcoCollectorMod.MOD_ID, "textures/gui/collector.png");

    private final TileEntityCollector tileEntityCollector;

    private int syncCountdown = 0;

    public GUICollector(InventoryPlayer invPlayer, TileEntityCollector tileEntityCollector) {
        super(new ContainerCollector(invPlayer, tileEntityCollector));
        this.tileEntityCollector = tileEntityCollector;
        this.xSize = 176;
        this.ySize = 166;

        if (this.tileEntityCollector != null) {
            this.tileEntityCollector.debugInfo();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        addButton(UPGRADE_BUTTON_ID, 132, 56);
        addButton(CHEST_BUTTON_ID, 10, 6);

        if (this.tileEntityCollector != null && this.mc.theWorld.isRemote) {
            EcoCollectorMod.network.sendToServer(new CollectorSyncPacketRequest(tileEntityCollector.xCoord, tileEntityCollector.yCoord, tileEntityCollector.zCoord));
        }
    }

    @SuppressWarnings("unchecked")
    private void addButton(int id, int x, int y) {
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        this.buttonList.add(new GuiButton(id, guiLeft + x, guiTop + y, 19, 19, ""));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(GUI_TEXTURE);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        drawLevelText(guiLeft, guiTop);
        drawProgressBar(guiLeft, guiTop);
    }

    private void drawLevelText(int guiLeft, int guiTop) {
        String levelText = "Niveau " + tileEntityCollector.getLevel() + "/5";
        int textWidth = this.fontRendererObj.getStringWidth(levelText);
        int textX = guiLeft + 42 + (89 - textWidth) / 2;
        int textY = guiTop + 6 + (18 - this.fontRendererObj.FONT_HEIGHT) / 2;
        this.fontRendererObj.drawString(levelText, textX, textY, 0x000000);
    }

    private void drawProgressBar(int guiLeft, int guiTop) {
        int progressBarWidth = 127;
        int filledWidth = (int) ((float) tileEntityCollector.getExperience() / tileEntityCollector.getMaxExperience() * progressBarWidth);
        filledWidth = Math.max(filledWidth, 1);
        drawRect(guiLeft + 24, guiTop + 36, guiLeft + 24 + filledWidth, guiTop + 45, 0xFF00FF00);

        String percentageText = String.format("%d%%", (int) ((float) tileEntityCollector.getExperience() / tileEntityCollector.getMaxExperience() * 100));
        String syncText = (syncCountdown > 0) ? String.format(" (Sync: %d s)", syncCountdown) : "";

        int percentageTextWidth = this.fontRendererObj.getStringWidth(percentageText);
        int syncTextWidth = this.fontRendererObj.getStringWidth(syncText);
        int totalTextWidth = percentageTextWidth + syncTextWidth;

        int textX = guiLeft + 24 + (progressBarWidth - totalTextWidth) / 2;
        int textY = guiTop + 36 + (9 - this.fontRendererObj.FONT_HEIGHT) / 2;

        this.fontRendererObj.drawString(percentageText, textX, textY, 0x000000);

        if (syncCountdown > 0) {
            this.fontRendererObj.drawString(syncText, textX + percentageTextWidth, textY, 0xFF0000);
        }
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        drawButtonTextures();
    }

    private void drawButtonTextures() {
        mc.getTextureManager().bindTexture(BUTTON_TEXTURE);
        for (Object buttonObj : this.buttonList) {
            GuiButton button = (GuiButton) buttonObj;
            int textureY = button.func_146115_a() ? 20 : 0;
            int textureX = button.id == UPGRADE_BUTTON_ID ? 0 : 20;
            this.drawTexturedModalRect(button.xPosition - guiLeft, button.yPosition - guiTop, textureX, textureY, 20, 20);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == UPGRADE_BUTTON_ID) {
            handleUpgradeAction();
            syncCountdown = 5;
        } else if (button.id == CHEST_BUTTON_ID) {
            handleChestAction();
        }
    }

    private void handleUpgradeAction() {
        int playerExp = mc.thePlayer.experienceLevel;
        int nextLevelExp = tileEntityCollector.calculateMaxExperience(tileEntityCollector.getLevel() + 1);
        int currentCollectorExp = tileEntityCollector.getExperience();
        int expNeeded = nextLevelExp > currentCollectorExp ? 1 : nextLevelExp - currentCollectorExp;

        if (playerExp >= expNeeded) {
            EcoCollectorMod.network.sendToServer(new UpgradeCollectorPacket(tileEntityCollector.xCoord, tileEntityCollector.yCoord, tileEntityCollector.zCoord, expNeeded));
        } else {
            mc.thePlayer.closeScreen();
            String message = "Vous avez besoin d'au moins " + expNeeded + " niveau(x) d'expérience pour effectuer une mise à niveau (vous avez actuellement " + playerExp + " niveaux).";
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }


    private void handleChestAction() {
        EcoCollectorMod.network.sendToServer(new OpenChestPacket(tileEntityCollector.xCoord, tileEntityCollector.yCoord - 1, tileEntityCollector.zCoord));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (syncCountdown > 0) {
            if (Minecraft.getMinecraft().theWorld.getTotalWorldTime() % 20 == 0) {
                syncCountdown--;
            }
        }

        if (this.tileEntityCollector != null && mc.theWorld != null) {
            TileEntity tileEntity = mc.theWorld.getTileEntity(tileEntityCollector.xCoord, tileEntityCollector.yCoord, tileEntityCollector.zCoord);
            if (tileEntity instanceof TileEntityCollector) {
                TileEntityCollector updatedCollector = (TileEntityCollector) tileEntity;
                this.tileEntityCollector.setLevel(updatedCollector.getLevel());
                this.tileEntityCollector.setExperience(updatedCollector.getExperience());
                this.tileEntityCollector.setMaxExperience(updatedCollector.getMaxExperience());
            }
        }
    }

    public void updateCollectorData(TileEntityCollector updatedCollector) {
        this.tileEntityCollector.setLevel(updatedCollector.getLevel());
        this.tileEntityCollector.setExperience(updatedCollector.getExperience());
        this.tileEntityCollector.setMaxExperience(updatedCollector.getMaxExperience());
    }
}