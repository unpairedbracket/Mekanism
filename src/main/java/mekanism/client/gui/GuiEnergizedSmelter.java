package mekanism.client.gui;

import mekanism.client.gui.GuiProgress.ProgressBar;
import mekanism.common.tile.TileEntityElectricMachine;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnergizedSmelter extends GuiElectricMachine
{
	public GuiEnergizedSmelter(InventoryPlayer inventory, TileEntityElectricMachine tentity)
	{
		super(inventory, tentity);
	}
	
	@Override
	public ProgressBar getProgressType()
	{
		return ProgressBar.GREEN;
	}
}
