/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.transport;

import net.minecraft.util.EnumFacing;
import buildcraft.api.gates.IGate;

public interface IPipe {

	int x();

	int y();

	int z();

	IPipeTile getTile();

	IGate getGate(EnumFacing side);
	
	boolean hasGate(EnumFacing side);
	
	boolean isWired(PipeWire wire);
	
	boolean isWireActive(PipeWire wire);
}
