package mekanism.common.base;

import java.util.ArrayList;

import mekanism.common.SideData;

import net.minecraft.util.EnumFacing;

/**
 * Implement this if your TileEntity is capable of being modified by a Configurator in it's 'modify' mode.
 * @author AidanBrady
 *
 */
public interface IInvConfiguration
{
	/**
	 * Gets an ArrayList of side data this machine contains.
	 * @return
	 */
	public ArrayList<SideData> getSideData();

	/**
	 * Gets this machine's configuration as a byte[] -- each byte matching with the index of the defined SideData.
	 * @return
	 */
	public byte[] getConfiguration();

	/**
	 * Gets this machine's current orientation.
	 * @return
	 */
	public EnumFacing getFacing();

	/**
	 * Gets this machine's ejector.
	 * @return
	 */
	public IEjector getEjector();
}
