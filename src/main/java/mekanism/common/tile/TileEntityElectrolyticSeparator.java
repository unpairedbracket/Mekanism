package mekanism.common.tile;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.base.ISustainedData;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.outputs.ChemicalPairOutput;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;

@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class TileEntityElectrolyticSeparator extends TileEntityElectricBlock implements IFluidHandler, IPeripheral, ITubeConnection, ISustainedData, IGasHandler
{
	/** This separator's water slot. */
	public FluidTank fluidTank = new FluidTank(24000);

	/** The maximum amount of gas this block can store. */
	public int MAX_GAS = 2400;

	/** The amount of oxygen this block is storing. */
	public GasTank leftTank = new GasTank(MAX_GAS);

	/** The amount of hydrogen this block is storing. */
	public GasTank rightTank = new GasTank(MAX_GAS);

	/** How fast this block can output gas. */
	public int output = 16;

	/** The type of gas this block is outputting. */
	public boolean dumpLeft = false;

	/** Type type of gas this block is dumping. */
	public boolean dumpRight = false;

	public boolean isActive = false;

	public SeparatorRecipe cachedRecipe;

	public TileEntityElectrolyticSeparator()
	{
		super("ElectrolyticSeparator", MachineType.ELECTROLYTIC_SEPARATOR.baseEnergy);
		inventory = new ItemStack[4];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.discharge(3, this);

			SeparatorRecipe recipe = getRecipe();

			if(inventory[0] != null)
			{
				if(RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(inventory[0]))
				{
					if(inventory[0].getItem() instanceof IFluidContainerItem)
					{
						fluidTank.fill(FluidContainerUtils.extractFluid(fluidTank, inventory[0]), true);
					}
					else {
						FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(inventory[0]);
	
						if(fluid != null && (fluidTank.getFluid() == null || fluid.isFluidEqual(fluidTank.getFluid()) && fluidTank.getFluid().amount+fluid.amount <= fluidTank.getCapacity()))
						{
							fluidTank.fill(fluid, true);
	
							if(inventory[0].getItem().hasContainerItem(inventory[0]))
							{
								inventory[0] = inventory[0].getItem().getContainerItem(inventory[0]);
							}
							else {
								inventory[0].stackSize--;
							}
	
							if(inventory[0].stackSize == 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
			}

			if(inventory[1] != null && leftTank.getStored() > 0)
			{
				leftTank.draw(GasTransmission.addGas(inventory[1], leftTank.getGas()), true);
				MekanismUtils.saveChunk(this);
			}

			if(inventory[2] != null && rightTank.getStored() > 0)
			{
				rightTank.draw(GasTransmission.addGas(inventory[2], rightTank.getGas()), true);
				MekanismUtils.saveChunk(this);
			}

			if(canOperate(recipe) && getEnergy() >= recipe.extraEnergy)
			{
				setActive(true);
				operate(recipe);
				setEnergy(getEnergy() - recipe.extraEnergy);
			}
			else {
				setActive(false);
			}

			if(leftTank.getGas() != null)
			{
				if(!dumpLeft)
				{
					GasStack toSend = new GasStack(leftTank.getGas().getGas(), Math.min(leftTank.getStored(), output));

					TileEntity tileEntity = Coord4D.get(this).offset(MekanismUtils.getLeft(facing)).getTileEntity(worldObj);

					if(tileEntity instanceof IGasHandler)
					{
						if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getLeft(facing).getOpposite(), leftTank.getGas().getGas()))
						{
							leftTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getLeft(facing).getOpposite(), toSend, true), true);
						}
					}
				}
				else {
					leftTank.draw(8, true);

					if(worldObj.rand.nextInt(3) == 2)
					{
						Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getParticlePacket(0, new ArrayList())), new Range4D(Coord4D.get(this)));
					}
				}
			}

			if(rightTank.getGas() != null)
			{
				if(!dumpRight)
				{
					GasStack toSend = new GasStack(rightTank.getGas().getGas(), Math.min(rightTank.getStored(), output));

					TileEntity tileEntity = Coord4D.get(this).offset(MekanismUtils.getRight(facing)).getTileEntity(worldObj);

					if(tileEntity instanceof IGasHandler)
					{
						if(((IGasHandler)tileEntity).canReceiveGas(MekanismUtils.getRight(facing).getOpposite(), rightTank.getGas().getGas()))
						{
							rightTank.draw(((IGasHandler)tileEntity).receiveGas(MekanismUtils.getRight(facing).getOpposite(), toSend, true), true);
						}
					}
				}
				else {
					rightTank.draw(8, true);

					if(worldObj.rand.nextInt(3) == 2)
					{
						Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getParticlePacket(1, new ArrayList())), new Range4D(Coord4D.get(this)));
					}
				}
			}

		}
	}

	public SeparatorRecipe getRecipe()
	{
		FluidInput input = getInput();
		if(cachedRecipe == null || !input.testEquality(cachedRecipe.getInput()))
		{
			cachedRecipe = RecipeHandler.getElectrolyticSeparatorRecipe(getInput());
		}
		return cachedRecipe;
	}

	public FluidInput getInput()
	{
		return new FluidInput(fluidTank.getFluid());
	}

	public boolean canOperate(SeparatorRecipe recipe)
	{
		return recipe != null && recipe.canOperate(fluidTank, leftTank, rightTank);
	}

	public void operate(SeparatorRecipe recipe)
	{
		recipe.operate(fluidTank, leftTank, rightTank);
	}

	public boolean canFill(ChemicalPairOutput gases)
	{
		return (leftTank.canReceive(gases.leftGas.getGas()) && leftTank.getNeeded() >= gases.leftGas.amount
				&& rightTank.canReceive(gases.rightGas.getGas()) && rightTank.getNeeded() >= gases.rightGas.amount);
	}

	public void spawnParticle(int type)
	{
		if(type == 0)
		{
			EnumFacing side = EnumFacing.getFront(facing);

			double x = xCoord + (side.offsetX == 0 ? 0.5 : Math.max(side.offsetX, 0));
			double z = zCoord + (side.offsetZ == 0 ? 0.5 : Math.max(side.offsetZ, 0));

			worldObj.spawnParticle("smoke", x, yCoord + 0.5, z, 0.0D, 0.0D, 0.0D);

		}
		else if(type == 1)
		{
			switch(facing)
			{
				case 3:
					worldObj.spawnParticle("smoke", xCoord+0.9, yCoord+1, zCoord+0.75, 0.0D, 0.0D, 0.0D);
					break;
				case 4:
					worldObj.spawnParticle("smoke", xCoord+0.25, yCoord+1, zCoord+0.9, 0.0D, 0.0D, 0.0D);
					break;
				case 2:
					worldObj.spawnParticle("smoke", xCoord+0.1, yCoord+1, zCoord+0.25, 0.0D, 0.0D, 0.0D);
					break;
				case 5:
					worldObj.spawnParticle("smoke", xCoord+0.75, yCoord+1, zCoord+0.1, 0.0D, 0.0D, 0.0D);
					break;
			}
		}
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 3)
		{
			return ChargeUtils.canBeOutputted(itemstack, false);
		}
		else if(slotID == 0)
		{
			return FluidContainerRegistry.isEmptyContainer(itemstack);
		}
		else if(slotID == 1 || slotID == 2)
		{
			return itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
					((IGasItem)itemstack.getItem()).getGas(itemstack).amount == ((IGasItem)itemstack.getItem()).getMaxGas(itemstack);
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(itemstack);
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IGasItem && (((IGasItem)itemstack.getItem()).getGas(itemstack) == null || ((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("hydrogen"));
		}
		else if(slotID == 2)
		{
			return itemstack.getItem() instanceof IGasItem && (((IGasItem)itemstack.getItem()).getGas(itemstack) == null || ((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("oxygen"));
		}
		else if(slotID == 3)
		{
			return ChargeUtils.canBeDischarged(itemstack);
		}

		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(EnumFacing.getFront(side) == MekanismUtils.getRight(facing))
		{
			return new int[] {3};
		}
		else if(side == facing || EnumFacing.getFront(side) == EnumFacing.getFront(facing).getOpposite())
		{
			return new int[] {1, 2};
		}

		return InventoryUtils.EMPTY;
	}

	/**
	 * Gets the scaled energy level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / BASE_MAX_ENERGY);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			byte type = dataStream.readByte();

			if(type == 0)
			{
				dumpLeft ^= true;
			}
			else if(type == 1)
			{
				dumpRight ^= true;
			}

			return;
		}

		super.handlePacketData(dataStream);

		int type = dataStream.readInt();

		if(type == 0)
		{
			if(dataStream.readBoolean())
			{
				fluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				fluidTank.setFluid(null);
			}

			if(dataStream.readBoolean())
			{
				leftTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				leftTank.setGas(null);
			}

			if(dataStream.readBoolean())
			{
				rightTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				rightTank.setGas(null);
			}

			dumpLeft = dataStream.readBoolean();
			dumpRight = dataStream.readBoolean();
			isActive = dataStream.readBoolean();
		}
		else if(type == 1)
		{
			spawnParticle(dataStream.readInt());
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(0);

		if(fluidTank.getFluid() != null)
		{
			data.add(true);
			data.add(fluidTank.getFluid().getFluid().getID());
			data.add(fluidTank.getFluidAmount());
		}
		else {
			data.add(false);
		}

		if(leftTank.getGas() != null)
		{
			data.add(true);
			data.add(leftTank.getGas().getGas().getID());
			data.add(leftTank.getStored());
		}
		else {
			data.add(false);
		}

		if(rightTank.getGas() != null)
		{
			data.add(true);
			data.add(rightTank.getGas().getGas().getID());
			data.add(rightTank.getStored());
		}
		else {
			data.add(false);
		}

		data.add(dumpLeft);
		data.add(dumpRight);
		data.add(isActive);

		return data;
	}

	public ArrayList getParticlePacket(int type, ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(1);
		data.add(type);
		return data;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		if(nbtTags.hasKey("fluidTank"))
		{
			fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
		}

		leftTank.read(nbtTags.getCompoundTag("leftTank"));
		rightTank.read(nbtTags.getCompoundTag("rightTank"));

		dumpLeft = nbtTags.getBoolean("dumpLeft");
		dumpRight = nbtTags.getBoolean("dumpRight");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		if(fluidTank.getFluid() != null)
		{
			nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
		}

		nbtTags.setTag("leftTank", leftTank.write(new NBTTagCompound()));
		nbtTags.setTag("rightTank", rightTank.write(new NBTTagCompound()));

		nbtTags.setBoolean("dumpLeft", dumpLeft);
		nbtTags.setBoolean("dumpRight", dumpRight);
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String getType()
	{
		return getInventoryName();
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getWater", "getWaterNeeded", "getHydrogen", "getHydrogenNeeded", "getOxygen", "getOxygenNeeded"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {BASE_MAX_ENERGY};
			case 3:
				return new Object[] {(BASE_MAX_ENERGY -electricityStored)};
			case 4:
				return new Object[] {fluidTank.getFluid() != null ? fluidTank.getFluid().amount : 0};
			case 5:
				return new Object[] {fluidTank.getFluid() != null ? (fluidTank.getCapacity()- fluidTank.getFluid().amount) : 0};
			case 6:
				return new Object[] {leftTank.getStored()};
			case 7:
				return new Object[] {leftTank.getNeeded()};
			case 8:
				return new Object[] {rightTank.getStored()};
			case 9:
				return new Object[] {rightTank.getNeeded()};
			default:
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return new Object[] {"Unknown command."};
		}
	}

	@Override
	@Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this == other;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {}

	@Override
	@Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean canTubeConnect(EnumFacing side)
	{
		return side == MekanismUtils.getLeft(facing) || side == MekanismUtils.getRight(facing);
	}

	@Override
	public void writeSustainedData(ItemStack itemStack) 
	{
		if(fluidTank.getFluid() != null)
		{
			itemStack.getTagCompound().setTag("fluidTank", fluidTank.getFluid().writeToNBT(new NBTTagCompound()));
		}
		
		if(leftTank.getGas() != null)
		{
			itemStack.getTagCompound().setTag("leftTank", leftTank.getGas().write(new NBTTagCompound()));
		}
		
		if(rightTank.getGas() != null)
		{
			itemStack.getTagCompound().setTag("rightTank", rightTank.getGas().write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack) 
	{
		fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(itemStack.getTagCompound().getCompoundTag("fluidTank")));
		leftTank.setGas(GasStack.readFromNBT(itemStack.getTagCompound().getCompoundTag("leftTank")));
		rightTank.setGas(GasStack.readFromNBT(itemStack.getTagCompound().getCompoundTag("rightTank")));
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(fluid);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return false;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		if(RecipeHandler.Recipe.ELECTROLYTIC_SEPARATOR.containsRecipe(resource.getFluid()))
		{
			return fluidTank.fill(resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return new FluidTankInfo[] {fluidTank.getInfo()};
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		return 0;
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return leftTank.draw(amount, doTransfer);
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return rightTank.draw(amount, doTransfer);
		}

		return null;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return false;
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		if(side == MekanismUtils.getLeft(facing))
		{
			return leftTank.getGas() != null && leftTank.getGas().getGas() == type;
		}
		else if(side == MekanismUtils.getRight(facing))
		{
			return rightTank.getGas() != null && rightTank.getGas().getGas() == type;
		}

		return false;
	}

	public void setActive(boolean active)
	{
		isActive = active;
	}
}
