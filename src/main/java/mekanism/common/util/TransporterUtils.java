package mekanism.common.util;

import java.util.Arrays;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.util.ListUtils;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.TileEntityLogisticalSorter;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public final class TransporterUtils
{
	public static List<EnumColor> colors = ListUtils.asList(EnumColor.DARK_BLUE, EnumColor.DARK_GREEN, EnumColor.DARK_AQUA, EnumColor.DARK_RED, EnumColor.PURPLE,
			EnumColor.INDIGO, EnumColor.BRIGHT_GREEN, EnumColor.AQUA, EnumColor.RED, EnumColor.PINK, EnumColor.YELLOW, EnumColor.BLACK);

	/**
	 * Gets all the transporters around a tile entity.
	 * @param tileEntity - center tile entity
	 * @return array of TileEntities
	 */
	public static TileEntity[] getConnectedTransporters(ILogisticalTransporter tileEntity)
	{
		TileEntity[] transporters = new TileEntity[] {null, null, null, null, null, null};

		for(EnumFacing orientation : EnumFacing.values())
		{
			TileEntity tile = Coord4D.get(tileEntity.getTile()).offset(orientation).getTileEntity(tileEntity.getTile().getWorld());

			if(tile instanceof ILogisticalTransporter)
			{
				ILogisticalTransporter transporter = (ILogisticalTransporter)tile;

				if(transporter.getColor() == null || tileEntity.getColor() == null || transporter.getColor() == tileEntity.getColor())
				{
					transporters[orientation.ordinal()] = transporter.getTile();
				}
			}
		}

		return transporters;
	}

	/**
	 * Gets all the adjacent connections to a TileEntity.
	 * @param tileEntity - center TileEntity
	 * @return boolean[] of adjacent connections
	 */
	public static boolean[] getConnections(ILogisticalTransporter tileEntity)
	{
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};

		TileEntity[] connectedTransporters = getConnectedTransporters(tileEntity);
		IInventory[] connectedInventories = getConnectedInventories(tileEntity);

		for(IInventory inventory : connectedInventories)
		{
			if(inventory != null)
			{
				int side = Arrays.asList(connectedInventories).indexOf(inventory);

				if(!tileEntity.canConnect(EnumFacing.getFront(side)))
				{
					continue;
				}

				EnumFacing forgeSide = EnumFacing.getFront(side).getOpposite();

				if(inventory.getSizeInventory() > 0)
				{
					if(inventory instanceof ISidedInventory)
					{
						ISidedInventory sidedInventory = (ISidedInventory)inventory;

						if(sidedInventory.getAccessibleSlotsFromSide(forgeSide.ordinal()) != null)
						{
							if(sidedInventory.getAccessibleSlotsFromSide(forgeSide.ordinal()).length > 0)
							{
								connectable[side] = true;
							}
						}
					}
					else {
						connectable[side] = true;
					}
				}
			}
		}

		for(TileEntity tile : connectedTransporters)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedTransporters).indexOf(tile);

				if(tileEntity.canConnectMutual(EnumFacing.getFront(side)))
				{
					connectable[side] = true;
				}
			}
		}

		return connectable;
	}

	/**
	 * Gets all the inventories around a tile entity.
	 * @param tileEntity - center tile entity
	 * @return array of IInventories
	 */
	public static IInventory[] getConnectedInventories(ILogisticalTransporter tileEntity)
	{
		IInventory[] inventories = new IInventory[] {null, null, null, null, null, null};

		for(EnumFacing orientation : EnumFacing.values())
		{
			TileEntity inventory = Coord4D.get(tileEntity.getTile()).offset(orientation).getTileEntity(tileEntity.getTile().getWorld());

			if(inventory instanceof IInventory && !(inventory instanceof IGridTransmitter))
			{
				inventories[orientation.ordinal()] = (IInventory)inventory;
			}
		}

		return inventories;
	}

	public static ItemStack insert(TileEntity outputter, ILogisticalTransporter tileEntity, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		return tileEntity.insert(Coord4D.get(outputter), itemStack.copy(), color, doEmit, min);
	}

	public static ItemStack insertRR(TileEntityLogisticalSorter outputter, ILogisticalTransporter tileEntity, ItemStack itemStack, EnumColor color, boolean doEmit, int min)
	{
		return tileEntity.insertRR(outputter, itemStack.copy(), color, doEmit, min);
	}

	public static EnumColor increment(EnumColor color)
	{
		if(color == null)
		{
			return colors.get(0);
		}
		else if(colors.indexOf(color) == colors.size()-1)
		{
			return null;
		}

		return colors.get(colors.indexOf(color)+1);
	}

	public static EnumColor decrement(EnumColor color)
	{
		if(color == null)
		{
			return colors.get(colors.size()-1);
		}
		else if(colors.indexOf(color) == 0)
		{
			return null;
		}

		return colors.get(colors.indexOf(color)-1);
	}

	public static void drop(ILogisticalTransporter tileEntity, TransporterStack stack)
	{
		float[] pos = null;

		if(stack.pathToTarget != null)
		{
			pos = TransporterUtils.getStackPosition(tileEntity, stack, 0);
		}
		else {
			pos = new float[] {0, 0, 0};
		}

		TransporterManager.remove(stack);

		EntityItem entityItem = new EntityItem(tileEntity.getTile().getWorld(), tileEntity.getTile().getPos().getX() + pos[0], tileEntity.getTile().getPos().getY() + pos[1], tileEntity.getTile().getPos().getZ() + pos[2], stack.itemStack);

		entityItem.motionX = 0;
		entityItem.motionY = 0;
		entityItem.motionZ = 0;

		tileEntity.getTile().getWorld().spawnEntityInWorld(entityItem);
	}

	public static float[] getStackPosition(ILogisticalTransporter tileEntity, TransporterStack stack, float partial)
	{
		Coord4D offset = new Coord4D(0, 0, 0, tileEntity.getTile().getWorld().provider.dimensionId).step(EnumFacing.getFront(stack.getSide(tileEntity)));
		float progress = (((float)stack.progress + partial) / 100F) - 0.5F;

		float itemFix = 0;

		if(!(stack.itemStack.getItem() instanceof ItemBlock))
		{
			itemFix = 0.1F;
		}

		return new float[] {0.5F + offset.getPos().getX()*progress, 0.5F + offset.getPos().getY()*progress - itemFix, 0.5F + offset.getPos().getZ()*progress};
	}

	public static void incrementColor(ILogisticalTransporter tileEntity)
	{
		if(tileEntity.getColor() == null)
		{
			tileEntity.setColor(colors.get(0));
			return;
		}
		else if(colors.indexOf(tileEntity.getColor()) == colors.size()-1)
		{
			tileEntity.setColor(null);
			return;
		}

		int index = colors.indexOf(tileEntity.getColor());
		tileEntity.setColor(colors.get(index+1));
	}
}