package fr.warden.ecocollector.sync;

import fr.warden.ecocollector.container.ContainerCollector;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.util.Timer;
import java.util.TimerTask;

public class CollectorSyncManager {
    private final TileEntityCollector collector;
    private int transactionCount = 0;
    private static final long SYNC_DELAY = 5000;
    private static final int MAX_TRANSACTIONS = 25;
    private final Timer syncTimer;
    private TimerTask syncTask;

    public CollectorSyncManager(TileEntityCollector collector) {
        this.collector = collector;
        this.syncTimer = new Timer();
    }

    public void addExperience(int exp, EntityPlayerMP player) {
        collector.setExperience(collector.getExperience() + exp);
        sendExperienceAddedMessage(exp, player);
        checkForLevelUp(player);
        transactionCount++;
        checkSyncConditions();
    }

    private void sendExperienceAddedMessage(int exp, EntityPlayerMP player) {
        int expToNextLevel = collector.getMaxExperience() - collector.getExperience();
        String message = String.format("Vous avez ajouté %d niveaux d'expérience au collecteur. %d niveaux d'expérience requis pour le prochain niveau.", exp, expToNextLevel);
        player.addChatMessage(new ChatComponentText(message));
    }

    private void checkForLevelUp(EntityPlayerMP player) {
        if (collector.getExperience() >= collector.getMaxExperience() && collector.getLevel() < 5) {
            collector.setLevel(collector.getLevel() + 1);
            collector.setExperience(0);
            collector.setMaxExperience(collector.calculateMaxExperience(collector.getLevel()));
            String levelUpMessage = String.format("Votre collecteur a été mis à niveau au niveau %d. L'expérience actuelle est réinitialisée à 0.", collector.getLevel());
            player.addChatMessage(new ChatComponentText(levelUpMessage));

            ContainerCollector container = collector.getContainer();
            if (container != null) container.updateSlots();
        }
    }

    private void checkSyncConditions() {
        transactionCount++;
        if (transactionCount >= MAX_TRANSACTIONS) {
            sendSyncPacket();
            resetSyncConditions();
        } else {
            scheduleSyncCheck();
        }
    }

    private void resetSyncConditions() {
        transactionCount = 0;
        cancelSyncTask();
    }

    private void scheduleSyncCheck() {
        cancelSyncTask();
        syncTask = new TimerTask() {
            @Override
            public void run() {
                if (transactionCount > 0) {
                    sendSyncPacket();
                }
                resetSyncConditions();
            }
        };
        syncTimer.schedule(syncTask, SYNC_DELAY);
    }

    private void cancelSyncTask() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }

    private void sendSyncPacket() {
        this.collector.sendSyncPacket();
    }
}