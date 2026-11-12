package com.kaiju;

import com.kaiju.entity.GodzillaEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

/** Use on a fully charged Godzilla (charge it in the pink cave) to evolve it into its final form. */
public class NuclearShipItem extends Item {
    public NuclearShipItem(Settings s) { super(s); }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof GodzillaEntity g)) return ActionResult.PASS;
        if (user.getWorld().isClient) return ActionResult.SUCCESS;
        if (g.getStage() != 1) { user.sendMessage(Text.translatable("msg.kaiju.ship.stage"), true); return ActionResult.FAIL; }
        if (g.getCharge() < GodzillaEntity.EVOLVE_CHARGE) { user.sendMessage(Text.translatable("msg.kaiju.ship.charge", g.getCharge() * 100 / GodzillaEntity.EVOLVE_CHARGE), true); return ActionResult.FAIL; }
        g.setCharge(0);
        g.evolveTo(2);
        if (!user.isCreative()) stack.decrement(1);
        return ActionResult.SUCCESS;
    }
}
