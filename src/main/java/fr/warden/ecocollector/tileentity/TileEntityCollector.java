package fr.warden.ecocollector.tileentity;

import fr.warden.ecocollector.EcoCollectorMod;
import fr.warden.ecocollector.container.ContainerCollector;
import fr.warden.ecocollector.network.CollectorSyncPacket;
import fr.warden.ecocollector.sync.CollectorSyncManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityCollector extends TileEntity implements IInventory {
    private ContainerCollector container;
    private final CollectorSyncManager syncManager;
    private final ItemStack[] inventory = new ItemStack[5];
    private boolean isGuiOpen = false;
    private int level;
    private int experience;
    private int maxExperience;
    private boolean firstTick = true;

    public TileEntityCollector() {
        this.syncManager = new CollectorSyncManager(this);
        this.level = 0;
        this.experience = 0;
        this.maxExperience = 50;
    }

    public TileEntityCollector(ContainerCollector container) {
        this.container = container;
        this.syncManager = new CollectorSyncManager(this);
        this.level = 0;
        this.experience = 0;
        this.maxExperience = 50;
    }

    public ContainerCollector getContainer() {
        return this.container;
    }

    public void setContainer(ContainerCollector container) {
        this.container = container;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void setMaxExperience(int maxExperience) {
        this.maxExperience = maxExperience;
    }

    public int getMaxExperience() {
        return maxExperience;
    }

    public boolean isGuiOpen() {
        return isGuiOpen;
    }

    public void setGuiOpen(boolean isGuiOpen) {
        this.isGuiOpen = isGuiOpen;
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        if (inventory[slot] != null) {
            ItemStack itemstack;

            if (inventory[slot].stackSize <= count) {
                itemstack = inventory[slot];
                inventory[slot] = null;
            } else {
                itemstack = inventory[slot].splitStack(count);

                if (inventory[slot].stackSize == 0) {
                    inventory[slot] = null;
                }

            }

            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (inventory[slot] != null) {
            ItemStack itemstack = inventory[slot];
            inventory[slot] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;

        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return "container.collector";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this &&
                player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {
        if (this.container != null) {
            this.container.updateSlots();
        }

        sendSyncPacket();
    }

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!this.worldObj.isRemote && this.firstTick) {
            sendSyncPacket();
            this.firstTick = false;
        }

        collectItems(this.worldObj);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        NBTTagList itemList = compound.getTagList("Inventory", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound slotTag = itemList.getCompoundTagAt(i);
            int slot = slotTag.getInteger("Slot");
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(slotTag);
            }
        }

        level = compound.getInteger("Level");
        experience = compound.getInteger("Experience");

        if (this.container != null) {
            this.container.updateSlots();
        } else {
            this.container = new ContainerCollector(null, this);
            this.container.updateSlots();
        }

        if (this.worldObj != null && !this.worldObj.isRemote) {
            sendSyncPacket();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound slotTag = new NBTTagCompound();
                slotTag.setInteger("Slot", i);
                inventory[i].writeToNBT(slotTag);
                itemList.appendTag(slotTag);
            }
        }
        compound.setTag("Inventory", itemList);

        compound.setInteger("Level", level);
        compound.setInteger("Experience", experience);
    }

    public void addExperience(int exp, EntityPlayerMP player) {
        this.syncManager.addExperience(exp, player);
        this.markDirty();
    }

    public void sendSyncPacket() {
        if (!this.worldObj.isRemote) {
            double range = 64.0;
            AxisAlignedBB detectionBox = AxisAlignedBB.getBoundingBox(
                    xCoord - range, yCoord - range, zCoord - range,
                    xCoord + range, yCoord + range, zCoord + range);
            List<?> players = worldObj.getEntitiesWithinAABB(EntityPlayerMP.class, detectionBox);

            for (Object obj : players) {
                if (obj instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) obj;
                    CollectorSyncPacket packet = new CollectorSyncPacket(this);
                    EcoCollectorMod.network.sendTo(packet, player);
                }
            }
        }
    }

    public void collectItems(World world) {
        TileEntity tileEntity = world.getTileEntity(xCoord, yCoord - 1, zCoord);
        if (!(tileEntity instanceof TileEntityChest)) {
            return;
        }
        TileEntityChest chest = (TileEntityChest) tileEntity;

        int range = 5 * this.level;
        AxisAlignedBB collectBox = AxisAlignedBB.getBoundingBox(
                xCoord - range, yCoord - range, zCoord - range,
                xCoord + range, yCoord + range, zCoord + range);

        List<?> items = world.getEntitiesWithinAABB(EntityItem.class, collectBox);
        for (Object obj : items) {
            if (obj instanceof EntityItem) {
                EntityItem itemEntity = (EntityItem) obj;
                ItemStack itemStack = itemEntity.getEntityItem().copy();

                if (isItemFiltered(itemStack) && addItemToChest(chest, itemStack)) {
                    itemEntity.setDead();
                }
            }
        }
    }

    private boolean isItemFiltered(ItemStack itemStack) {
        for (ItemStack filterStack : this.inventory) {
            if (filterStack != null && filterStack.getItem() != Items.string && filterStack.isItemEqual(itemStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean addItemToChest(TileEntityChest chest, ItemStack itemStack) {
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            ItemStack stackInSlot = chest.getStackInSlot(i);

            if (stackInSlot == null) {
                chest.setInventorySlotContents(i, itemStack);
                return true;
            } else if (itemStack.isItemEqual(stackInSlot) && ItemStack.areItemStackTagsEqual(stackInSlot, itemStack)) {
                int spaceLeft = chest.getInventoryStackLimit() - stackInSlot.stackSize;
                if (spaceLeft > 0) {
                    int transferAmount = Math.min(spaceLeft, itemStack.stackSize);
                    stackInSlot.stackSize += transferAmount;
                    itemStack.stackSize -= transferAmount;

                    if (itemStack.stackSize == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int calculateMaxExperience(int level) {
        return 50 * (int) Math.pow(2, level - 1);
    }

    public void debugInfo() {
        System.out.println("Debug Information:");
        System.out.println("Level: " + this.level);
        System.out.println("Experience: " + this.experience);
        System.out.println("Max Experience: " + this.maxExperience);
    }
}
