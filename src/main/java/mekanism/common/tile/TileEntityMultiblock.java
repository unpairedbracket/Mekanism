package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.netty.buffer.ByteBuf;

public abstract class TileEntityMultiblock<T> extends TileEntityContainerBlock implements IMultiblock<T>
{
	/** The tank data for this structure. */
	public T structure;
	
	/** Whether or not to send this tank's structure in the next update packet. */
	public boolean sendStructure;

	/** This tank's previous "has structure" state. */
	public boolean prevStructure;

	/** Whether or not this tank has it's structure, for the client side mechanics. */
	public boolean clientHasStructure;
	
	/** Whether or not this tank segment is rendering the structure. */
	public boolean isRendering;
	
	public TileEntityMultiblock(String name)
	{
		super(name);
	}
	
	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
		{
			if(structure == null)
			{
				structure = getNewStructure();
			}

			if(structure != null && clientHasStructure && isRendering)
			{
				if(!prevStructure)
				{
					Mekanism.proxy.doAnimation(this);
				}
			}

			prevStructure = clientHasStructure;
		}

		if(playersUsing.size() > 0 && ((worldObj.isRemote && !clientHasStructure) || (!worldObj.isRemote && structure == null)))
		{
			for(EntityPlayer player : playersUsing)
			{
				player.closeScreen();
			}
		}

		if(!worldObj.isRemote)
		{
			if(structure == null)
			{
				isRendering = false;
			}

			if(structure == null && ticker == 5)
			{
				update();
			}

			if(prevStructure != (structure != null))
			{
				if(structure != null && !getSynchronizedData().hasRenderer)
				{
					getSynchronizedData().hasRenderer = true;
					isRendering = true;
					sendStructure = true;
				}

				for(EnumFacing side : EnumFacing.values())
				{
					Coord4D obj = Coord4D.get(this).offset(side);

					if(!obj.isAirBlock(worldObj) && (obj.getTileEntity(worldObj) == null || obj.getTileEntity(worldObj).getClass() != getClass()))
					{
						obj.getBlock(worldObj).onNeighborChange(worldObj, obj.getPos().getX(), obj.getPos().getY(), obj.getPos().getZ(), xCoord, yCoord, zCoord);
					}
				}

				Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			}

			prevStructure = structure != null;

			if(structure != null)
			{
				getSynchronizedData().didTick = false;

				if(getSynchronizedData().inventoryID != -1)
				{
					getManager().updateCache(this);
				}
			}
		}
	}
	
	public void update()
	{
		if(!worldObj.isRemote && (structure == null || !getSynchronizedData().didTick))
		{
			getProtocol().doUpdate();

			if(structure != null)
			{
				getSynchronizedData().didTick = true;
			}
		}
	}
	
	public void sendPacketToRenderer()
	{
		if(structure != null)
		{
			for(Coord4D obj : getSynchronizedData().locations)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)obj.getTileEntity(worldObj);

				if(tileEntity != null && tileEntity.isRendering)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tileEntity)));
				}
			}
		}
	}
	
	protected abstract T getNewStructure();
	
	protected abstract UpdateProtocol<T> getProtocol();
	
	public abstract MultiblockManager<T> getManager();
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(isRendering);
		data.add(structure != null);

		if(structure != null && isRendering)
		{
			if(sendStructure)
			{
				sendStructure = false;

				data.add(true);

				data.add(getSynchronizedData().volHeight);
				data.add(getSynchronizedData().volWidth);
				data.add(getSynchronizedData().volLength);

				getSynchronizedData().renderLocation.write(data);
			}
			else {
				data.add(false);
			}
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(structure == null)
		{
			structure = getNewStructure();
		}

		isRendering = dataStream.readBoolean();
		clientHasStructure = dataStream.readBoolean();

		if(clientHasStructure && isRendering)
		{
			if(dataStream.readBoolean())
			{
				getSynchronizedData().volHeight = dataStream.readInt();
				getSynchronizedData().volWidth = dataStream.readInt();
				getSynchronizedData().volLength = dataStream.readInt();

				getSynchronizedData().renderLocation = Coord4D.read(dataStream);
			}
		}
	}
	
	@Override
	public ItemStack getStackInSlot(int slotID)
	{
		return structure != null && getSynchronizedData().getInventory() != null ? getSynchronizedData().getInventory()[slotID] : null;
	}

	@Override
	public void setInventorySlotContents(int slotID, ItemStack itemstack)
	{
		if(structure != null && getSynchronizedData().getInventory() != null)
		{
			getSynchronizedData().getInventory()[slotID] = itemstack;

			if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
			{
				itemstack.stackSize = getInventoryStackLimit();
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
	
	@Override
	public boolean handleInventory()
	{
		return false;
	}

	@Override
	public SynchronizedData<T> getSynchronizedData() 
	{
		return (SynchronizedData<T>)structure;
	}
}
