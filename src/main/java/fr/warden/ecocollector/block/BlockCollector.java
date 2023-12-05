package fr.warden.ecocollector.block;

import fr.warden.ecocollector.EcoCollectorMod;
import fr.warden.ecocollector.network.GuiOpenPacket;
import fr.warden.ecocollector.tileentity.TileEntityCollector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class BlockCollector extends Block {

    private final IIcon[] icons = new IIcon[6];

    public BlockCollector(Material material, String name) {
        super(material);
        setBlockName(name);
        setBlockTextureName(EcoCollectorMod.MOD_ID + ":" + name);
        setCreativeTab(CreativeTabs.tabRedstone);

        setHardness(3.0f);
        setHarvestLevel("pickaxe", 2);
        setLightLevel(0.5F);
        setResistance(30.0f);
        setStepSound(soundTypeMetal);
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        this.icons[0] = reg.registerIcon("ecocollector:blockcollector/blockcollector_bottom");
        this.icons[1] = reg.registerIcon("ecocollector:blockcollector/blockcollector_top");
        this.icons[2] = reg.registerIcon("ecocollector:blockcollector/blockcollector_front");
        this.icons[3] = reg.registerIcon("ecocollector:blockcollector/blockcollector_side");
        this.icons[4] = reg.registerIcon("ecocollector:blockcollector/blockcollector_side");
        this.icons[5] = reg.registerIcon("ecocollector:blockcollector/blockcollector_side");
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int[] directionMapping = {3, 4, 2, 5};
        int direction = directionMapping[MathHelper.floor_double((placer.rotationYaw + 180.0F) / 90.0F + 0.5D) & 3];

        world.setBlockMetadataWithNotify(x, y, z, direction, 2);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return side == 0 ? this.icons[0] :
                side == 1 ? this.icons[1] :
                        side == meta ? this.icons[2] :
                                this.icons[3];
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            final TileEntity tileEntity = world.getTileEntity(x, y - 1, z);
            if (tileEntity instanceof TileEntityChest) {
                if (player.isSneaking()) {
                    player.displayGUIChest((TileEntityChest) tileEntity);
                } else {
                    EcoCollectorMod.network.sendToServer(new GuiOpenPacket(x, y, z, true));
                    player.openGui(EcoCollectorMod.MOD_ID, EcoCollectorMod.COLLECTOR_GUI_ID, world, x, y, z);
                }
            } else {
                player.addChatMessage(new ChatComponentText("Aucun coffre n'est en dessous du bloc."));
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntityCollector tileEntity = (TileEntityCollector) world.getTileEntity(x, y, z);
        if (tileEntity != null) {
            tileEntity.setGuiOpen(false);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityCollector();
    }
}