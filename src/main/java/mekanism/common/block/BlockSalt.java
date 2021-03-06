package mekanism.common.block;

import java.util.Random;

import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSpriteRegister;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSalt extends Block
{
    public BlockSalt()
    {
        super(Material.sand);
        setCreativeTab(Mekanism.tabMekanism);
        setHardness(0.5F);
        setStepSound(soundTypeSand);
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureAtlasSpriteRegister register)
	{
		blockIcon = register.registerIcon("mekanism:SaltBlock");
	}

    @Override
    public Item getItemDropped(int i, Random random, int j)
    {
        return MekanismItems.Salt;
    }

    @Override
    public int quantityDropped(Random random)
    {
        return 4;
    }
}
