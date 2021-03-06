package mekanism.common.tile;

import java.util.Map;

import mekanism.api.MekanismConfig.usage;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.CrusherRecipe;

public class TileEntityCrusher extends TileEntityElectricMachine<CrusherRecipe>
{
	public TileEntityCrusher()
	{
		super("crusher", "Crusher", usage.crusherUsage, 200, MachineType.CRUSHER.baseEnergy);
	}

	@Override
	public Map getRecipes()
	{
		return Recipe.CRUSHER.get();
	}

	@Override
	public float getVolume()
	{
		return 0.5F;
	}
}
