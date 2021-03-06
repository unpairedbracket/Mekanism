package mekanism.common.recipe.inputs;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

public class GasInput extends MachineInput<GasInput>
{
	public GasStack ingredient;

	public GasInput(GasStack stack)
	{
		ingredient = stack;
	}

	@Override
	public GasInput copy()
	{
		return new GasInput(ingredient.copy());
	}

	@Override
	public boolean isValid()
	{
		return ingredient != null;
	}

	public boolean useGas(GasTank gasTank, boolean deplete)
	{
		if(gasTank.getGasType() == ingredient.getGas() && gasTank.getStored() >= ingredient.amount)
		{
			gasTank.draw(ingredient.amount, deplete);
			return true;
		}
		return false;
	}

	@Override
	public int hashIngredients()
	{
		return ingredient.hashCode();
	}

	@Override
	public boolean testEquality(GasInput other)
	{
		if(!isValid())
		{
			return !other.isValid();
		}
		return other.ingredient.hashCode() == ingredient.hashCode();
	}

	@Override
	public boolean isInstance(Object other)
	{
		return other instanceof GasInput;
	}
}
