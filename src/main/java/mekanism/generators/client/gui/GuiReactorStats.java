package mekanism.generators.client.gui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.util.ListUtils;
import mekanism.client.gui.GuiEnergyInfo;
import mekanism.client.gui.GuiEnergyInfo.IInfoHandler;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.sound.SoundHandler;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.PacketGeneratorsGui.GeneratorsGuiMessage;
import mekanism.generators.common.tile.reactor.TileEntityReactorController;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiReactorStats extends GuiMekanism
{
	public TileEntityReactorController tileEntity;
	public static NumberFormat nf = NumberFormat.getIntegerInstance();


	public GuiReactorStats(InventoryPlayer inventory, final TileEntityReactorController tentity)
	{
		super(new ContainerNull(inventory.player, tentity));
		tileEntity = tentity;
		guiElements.add(new GuiEnergyInfo(new IInfoHandler()
		{
			@Override
			public List<String> getInfo()
			{
				return tileEntity.isFormed() ? ListUtils.asList(
						"Storing: " + MekanismUtils.getEnergyDisplay(tileEntity.getEnergy()),
						"Producing: " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, true)) + "/t") : new ArrayList();
			}
		}, this, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
		guiElements.add(new GuiHeatTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
		guiElements.add(new GuiFuelTab(this, tileEntity, MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png")));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		fontRendererObj.drawString(tileEntity.getInventoryName(), 46, 6, 0x404040);
		if(tileEntity.isFormed())
		{
			fontRendererObj.drawString(EnumColor.DARK_GREEN + MekanismUtils.localize("gui.passive"), 6, 26, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.minInject") + ": " + (tileEntity.getReactor().getMinInjectionRate(false)), 16, 36, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.ignition") + ": " + (nf.format(tileEntity.getReactor().getIgnitionTemperature(false))), 16, 46, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.maxPlasma") + ": " + (nf.format(tileEntity.getReactor().getMaxPlasmaTemperature(false))), 16, 56, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.maxCasing") + ": " + (nf.format(tileEntity.getReactor().getMaxCasingTemperature(false))), 16, 66, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.passiveGeneration") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(false, false))+"/t", 16, 76, 0x404040);

			fontRendererObj.drawString(EnumColor.DARK_BLUE + MekanismUtils.localize("gui.active"), 6, 92, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.minInject") + ": " + (tileEntity.getReactor().getMinInjectionRate(true)), 16, 102, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.ignition") + ": " + (nf.format(tileEntity.getReactor().getIgnitionTemperature(true))), 16, 112, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.maxPlasma") + ": " + (nf.format(tileEntity.getReactor().getMaxPlasmaTemperature(true))), 16, 122, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.maxCasing") + ": " + (nf.format(tileEntity.getReactor().getMaxCasingTemperature(true))), 16, 132, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.passiveGeneration") + ": " + MekanismUtils.getEnergyDisplay(tileEntity.getReactor().getPassiveGeneration(true, false))+"/t", 16, 142, 0x404040);
			fontRendererObj.drawString(MekanismUtils.localize("gui.steamProduction") + ": " + nf.format(tileEntity.getReactor().getSteamPerTick(false)) + "mB/t", 16, 152, 0x404040);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mouseX, int mouseY)
	{
		mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiTall.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int guiWidth = (width - xSize) / 2;
		int guiHeight = (height - ySize) / 2;
		drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
		{
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 0, 14, 14);
		}
		else {
			drawTexturedModalRect(guiWidth + 6, guiHeight + 6, 176, 14, 14, 14);
		}

		super.drawGuiContainerBackgroundLayer(partialTick, mouseX, mouseY);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);

		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);

		if(button == 0)
		{
			if(xAxis >= 6 && xAxis <= 20 && yAxis >= 6 && yAxis <= 20)
			{
				SoundHandler.playSound("gui.button.press");
				MekanismGenerators.packetHandler.sendToServer(new GeneratorsGuiMessage(Coord4D.get(tileEntity), 10));
			}

		}
	}}
