/*
 * Copyright (c) 2018-2020 C4
 *
 * This file is part of Curios, a mod made for Minecraft.
 *
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.curios.common.inventory;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import top.theillusivec4.curios.Curios;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CurioSlot extends SlotItemHandler {

  private final String identifier;
  private final PlayerEntity player;
  private final UUID uuid;
  private NonNullList<Boolean> renderStatuses;

  public CurioSlot(PlayerEntity player, IDynamicStackHandler handler, int index, String identifier,
	  int xPosition, int yPosition, NonNullList<Boolean> renders) {
	super(handler, index, xPosition, yPosition);
	this.identifier = identifier;
	this.renderStatuses = renders;
	this.player = player;
	this.uuid = MathHelper.getRandomUUID();
	this.setBackground(PlayerContainer.LOCATION_BLOCKS_TEXTURE,
		player.getEntityWorld().isRemote() ? CuriosApi.getIconHelper().getIcon(identifier)
			: new ResourceLocation(Curios.MODID, "item/empty_curio_slot"));

	this.debug("[CREATION]");
  }

  public void debug(String marker) {
	//Curios.LOGGER.info(marker + " UUID: " + this.uuid + ", Slot: " + this.identifier + ", index: " + this.getSlotIndex() + ", SlotPosX: " + this.xPos + ", SlotPosY: " + this.yPos);
  }

  public String getIdentifier() {
	return this.identifier;
  }

  public boolean getRenderStatus() {
	return this.renderStatuses.get(this.getSlotIndex());
  }

  @OnlyIn(Dist.CLIENT)
  public String getSlotName() {
	return I18n.format("curios.identifier." + this.identifier);
  }

  @Override
  public boolean isItemValid(@Nonnull ItemStack stack) {
	return this.hasValidTag(CuriosApi.getCuriosHelper().getCurioTags(stack.getItem())) && CuriosApi
		.getCuriosHelper().getCurio(stack).map(curio -> curio.canEquip(this.identifier, this.player))
		.orElse(true) && super.isItemValid(stack);
  }

  protected boolean hasValidTag(Set<String> tags) {
	return tags.contains(this.identifier) || tags.contains("curio");
  }

  @Override
  public boolean canTakeStack(PlayerEntity playerIn) {
	ItemStack stack = this.getStack();
	return (stack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(stack))
		&& CuriosApi.getCuriosHelper().getCurio(stack)
		.map(curio -> curio.canUnequip(this.identifier, playerIn)).orElse(true) && super
		.canTakeStack(playerIn);
  }
}
