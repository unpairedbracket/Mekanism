package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.vec.Vector3;

public class PartPressurizedTube extends PartTransmitter<GasNetwork> implements IGasHandler
{
	public static TransmitterIcons tubeIcons = new TransmitterIcons(1, 2);

	public float currentScale;

	public GasStack cacheGas;
	public GasStack lastWrite;

	@Override
	public void update()
	{
		if(!world().isRemote)
		{
			if(cacheGas != null)
			{
				if(getTransmitterNetwork().gasStored == null)
				{
					getTransmitterNetwork().gasStored = cacheGas;
				}
				else {
					getTransmitterNetwork().gasStored.amount += cacheGas.amount;
				}

				cacheGas = null;
			}

			if(getTransmitterNetwork(false) != null && getTransmitterNetwork(false).getSize() > 0)
			{
				int last = lastWrite != null ? lastWrite.amount : 0;

				if(last != getSaveShare())
				{
					MekanismUtils.saveChunk(tile());
				}
			}

			IGasHandler[] connectedAcceptors = GasTransmission.getConnectedAcceptors(tile());

			for(EnumFacing side : getConnections(ConnectionType.PULL))
			{
				if(connectedAcceptors[side.ordinal()] != null)
				{
					IGasHandler container = connectedAcceptors[side.ordinal()];

					if(container != null)
					{
						GasStack received = container.drawGas(side.getOpposite(), 100, false);

						if(received != null && received.amount != 0)
						{
							container.drawGas(side.getOpposite(), getTransmitterNetwork().emit(received, true), true);
						}
					}
				}
			}

		}
		else {
			float targetScale = getTransmitterNetwork().gasScale;

			if(Math.abs(currentScale - targetScale) > 0.01)
			{
				currentScale = (9 * currentScale + targetScale) / 10;
			}
		}

		super.update();
	}

	private int getSaveShare()
	{
		if(getTransmitterNetwork().gasStored != null)
		{
			int remain = getTransmitterNetwork().gasStored.amount%getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitterNetwork().gasStored.amount/getTransmitterNetwork().transmitters.size();

			if(getTransmitterNetwork().isFirst((IGridTransmitter<GasNetwork>)tile()))
			{
				toSave += remain;
			}

			return toSave;
		}

		return 0;
	}

	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.PRESSURIZED_TUBE;
	}

	@Override
	public void preSingleMerge(GasNetwork network)
	{
		if(cacheGas != null)
		{
			if(network.gasStored == null)
			{
				network.gasStored = cacheGas;
			}
			else {
				network.gasStored.amount += cacheGas.amount;
			}

			cacheGas = null;
		}
	}

	@Override
	public void onChunkUnload()
	{
		if(!world().isRemote)
		{
			if(lastWrite != null)
			{
				if(getTransmitterNetwork().gasStored != null)
				{
					getTransmitterNetwork().gasStored.amount -= lastWrite.amount;

					if(getTransmitterNetwork().gasStored.amount <= 0)
					{
						getTransmitterNetwork().gasStored = null;
					}
				}
			}
		}

		super.onChunkUnload();
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		if(nbtTags.hasKey("cacheGas"))
		{
			cacheGas = GasStack.readFromNBT(nbtTags.getCompoundTag("cacheGas"));
		}
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		if(getTransmitterNetwork(false) != null && getTransmitterNetwork(false).getSize() > 0 && getTransmitterNetwork(false).gasStored != null)
		{
			int remain = getTransmitterNetwork().gasStored.amount%getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitterNetwork().gasStored.amount/getTransmitterNetwork().transmitters.size();

			if(getTransmitterNetwork().isFirst((IGridTransmitter<GasNetwork>)tile()))
			{
				toSave += remain;
			}

			if(toSave > 0)
			{
				GasStack stack = new GasStack(getTransmitterNetwork().gasStored.getGas(), toSave);

				lastWrite = stack;
				nbtTags.setTag("cacheGas", stack.write(new NBTTagCompound()));
			}
		}
	}

	@Override
	public String getType()
	{
		return "mekanism:pressurized_tube";
	}

	public static void registerIcons(IIconRegister register)
	{
		tubeIcons.registerCenterIcons(register, new String[] {"PressurizedTube"});
		tubeIcons.registerSideIcons(register, new String[] {"SmallTransmitterVertical", "SmallTransmitterHorizontal"});
	}

	@Override
	public IIcon getCenterIcon()
	{
		return tubeIcons.getCenterIcon(0);
	}

	@Override
	public IIcon getSideIcon()
	{
		return tubeIcons.getSideIcon(0);
	}

	@Override
	public IIcon getSideIconRotated()
	{
		return tubeIcons.getSideIcon(1);
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, EnumFacing side)
	{
		return GasTransmission.canConnect(tile, side);
	}

	@Override
	public GasNetwork createNetworkFromSingleTransmitter(IGridTransmitter<GasNetwork> transmitter)
	{
		return new GasNetwork(transmitter);
	}

	@Override
	public GasNetwork createNetworkByMergingSet(Set<GasNetwork> networks)
	{
		return new GasNetwork(networks);
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeededInfo();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlowInfo();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		if(pass == 0)
		{
			RenderPartTransmitter.getInstance().renderContents(this, pos);
		}
	}

	@Override
	public int getCapacity()
	{
		return 256;
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		if(getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL)
		{
			return getTransmitterNetwork().emit(stack, doTransfer);
		}
		
		return 0;
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return getConnectionType(side) == ConnectionType.NORMAL || getConnectionType(side) == ConnectionType.PULL;
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return false;
	}
}
