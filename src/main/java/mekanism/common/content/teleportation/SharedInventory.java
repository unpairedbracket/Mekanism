package mekanism.common.content.teleportation;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class SharedInventory implements IStrictEnergyStorage, IFluidHandler, IGasHandler
{
	public String name;

	public double storedEnergy;
	public double MAX_ENERGY = 1000;
	public FluidTank storedFluid;
	public GasTank storedGas;
	public ItemStack storedItem;

	public SharedInventory(String freq)
	{
		name = freq;

		storedEnergy = 0;
		storedFluid = new FluidTank(1000);
		storedGas = new GasTank(1000);
		storedItem = null;
	}

	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill)
	{
		return storedFluid.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
	{
		if(resource.isFluidEqual(storedFluid.getFluid()))
		{
			return storedFluid.drain(resource.amount, doDrain);
		}

		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
	{
		return storedFluid.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid)
	{
		return storedFluid.getFluid() == null || fluid == storedFluid.getFluid().getFluid();
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid)
	{
		return storedFluid.getFluid() == null || fluid == storedFluid.getFluid().getFluid();
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from)
	{
		return new FluidTankInfo[]{new FluidTankInfo(storedFluid)};
	}

	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		return storedGas.receive(stack, doTransfer);
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer)
	{
		return storedGas.draw(amount, doTransfer);
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type)
	{
		return storedGas.getGasType() == null || type == storedGas.getGasType();
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type)
	{
		return storedGas.getGasType() == null || type == storedGas.getGasType();
	}

	@Override
	public double getEnergy()
	{
		return storedEnergy;
	}

	@Override
	public void setEnergy(double energy)
	{
		storedEnergy = Math.max(0, Math.min(energy, MAX_ENERGY));
	}

	@Override
	public double getMaxEnergy()
	{
		return MAX_ENERGY;
	}
}
