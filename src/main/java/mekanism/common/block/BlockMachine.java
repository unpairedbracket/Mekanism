package mekanism.common.block;

import java.util.List;
import java.util.Random;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.api.MekanismConfig.usage;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.ClientProxy;
import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.IElectricChest;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.IInvConfiguration;
import mekanism.common.base.IRedstoneControl;
import mekanism.common.base.ISpecialBounds;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.network.PacketElectricChest.ElectricChestMessage;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tile.TileEntityAdvancedFactory;
import mekanism.common.tile.TileEntityAmbientAccumulator;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
import mekanism.common.tile.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.TileEntityChemicalInfuser;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.TileEntityChemicalOxidizer;
import mekanism.common.tile.TileEntityChemicalWasher;
import mekanism.common.tile.TileEntityCombiner;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityCrusher;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.TileEntityEliteFactory;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import mekanism.common.tile.TileEntityEntangledBlock;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidicPlenisher;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.tile.TileEntityLaserAmplifier;
import mekanism.common.tile.TileEntityLaserTractorBeam;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import mekanism.common.tile.TileEntityPRC;
import mekanism.common.tile.TileEntityPortableTank;
import mekanism.common.tile.TileEntityPrecisionSawmill;
import mekanism.common.tile.TileEntityPurificationChamber;
import mekanism.common.tile.TileEntityRotaryCondensentrator;
import mekanism.common.tile.TileEntitySeismicVibrator;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSpriteRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.tools.IToolWrench;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

/**
 * Block class for handling multiple machine block IDs.
 * 0:0: Enrichment Chamber
 * 0:1: Osmium Compressor
 * 0:2: Combiner
 * 0:3: Crusher
 * 0:4: Digital Miner
 * 0:5: Basic Factory
 * 0:6: Advanced Factory
 * 0:7: Elite Factory
 * 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber
 * 0:10: Energized Smelter
 * 0:11: Teleporter
 * 0:12: Electric Pump
 * 0:13: Electric Chest
 * 0:14: Chargepad
 * 0:15: Logistical Sorter
 * 1:0: Rotary Condensentrator
 * 1:1: Chemical Oxidizer
 * 1:2: Chemical Infuser
 * 1:3: Chemical Injection Chamber
 * 1:4: Electrolytic Separator
 * 1:5: Precision Sawmill
 * 1:6: Chemical Dissolution Chamber
 * 1:7: Chemical Washer
 * 1:8: Chemical Crystallizer
 * 1:9: Seismic Vibrator
 * 1:10: Pressurized Reaction Chamber
 * 1:11: Portable Tank
 * 1:12: Fluidic Plenisher
 * 1:13: Laser
 * 1:14: Laser Amplifier
 * 1:15: Laser Tractor Beam
 * 2:0: Entangled Block
 * 2:1: Ambient Accumulator
 * @author AidanBrady
 *
 */
@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheralProvider", modid = "ComputerCraft")
public class BlockMachine extends BlockContainer implements ISpecialBounds, IPeripheralProvider
{
	public TextureAtlasSprite[][] icons = new TextureAtlasSprite[16][16];

	public MachineBlock blockType;

