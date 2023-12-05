package fr.warden.ecocollector.container;

import fr.warden.ecocollector.EcoCollectorMod;
import fr.warden.ecocollector.container.slot.SlotCollector;
import fr.warden.ecocollector.network.GuiOpenPacket;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;

public class ContainerCollector extends Container {

    private final TileEntityCollector tileEntityCollector;

    public ContainerCollector(InventoryPlayer inventoryPlayer, TileEntityCollector tileEntityCollector) {
        this.tileEntityCollector = tileEntityCollector;
        this.tileEntityCollector.setContainer(this);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }

        int[] slotXCoordinates = {26, 44, 62, 80, 98};
        int slotYCoordinate = 58;
        int[] unlockLevels = {0, 1, 2, 3, 4};

        for (int i = 0; i < 5; ++i) {
            SlotCollector slot = new SlotCollector(tileEntityCollector, i, slotXCoordinates[i], slotYCoordinate, unlockLevels[i], tileEntityCollector);
            this.addSlotToContainer(slot);
            if (tileEntityCollector.getLevel() < unlockLevels[i]) {
                slot.putStack(createBarrierItem(unlockLevels[i]));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tileEntityCollector != null;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            
            if (index < tileEntityCollector.getSizeInventory()) {
                if (!this.mergeItemStack(itemstack1, tileEntityCollector.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, tileEntityCollector.getSizeInventory(), false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote && this.tileEntityCollector != null) {
            EcoCollectorMod.network.sendToServer(new GuiOpenPacket(tileEntityCollector.xCoord, tileEntityCollector.yCoord, tileEntityCollector.zCoord, false));
        }
    }

    private ItemStack createBarrierItem(int unlockLevel) {
        ItemStack barrier = new ItemStack(Items.string);

        NBTTagCompound displayTag = new NBTTagCompound();
        displayTag.setString("Name", "§6Slot verrouillé");

        List<String> lore = new ArrayList<String>();
        lore.add("§7Débloqué au niveau " + unlockLevel);

        NBTTagList loreList = new NBTTagList();
        for (String line : lore) {
            loreList.appendTag(new NBTTagString(line));
        }

        displayTag.setTag("Lore", loreList);
        barrier.setTagCompound(new NBTTagCompound());
        barrier.getTagCompound().setTag("display", displayTag);

        return barrier;
    }

    public void updateSlots() {
        for (Object inventorySlot : this.inventorySlots) {
            Slot slot = (Slot) inventorySlot;

            if (slot instanceof SlotCollector) {
                SlotCollector slotCollector = (SlotCollector) slot;
                int unlockLevel = slotCollector.getUnlockLevel();

                if (unlockLevel > tileEntityCollector.getLevel()) {
                    if (!slot.getHasStack() || slot.getStack().getItem() != Items.string) {
                        slot.putStack(createBarrierItem(unlockLevel));
                    }
                } else {
                    if (slot.getHasStack() && slot.getStack().getItem() == Items.string) {
                        slot.putStack(null);
                    }
                }
            }
        }
    }
}
