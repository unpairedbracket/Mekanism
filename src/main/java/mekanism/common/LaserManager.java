package mekanism.common;

import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.Pos3D;
import mekanism.api.lasers.ILaserReceptor;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;

public class LaserManager
{
	public static MovingObjectPosition fireLaser(TileEntity from, EnumFacing direction, double energy, World world)
	{
		return fireLaser(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
	}

	public static MovingObjectPosition fireLaser(Pos3D from, EnumFacing direction, double energy, World world)
	{
		Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);

		MovingObjectPosition mop = world.rayTraceBlocks(Vec3.createVectorHelper(from.xPos, from.yPos, from.zPos), Vec3.createVectorHelper(to.xPos, to.yPos, to.zPos));

		if(mop != null)
		{
			to = new Pos3D(mop.hitVec);
			Coord4D toCoord = new Coord4D(mop.blockX, mop.blockY, mop.blockZ);
			TileEntity tile = toCoord.getTileEntity(world);

			if(tile instanceof ILaserReceptor)
			{
				if(!(((ILaserReceptor)tile).canLasersDig()))
				{
					((ILaserReceptor)tile).receiveLaserEnergy(energy, EnumFacing.getFront(mop.sideHit));
				}
			}
		}

		from.translateExcludingSide(direction, -0.1);
		to.translateExcludingSide(direction, 0.1);

		for(Entity e : (List<Entity>)world.getEntitiesWithinAABB(Entity.class, Pos3D.getAABB(from, to)))
		{
			if(!e.isImmuneToFire()) e.setFire((int)(energy / 1000));
		}

		return mop;
	}

	public static List<ItemStack> breakBlock(Coord4D blockCoord, boolean dropAtBlock, World world)
	{
		List<ItemStack> ret = null;
		Block blockHit = blockCoord.getBlock(world);
		if(dropAtBlock)
		{
			blockHit.dropBlockAsItem(world, blockCoord.getPos().getX(), blockCoord.getPos().getY(), blockCoord.getPos().getZ(), blockCoord.getMetadata(world), 0);
		}
		else
		{
			ret = blockHit.getDrops(world, blockCoord.getPos().getX(), blockCoord.getPos().getY(), blockCoord.getPos().getZ(), blockCoord.getMetadata(world), 0);
		}
		blockHit.breakBlock(world, blockCoord.getPos().getX(), blockCoord.getPos().getY(), blockCoord.getPos().getZ(), blockHit, blockCoord.getMetadata(world));
		world.setBlockToAir(blockCoord.getPos().getX(), blockCoord.getPos().getY(), blockCoord.getPos().getZ());
		world.playAuxSFX(2001, blockCoord.getPos().getX(), blockCoord.getPos().getY(), blockCoord.getPos().getZ(), Block.getIdFromBlock(blockHit));
		return ret;
	}

	public static void fireLaserClient(TileEntity from, EnumFacing direction, double energy, World world)
	{
		fireLaserClient(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
	}

	public static void fireLaserClient(Pos3D from, EnumFacing direction, double energy, World world)
	{
		Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);
		MovingObjectPosition mop = world.rayTraceBlocks(Vec3.createVectorHelper(from.xPos, from.yPos, from.zPos), Vec3.createVectorHelper(to.xPos, to.yPos, to.zPos));

		if(mop != null)
		{
			to = new Pos3D(mop.hitVec);
		}
		from.translate(direction, -0.501);
		Mekanism.proxy.renderLaser(world, from, to, direction, energy);
	}

}
