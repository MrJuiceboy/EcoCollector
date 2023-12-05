package fr.warden.ecocollector.container.slot;

import fr.warden.ecocollector.tileentity.TileEntityCollector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotCollector extends Slot {
    private final int unlockLevel;
    private final TileEntityCollector collector;

    public SlotCollector(IInventory inventoryIn, int index, int xPosition, int yPosition, int unlockLevel, TileEntityCollector collector) {
        super(inventoryIn, index, xPosition, yPosition);
        this.unlockLevel = unlockLevel;
        this.collector = collector;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if(collector.getLevel() < unlockLevel) {
            return stack.getItem() == Items.string;
        }
        return true;
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        return collector.getLevel() >= unlockLevel;
    }

    public int getUnlockLevel() {
        return this.unlockLevel;
    }
}
