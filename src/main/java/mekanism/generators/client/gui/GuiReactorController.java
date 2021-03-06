package mekanism.generators.client.gui;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.util.ListUtils;
import mekanism.client.gui.GuiEnergyInfo;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiSlot;
import mekanism.client.gui.GuiSlot.SlotType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.inventory.container.ContainerReactorController;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorController extends GuiMekanism
{
	public TileEntityReactorController tileEntity;

	public GuiReactorController(InventoryPlayer inventory, final TileEntityReactorController tentity)
	{
		super(new ContainerReactorController(inventory, tentity));
		tileEntity = tentity;
		if(tileEntity.isFormed())
		{
			guiElements.add(new GuiEnergyInfo(new IInfoHandler()
			{
				@Override
				public List<String> getInfo()
				{
					return tileEntity.isFormed() ? ListUtils.asList(
							"Storing: " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()),
							"Producing: " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t") : new ArrayList();
				}
			}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
			guiElements.add(new GuiSlot(SlotType.NORMAL, this, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"), 79, 38));
			guiElements.add(new GuiHeatTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
			guiElements.add(new GuiFuelTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
			guiElements.add(new GuiStatTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png")));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 46, 6, 0x404040);
		
		if(tileEntity.getActive())
		{
			fontRendererObj.drawString(MekanismUtils.localize("container.reactor.formed"), 8, 16, 0x404040);
		}
		else {
			fontRendererObj.drawString(MekanismUtils.localize("container.reactor.notFormed"), 8, 16, 0x404040);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiBlank.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}
}
