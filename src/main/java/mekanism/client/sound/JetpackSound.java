package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemJetpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class JetpackSound extends PlayerSound
{
	public JetpackSound(EntityPlayer player)
	{
		super(player, new ResourceLocation("mekanism", "item.jetpack"));
		
		setFadeIn(30);
		setFadeOut(10);
	}

	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}

	@Override
	public boolean shouldPlaySound()
	{
		return hasJetpack(player) && ClientTickHandler.isJetpackOn(player);
	}

	private boolean hasJetpack(EntityPlayer player)
	{
		return player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof ItemJetpack;
	}
}
