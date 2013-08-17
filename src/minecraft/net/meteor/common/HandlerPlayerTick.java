package net.meteor.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class HandlerPlayerTick
implements ITickHandler
{
	public void tickStart(EnumSet type, Object... tickData) {}

	public void tickEnd(EnumSet type, Object... tickData)
	{
		EntityPlayer player = (EntityPlayer)tickData[0];
		World world = player.worldObj;
		InventoryPlayer inv = player.inventory;

		if ((isWearing(MeteorsMod.KreknoriteHelmet.itemID, inv.armorItemInSlot(3))) && 
				(isWearing(MeteorsMod.KreknoriteBody.itemID, inv.armorItemInSlot(2))) && 
				(isWearing(MeteorsMod.KreknoriteLegs.itemID, inv.armorItemInSlot(1))) && 
				(isWearing(MeteorsMod.KreknoriteBoots.itemID, inv.armorItemInSlot(0)))) {
			ArmorEffectController.setImmuneToFire(player, true);
		} else {
			ArmorEffectController.setImmuneToFire(player, false);
		}

		boolean wearingLegs = EnchantmentHelper.getEnchantmentLevel(MeteorsMod.ColdTouch.effectId, inv.armorItemInSlot(1)) > 0;
		boolean wearingBoots = EnchantmentHelper.getEnchantmentLevel(MeteorsMod.ColdTouch.effectId, inv.armorItemInSlot(0)) > 0;
		if ((wearingLegs || wearingBoots) && !player.isInWater())
		{
			if (wearingLegs && wearingBoots && player.isSprinting())
			{
				int l = MathHelper.floor_double(player.posX);
				int j1 = MathHelper.floor_double(player.posY - 2.0D);
				int k1 = MathHelper.floor_double(player.posZ);
				for (int x = l - 2; x < l + 2; x++) {
					for (int z = k1 - 2; z < k1 + 2; z++) {
						if ((world.getBlockId(x, j1, z) == Block.waterStill.blockID) || (world.getBlockId(x, j1, z) == Block.waterMoving.blockID)) {
							world.setBlock(x, j1, z, Block.ice.blockID, 0, 2);
						}
					}		
				}
			}
			else
			{
				for (int j = 0; j < 4; j++) {
					int l = MathHelper.floor_double(player.posX + (j % 2 * 2 - 1) * 0.25F);
					int j1 = MathHelper.floor_double(player.posY - 2.0D);
					int k1 = MathHelper.floor_double(player.posZ + (j / 2 % 2 * 2 - 1) * 0.25F);
					if ((world.getBlockId(l, j1, k1) == Block.waterStill.blockID) || (world.getBlockId(l, j1, k1) == Block.waterMoving.blockID)) {
						world.setBlock(l, j1, k1, Block.ice.blockID, 0, 2);
					}
				}
			}
		}

		List entities = world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(player.posX - 16.0D, player.posY - 16.0D, player.posZ - 16.0D, player.posX + 16.0D, player.posY + 16.0D, player.posZ + 16.0D));
		for (int i1 = 0; i1 < entities.size(); i1++)
			if ((entities.get(i1) instanceof EntityItem))
				updateEntityItem((EntityItem)entities.get(i1));
	}

	public EnumSet ticks()
	{
		return EnumSet.of(TickType.PLAYER);
	}

	public String getLabel()
	{
		return "PlayerMeteor";
	}

	private static boolean isWearing(int ID, ItemStack item) {
		if ((item != null) && 
				(item.itemID == ID)) return true;

		return false;
	}
	
	public static boolean isPlayerMagnetized(EntityPlayer player) {
		return EnchantmentHelper.getMaxEnchantmentLevel(MeteorsMod.Magnetization.effectId, player.getLastActiveItems()) > 0 || 
				EnchantmentHelper.getEnchantmentLevel(MeteorsMod.Magnetization.effectId, player.getHeldItem()) > 0;
	}

	private void updateEntityItem(EntityItem en) {
		double closeness = 16.0D;
		EntityPlayer player = en.worldObj.getClosestPlayerToEntity(en, closeness);

		if ((player != null) && isPlayerMagnetized(player)) {
			double var3 = (player.posX - en.posX) / closeness;
			double var5 = (player.posY + player.getEyeHeight() - en.posY) / closeness;
			double var7 = (player.posZ - en.posZ) / closeness;
			double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
			double var11 = 1.0D - var9;

			if (var11 > 0.0D)
			{
				var11 *= var11;
				en.motionX += var3 / var9 * var11 * 0.1D;
				en.motionY += var5 / var9 * var11 * 0.1D;
				en.motionZ += var7 / var9 * var11 * 0.1D;
			}
		}
	}
}