	public BlockMachine(MachineBlock type)
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
		blockType = type;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(TextureAtlasSpriteRegister register)
	{
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				icons[0][0] = register.registerIcon("mekanism:EnrichmentChamberFrontOff");
				icons[0][1] = register.registerIcon("mekanism:EnrichmentChamberFrontOn");
				icons[0][2] = register.registerIcon("mekanism:SteelCasing");
				icons[1][0] = register.registerIcon("mekanism:OsmiumCompressorFrontOff");
				icons[1][1] = register.registerIcon("mekanism:OsmiumCompressorFrontOn");
				icons[1][2] = register.registerIcon("mekanism:SteelCasing");
				icons[2][0] = register.registerIcon("mekanism:CombinerFrontOff");
				icons[2][1] = register.registerIcon("mekanism:CombinerFrontOn");
				icons[2][2] = register.registerIcon("mekanism:SteelCasing");
				icons[3][0] = register.registerIcon("mekanism:CrusherFrontOff");
				icons[3][1] = register.registerIcon("mekanism:CrusherFrontOn");
				icons[3][2] = register.registerIcon("mekanism:SteelCasing");
				icons[5][0] = register.registerIcon("mekanism:BasicFactoryFront");
				icons[5][1] = register.registerIcon("mekanism:BasicFactorySide");
				icons[5][2] = register.registerIcon("mekanism:BasicFactoryTop");
				icons[6][0] = register.registerIcon("mekanism:AdvancedFactoryFront");
				icons[6][1] = register.registerIcon("mekanism:AdvancedFactorySide");
				icons[6][2] = register.registerIcon("mekanism:AdvancedFactoryTop");
				icons[7][0] = register.registerIcon("mekanism:EliteFactoryFront");
				icons[7][1] = register.registerIcon("mekanism:EliteFactorySide");
				icons[7][2] = register.registerIcon("mekanism:EliteFactoryTop");
				icons[9][0] = register.registerIcon("mekanism:PurificationChamberFrontOff");
				icons[9][1] = register.registerIcon("mekanism:PurificationChamberFrontOn");
				icons[9][2] = register.registerIcon("mekanism:SteelCasing");
				icons[10][0] = register.registerIcon("mekanism:EnergizedSmelterFrontOff");
				icons[10][1] = register.registerIcon("mekanism:EnergizedSmelterFrontOn");
				icons[10][2] = register.registerIcon("mekanism:SteelCasing");
				icons[11][0] = register.registerIcon("mekanism:Teleporter");
				break;
			case MACHINE_BLOCK_2:
				icons[3][0] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOff");
				icons[3][1] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOn");
				icons[3][2] = register.registerIcon("mekanism:SteelCasing");
				icons[5][0] = register.registerIcon("mekanism:PrecisionSawmillFrontOff");
				icons[5][1] = register.registerIcon("mekanism:PrecisionSawmillFrontOn");
				icons[5][2] = register.registerIcon("mekanism:SteelCasing");
				break;
			case MACHINE_BLOCK_3:
				icons[0][0] = register.registerIcon("mekanism:AmbientAccumulator");
				icons[1][0] = register.registerIcon("mekanism:SteelCasing");
				break;
		}

	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));
		int side = MathHelper.floor_double((entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int height = Math.round(entityliving.rotationPitch);
		int change = 3;

		if(tileEntity == null)
		{
			return;
		}

		if(tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
		{
			if(height >= 65)
			{
				change = 1;
			}
			else if(height <= -65)
			{
				change = 0;
			}
		}

		if(change != 0 && change != 1)
		{
			switch(side)
			{
				case 0: change = 2; break;
				case 1: change = 5; break;
				case 2: change = 3; break;
				case 3: change = 4; break;
			}
		}

		if(tileEntity instanceof TileEntityLogisticalSorter)
		{
			TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter)tileEntity;

			if(!transporter.hasInventory())
			{
				for(EnumFacing dir : EnumFacing.values())
				{
					TileEntity tile = Coord4D.get(transporter).offset(dir).getTileEntity(world);

					if(tile instanceof IInventory)
					{
						change = dir.getOpposite().ordinal();
						break;
					}
				}
			}
		}

		tileEntity.setFacing((short)change);
		tileEntity.redstone = world.isBlockIndirectlyGettingPowered(x, y, z);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onPlace();
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));

		if(MekanismUtils.isActive(world, x, y, z) && ((IActiveState)tileEntity).renderUpdate() && client.machineEffects)
		{
			float xRandom = (float)x + 0.5F;
			float yRandom = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
			float zRandom = (float)z + 0.5F;
			float iRandom = 0.52F;
			float jRandom = random.nextFloat() * 0.6F - 0.3F;

			int side = tileEntity.facing;

			if(tileEntity instanceof TileEntityMetallurgicInfuser)
			{
				side = EnumFacing.getFront(side).getOpposite().ordinal();
			}

			if(side == 4)
			{
				world.spawnParticle("smoke", (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == 5)
			{
				world.spawnParticle("smoke", (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == 2)
			{
				world.spawnParticle("smoke", (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
			}
			else if(side == 3)
			{
				world.spawnParticle("smoke", (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
				world.spawnParticle("reddust", (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

		if(client.machineEffects && tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}

		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(int side, int meta)
	{
		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						if(side == 3)
						{
							return icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					case 5:
					case 6:
					case 7:
						if(side == 3)
						{
							return icons[meta][0];
						} else if(side == 0 || side == 1)
						{
							return icons[meta][2];
						} else
						{
							return icons[meta][1];
						}
					default:
						return icons[11][0];
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						if(side == 3)
						{
							return icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					default:
						return icons[meta][0];
				}
			default:
				return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));

		switch(blockType)
		{
			case MACHINE_BLOCK_1:
				switch(meta)
				{
					case 0:
					case 1:
					case 2:
					case 3:
					case 9:
					case 10:
						if(side == tileEntity.facing)
						{
							return MekanismUtils.isActive(world, x, y, z) ? icons[meta][1] : icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					case 5:
					case 6:
					case 7:
						if(side == tileEntity.facing)
						{
							return icons[meta][0];
						} else if(side == 0 || side == 1)
						{
							return icons[meta][2];
						} else
						{
							return icons[meta][1];
						}
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_2:
				switch(meta)
				{
					case 3:
					case 5:
						if(side == tileEntity.facing)
						{
							return MekanismUtils.isActive(world, x, y, z) ? icons[meta][1] : icons[meta][0];
						} else
						{
							return icons[meta][2];
						}
					default:
						return icons[meta][0];
				}
			case MACHINE_BLOCK_3:
				switch(meta)
				{
					default:
						return icons[meta][0];
				}
		}
		return null;
	}

	@Override
	public int damageDropped(int i)
	{
		return i;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativetabs, List list)
	{
		for(MachineType type : MachineType.values())
		{
			if(type.typeBlock == blockType)
			{
				switch(type)
				{
					case BASIC_FACTORY:
					case ADVANCED_FACTORY:
					case ELITE_FACTORY:
						for(RecipeType recipe : RecipeType.values())
						{
							ItemStack stack = new ItemStack(item, 1, type.meta);
							((IFactory)stack.getItem()).setRecipeType(recipe.ordinal(), stack);
							list.add(stack);
						}
						break;
					case PORTABLE_TANK:
						list.add(new ItemStack(item, 1, type.meta));

						ItemBlockMachine itemMachine = (ItemBlockMachine)item;

						for(Fluid f : FluidRegistry.getRegisteredFluids().values())
						{
							ItemStack filled = new ItemStack(item, 1, type.meta);
							itemMachine.setFluidStack(new FluidStack(f, itemMachine.getCapacity(filled)), filled);
							itemMachine.setPrevScale(filled, 1);
							list.add(filled);
						}
						break;
					default:
						list.add(new ItemStack(item, 1, type.meta));
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float posX, float posY, float posZ)
	{
		if(ItemAttacher.canAttach(entityplayer.getCurrentEquippedItem()))
		{
			return false;
		}

		if(world.isRemote)
		{
			return true;
		}

		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));
		int metadata = world.getBlockMetadata(x, y, z);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			Item tool = entityplayer.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(entityplayer, x, y, z))
			{
				if(entityplayer.isSneaking() && metadata != 13)
				{
					dismantleBlock(world, x, y, z, false);
					return true;
				}

				if(ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools") && tool instanceof IToolWrench)
					((IToolWrench)tool).wrenchUsed(entityplayer, x, y, z);

				int change = EnumFacing.ROTATION_MATRIX[EnumFacing.UP.ordinal()][tileEntity.facing];

				if(tileEntity instanceof TileEntityLogisticalSorter)
				{
					if(!((TileEntityLogisticalSorter)tileEntity).hasInventory())
					{
						for(EnumFacing dir : EnumFacing.values())
						{
							TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);

							if(tile instanceof IInventory)
							{
								change = dir.getOpposite().ordinal();
								break;
							}
						}
					}
				}

				tileEntity.setFacing((short)change);
				world.notifyBlocksOfNeighborChange(x, y, z, this);
				return true;
			}
		}

		if(tileEntity != null)
		{
			MachineType type = MachineType.get(this, metadata);

			switch(type)
			{
				case ELECTRIC_CHEST:
					TileEntityElectricChest electricChest = (TileEntityElectricChest)tileEntity;

					if(!(entityplayer.isSneaking() || world.isSideSolid(x, y + 1, z, EnumFacing.DOWN)))
					{
						if(electricChest.canAccess())
						{
							MekanismUtils.openElectricChestGui((EntityPlayerMP)entityplayer, electricChest, null, true);
						} else if(!electricChest.authenticated)
						{
							Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, true, false, 2, 0, null, Coord4D.get(electricChest)), (EntityPlayerMP)entityplayer);
						} else
						{
							Mekanism.packetHandler.sendTo(new ElectricChestMessage(ElectricChestPacketType.CLIENT_OPEN, true, false, 1, 0, null, Coord4D.get(electricChest)), (EntityPlayerMP)entityplayer);
						}

						return true;
					}
					break;
				case PORTABLE_TANK:
					if(entityplayer.getCurrentEquippedItem() != null && FluidContainerRegistry.isContainer(entityplayer.getCurrentEquippedItem()))
					{
						if(manageInventory(entityplayer, (TileEntityPortableTank)tileEntity))
						{
							entityplayer.inventory.markDirty();
							return true;
						}
					} else
					{
						entityplayer.openGui(Mekanism.instance, type.guiId, world, x, y, z);
					}
					return true;
				case LOGISTICAL_SORTER:
					LogisticalSorterGuiMessage.openServerGui(SorterGuiPacket.SERVER, 0, world, (EntityPlayerMP)entityplayer, Coord4D.get(tileEntity), -1);
					return true;
				default:
					if(!entityplayer.isSneaking() && type.guiId != -1)
					{
						entityplayer.openGui(Mekanism.instance, type.guiId, world, x, y, z);
						return true;
					}
					return false;
			}
		}
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if(MachineType.get(this, metadata) == null)
		{
			return null;
		}

		return MachineType.get(this, metadata).create();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return null;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public Item getItemDropped(int i, Random random, int j)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return ClientProxy.MACHINE_RENDER_ID;
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z)
	{
		if(!(blockType == MachineBlock.MACHINE_BLOCK_1 && world.getBlockMetadata(x, y, z) == 13))
		{
			return blockHardness;
		}
		else {
			TileEntityElectricChest tileEntity = (TileEntityElectricChest)world.getTileEntity(new BlockPos(x, y, z));
			return tileEntity.canAccess() ? 3.5F : -1;
		}
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
		{
			TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));

			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(x, y, z);
	}
	
	private boolean manageInventory(EntityPlayer player, TileEntityPortableTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();

		if(itemStack != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.fluidTank.getFluid() != null && tileEntity.fluidTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(tileEntity.fluidTank.getFluid(), itemStack);

					if(filled != null)
					{
						if(player.capabilities.isCreativeMode)
						{
							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}

						if(itemStack.stackSize > 1)
						{
							if(player.inventory.addItemStackToInventory(filled))
							{
								itemStack.stackSize--;

								tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);

							tileEntity.fluidTank.drain(FluidContainerRegistry.getFluidForFilledItem(filled).amount, true);

							return true;
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(itemStack))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				int needed = tileEntity.getCurrentNeeded();
				
				if((tileEntity.fluidTank.getFluid() == null && itemFluid.amount <= tileEntity.fluidTank.getCapacity()) || itemFluid.amount <= needed)
				{
					if(tileEntity.fluidTank.getFluid() != null && !tileEntity.fluidTank.getFluid().isFluidEqual(itemFluid))
					{
						return false;
					}
					
					boolean filled = false;
					
					if(player.capabilities.isCreativeMode)
					{
						filled = true;
					}
					else {
						ItemStack containerItem = itemStack.getItem().getContainerItem(itemStack);
	
						if(containerItem != null)
						{
							if(itemStack.stackSize == 1)
							{
								player.setCurrentItemOrArmor(0, containerItem);
								filled = true;
							}
							else {
								if(player.inventory.addItemStackToInventory(containerItem))
								{
									itemStack.stackSize--;
	
									filled = true;
								}
							}
						}
						else {
							itemStack.stackSize--;
	
							if(itemStack.stackSize == 0)
							{
								player.setCurrentItemOrArmor(0, null);
							}
	
							filled = true;
						}
					}

					if(filled)
					{
						int toFill = Math.min(tileEntity.fluidTank.getCapacity()-tileEntity.fluidTank.getFluidAmount(), itemFluid.amount);
						
						tileEntity.fluidTank.fill(itemFluid, true);
						
						if(itemFluid.amount-toFill > 0)
						{
							tileEntity.pushUp(new FluidStack(itemFluid.getFluid(), itemFluid.amount-toFill), true);
						}
						
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(block);
			}

			if(tileEntity instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;

				if(!sorter.hasInventory())
				{
					for(EnumFacing dir : EnumFacing.values())
					{
						TileEntity tile = Coord4D.get(tileEntity).offset(dir).getTileEntity(world);

						if(tile instanceof IInventory)
						{
							sorter.setFacing((short)dir.getOpposite().ordinal());
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(new BlockPos(x, y, z));
		ItemStack itemStack = new ItemStack(this, 1, world.getBlockMetadata(x, y, z));

		if(itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		if(tileEntity instanceof IUpgradeTile)
		{
			((IUpgradeTile)tileEntity).getComponent().write(itemStack.getTagCompound());
		}

		if(tileEntity instanceof IInvConfiguration)
		{
			IInvConfiguration config = (IInvConfiguration)tileEntity;

			itemStack.getTagCompound().setBoolean("hasSideData", true);

			itemStack.getTagCompound().setBoolean("ejecting", config.getEjector().isEjecting());

			for(int i = 0; i < 6; i++)
			{
				itemStack.getTagCompound().setByte("config"+i, config.getConfiguration()[i]);
			}
		}
		
		if(tileEntity instanceof ISustainedData)
		{
			((ISustainedData)tileEntity).writeSustainedData(itemStack);
		}

		if(tileEntity instanceof IRedstoneControl)
		{
			IRedstoneControl control = (IRedstoneControl)tileEntity;
			itemStack.getTagCompound().setInteger("controlType", control.getControlType().ordinal());
		}

		if(tileEntity instanceof TileEntityElectricBlock)
		{
			IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
			energizedItem.setEnergy(itemStack, ((TileEntityElectricBlock)tileEntity).electricityStored);
		}

		if(tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock)tileEntity).inventory.length > 0)
		{
			ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
			inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
		}

		if(((ISustainedTank)itemStack.getItem()).hasTank(itemStack))
		{
			if(tileEntity instanceof ISustainedTank)
			{
				if(((ISustainedTank)tileEntity).getFluidStack() != null)
				{
					((ISustainedTank)itemStack.getItem()).setFluidStack(((ISustainedTank)tileEntity).getFluidStack(), itemStack);
				}
			}
		}

		if(tileEntity instanceof TileEntityElectricChest)
		{
			IElectricChest electricChest = (IElectricChest)itemStack.getItem();
			electricChest.setAuthenticated(itemStack, ((TileEntityElectricChest)tileEntity).authenticated);
			electricChest.setLocked(itemStack, ((TileEntityElectricChest)tileEntity).locked);
			electricChest.setPassword(itemStack, ((TileEntityElectricChest)tileEntity).password);
		}

		if(tileEntity instanceof TileEntityFactory)
		{
			IFactory factoryItem = (IFactory)itemStack.getItem();
			factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType.ordinal(), itemStack);
		}

		return itemStack;
	}

	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, x, y, z);

		world.setBlockToAir(x, y, z);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		MachineType type = MachineType.get(this, world.getBlockMetadata(x, y, z));

		switch(type)
		{
			case CHARGEPAD:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.06F, 1.0F);
				break;
			case PORTABLE_TANK:
				setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
				break;
			default:
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if(world.getTileEntity(new BlockPos(x, y, z)) instanceof TileEntityChargepad)
		{
			return null;
		}

		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, EnumFacing side)
	{
		MachineType type = MachineType.get(blockType, world.getBlockMetadata(x, y, z));

		switch(type)
		{
			case CHARGEPAD:
				return false;
			case PORTABLE_TANK:
				return side == EnumFacing.UP || side == EnumFacing.DOWN;
		}

		return true;
	}

	public static enum MachineBlock
	{
		MACHINE_BLOCK_1,
		MACHINE_BLOCK_2,
		MACHINE_BLOCK_3;

		public Block getBlock()
		{
			switch(this)
			{
				case MACHINE_BLOCK_1:
					return MekanismBlocks.MachineBlock;
				case MACHINE_BLOCK_2:
					return MekanismBlocks.MachineBlock2;
				case MACHINE_BLOCK_3:
					return MekanismBlocks.MachineBlock3;
				default:
					return null;
			}
		}
	}

	public static enum MachineType
	{
		ENRICHMENT_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 0, "EnrichmentChamber", 3, TileEntityEnrichmentChamber.class, true, false, true),
		OSMIUM_COMPRESSOR(MachineBlock.MACHINE_BLOCK_1, 1, "OsmiumCompressor", 4, TileEntityOsmiumCompressor.class, true, false, true),
		COMBINER(MachineBlock.MACHINE_BLOCK_1, 2, "Combiner", 5, TileEntityCombiner.class, true, false, true),
		CRUSHER(MachineBlock.MACHINE_BLOCK_1, 3, "Crusher", 6, TileEntityCrusher.class, true, false, true),
		DIGITAL_MINER(MachineBlock.MACHINE_BLOCK_1, 4, "DigitalMiner", 2, TileEntityDigitalMiner.class, true, true, true),
		BASIC_FACTORY(MachineBlock.MACHINE_BLOCK_1, 5, "BasicFactory", 11, TileEntityFactory.class, true, false, true),
		ADVANCED_FACTORY(MachineBlock.MACHINE_BLOCK_1, 6, "AdvancedFactory", 11, TileEntityAdvancedFactory.class, true, false, true),
		ELITE_FACTORY(MachineBlock.MACHINE_BLOCK_1, 7, "EliteFactory", 11, TileEntityEliteFactory.class, true, false, true),
		METALLURGIC_INFUSER(MachineBlock.MACHINE_BLOCK_1, 8, "MetallurgicInfuser", 12, TileEntityMetallurgicInfuser.class, true, true, true),
		PURIFICATION_CHAMBER(MachineBlock.MACHINE_BLOCK_1, 9, "PurificationChamber", 15, TileEntityPurificationChamber.class, true, false, true),
		ENERGIZED_SMELTER(MachineBlock.MACHINE_BLOCK_1, 10, "EnergizedSmelter", 16, TileEntityEnergizedSmelter.class, true, false, true),
		TELEPORTER(MachineBlock.MACHINE_BLOCK_1, 11, "Teleporter", 13, TileEntityTeleporter.class, true, false, false),
		ELECTRIC_PUMP(MachineBlock.MACHINE_BLOCK_1, 12, "ElectricPump", 17, TileEntityElectricPump.class, true, true, false),
		ELECTRIC_CHEST(MachineBlock.MACHINE_BLOCK_1, 13, "ElectricChest", -1, TileEntityElectricChest.class, true, true, false),
		CHARGEPAD(MachineBlock.MACHINE_BLOCK_1, 14, "Chargepad", -1, TileEntityChargepad.class, true, true, false),
		LOGISTICAL_SORTER(MachineBlock.MACHINE_BLOCK_1, 15, "LogisticalSorter", -1, TileEntityLogisticalSorter.class, false, true, false),
		ROTARY_CONDENSENTRATOR(MachineBlock.MACHINE_BLOCK_2, 0, "RotaryCondensentrator", 7, TileEntityRotaryCondensentrator.class, true, true, false),
		CHEMICAL_OXIDIZER(MachineBlock.MACHINE_BLOCK_2, 1, "ChemicalOxidizer", 29, TileEntityChemicalOxidizer.class, true, true, true),
		CHEMICAL_INFUSER(MachineBlock.MACHINE_BLOCK_2, 2, "ChemicalInfuser", 30, TileEntityChemicalInfuser.class, true, true, false),
		CHEMICAL_INJECTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 3, "ChemicalInjectionChamber", 31, TileEntityChemicalInjectionChamber.class, true, false, true),
		ELECTROLYTIC_SEPARATOR(MachineBlock.MACHINE_BLOCK_2, 4, "ElectrolyticSeparator", 32, TileEntityElectrolyticSeparator.class, true, true, false),
		PRECISION_SAWMILL(MachineBlock.MACHINE_BLOCK_2, 5, "PrecisionSawmill", 34, TileEntityPrecisionSawmill.class, true, false, true),
		CHEMICAL_DISSOLUTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 6, "ChemicalDissolutionChamber", 35, TileEntityChemicalDissolutionChamber.class, true, true, true),
		CHEMICAL_WASHER(MachineBlock.MACHINE_BLOCK_2, 7, "ChemicalWasher", 36, TileEntityChemicalWasher.class, true, true, false),
		CHEMICAL_CRYSTALLIZER(MachineBlock.MACHINE_BLOCK_2, 8, "ChemicalCrystallizer", 37, TileEntityChemicalCrystallizer.class, true, true, true),
		SEISMIC_VIBRATOR(MachineBlock.MACHINE_BLOCK_2, 9, "SeismicVibrator", 39, TileEntitySeismicVibrator.class, true, true, false),
		PRESSURIZED_REACTION_CHAMBER(MachineBlock.MACHINE_BLOCK_2, 10, "PressurizedReactionChamber", 40, TileEntityPRC.class, true, true, false),
		PORTABLE_TANK(MachineBlock.MACHINE_BLOCK_2, 11, "PortableTank", 41, TileEntityPortableTank.class, false, true, false),
		FLUIDIC_PLENISHER(MachineBlock.MACHINE_BLOCK_2, 12, "FluidicPlenisher", 42, TileEntityFluidicPlenisher.class, true, true, false),
		LASER(MachineBlock.MACHINE_BLOCK_2, 13, "Laser", -1, TileEntityLaser.class, true, true, false),
		LASER_AMPLIFIER(MachineBlock.MACHINE_BLOCK_2, 14, "LaserAmplifier", 44, TileEntityLaserAmplifier.class, false, true, false),
		LASER_TRACTOR_BEAM(MachineBlock.MACHINE_BLOCK_2, 15, "LaserTractorBeam", 45, TileEntityLaserTractorBeam.class, false, true, false),
		AMBIENT_ACCUMULATOR(MachineBlock.MACHINE_BLOCK_3, 0, "AmbientAccumulator", 46, TileEntityAmbientAccumulator.class, true, false, false),
		ENTANGLED_BLOCK(MachineBlock.MACHINE_BLOCK_3, 1, "EntangledBlock", 47, TileEntityEntangledBlock.class, true, false, false);

		public MachineBlock typeBlock;
		public int meta;
		public String name;
		public int guiId;
		public double baseEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean isElectric;
		public boolean hasModel;
		public boolean supportsUpgrades;

		private MachineType(MachineBlock block, int i, String s, int j, Class<? extends TileEntity> tileClass, boolean electric, boolean model, boolean upgrades)
		{
			typeBlock = block;
			meta = i;
			name = s;
			guiId = j;
			tileEntityClass = tileClass;
			isElectric = electric;
			hasModel = model;
			supportsUpgrades = upgrades;
		}

		public static MachineType get(Block block, int meta)
		{
			if(block instanceof BlockMachine)
			{
				return get(((BlockMachine)block).blockType, meta);
			}

			return null;
		}

		public static MachineType get(MachineBlock block, int meta)
		{
			for(MachineType type : values())
			{
				if(type.meta == meta && type.typeBlock == block)
				{
					return type;
				}
			}

			return null;
		}

		public TileEntity create()
		{
			try {
				return tileEntityClass.newInstance();
			} catch(Exception e) {
				Mekanism.logger.error("Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}

		/** Used for getting the base energy storage. */
		public double getUsage()
		{
			switch(this)
			{
				case ENRICHMENT_CHAMBER:
					return usage.enrichmentChamberUsage;
				case OSMIUM_COMPRESSOR:
					return usage.osmiumCompressorUsage;
				case COMBINER:
					return usage.combinerUsage;
				case CRUSHER:
					return usage.crusherUsage;
				case DIGITAL_MINER:
					return usage.digitalMinerUsage;
				case BASIC_FACTORY:
					return usage.factoryUsage * 3;
				case ADVANCED_FACTORY:
					return usage.factoryUsage * 5;
				case ELITE_FACTORY:
					return usage.factoryUsage * 7;
				case METALLURGIC_INFUSER:
					return usage.metallurgicInfuserUsage;
				case PURIFICATION_CHAMBER:
					return usage.purificationChamberUsage;
				case ENERGIZED_SMELTER:
					return usage.energizedSmelterUsage;
				case TELEPORTER:
					return 12500;
				case ELECTRIC_PUMP:
					return usage.electricPumpUsage;
				case ELECTRIC_CHEST:
					return 30;
				case CHARGEPAD:
					return 25;
				case LOGISTICAL_SORTER:
					return 0;
				case ROTARY_CONDENSENTRATOR:
					return usage.rotaryCondensentratorUsage;
				case CHEMICAL_OXIDIZER:
					return usage.oxidationChamberUsage;
				case CHEMICAL_INFUSER:
					return usage.chemicalInfuserUsage;
				case CHEMICAL_INJECTION_CHAMBER:
					return usage.chemicalInjectionChamberUsage;
				case ELECTROLYTIC_SEPARATOR:
					return general.FROM_H2 * 2;
				case PRECISION_SAWMILL:
					return usage.precisionSawmillUsage;
				case CHEMICAL_DISSOLUTION_CHAMBER:
					return usage.chemicalDissolutionChamberUsage;
				case CHEMICAL_WASHER:
					return usage.chemicalWasherUsage;
				case CHEMICAL_CRYSTALLIZER:
					return usage.chemicalCrystallizerUsage;
				case SEISMIC_VIBRATOR:
					return usage.seismicVibratorUsage;
				case PRESSURIZED_REACTION_CHAMBER:
					return usage.pressurizedReactionBaseUsage;
				case PORTABLE_TANK:
					return 0;
				case FLUIDIC_PLENISHER:
					return usage.fluidicPlenisherUsage;
				case LASER:
					return usage.laserUsage;
				case LASER_AMPLIFIER:
					return 0;
				case LASER_TRACTOR_BEAM:
					return 0;
				case AMBIENT_ACCUMULATOR:
					return 0;
				case ENTANGLED_BLOCK:
					return 0;
				default:
					return 0;
			}
		}

		public static void updateAllUsages()
		{
			for(MachineType type : values())
			{
				type.updateUsage();
			}
		}

		public void updateUsage()
		{
			baseEnergy = 400 * getUsage();
		}

		public String getDescription()
		{
			return MekanismUtils.localize("tooltip." + name);
		}

		public ItemStack getStack()
		{
			return new ItemStack(typeBlock.getBlock(), 1, meta);
		}

		public static MachineType get(ItemStack stack)
		{
			return get(Block.getBlockFromItem(stack.getItem()), stack.getItemDamage());
		}

		@Override
		public String toString()
		{
			return Integer.toString(meta);
		}
	}

	@Override
	public void setRenderBounds(Block block, int metadata) {}

	@Override
	public boolean doDefaultBoundSetting(int metadata)
	{
		return false;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public IPeripheral getPeripheral(World world, int x, int y, int z, int side)
	{
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		
		if(te != null && te instanceof IPeripheral)
		{
			return (IPeripheral)te;
		}
		
		return null;
	}

	@Override
	public EnumFacing[] getValidRotations(World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		EnumFacing[] valid = new EnumFacing[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(EnumFacing dir : EnumFacing.values())
			{
				if(basicTile.canSetFacing(dir.ordinal()))
				{
					valid[dir.ordinal()] = dir;
				}
			}
		}
		
		return valid;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, EnumFacing axis)
	{
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			if(basicTile.canSetFacing(axis.ordinal()))
			{
				basicTile.setFacing((short)axis.ordinal());
				return true;
			}
		}
		
		return false;
	}
}
