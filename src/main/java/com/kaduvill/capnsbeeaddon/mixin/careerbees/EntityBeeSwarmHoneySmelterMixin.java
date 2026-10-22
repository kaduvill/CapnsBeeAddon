package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rwtema.careerbees.BeeMod;
import com.rwtema.careerbees.effects.EffectHoneyCombIngotConvert;
import com.rwtema.careerbees.effects.ISpecialBeeEffect;
import com.rwtema.careerbees.entity.EntityBeeSwarm;
import com.rwtema.careerbees.items.ItemIngredients;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityBeeSwarm.class, remap = false)
public abstract class EntityBeeSwarmHoneySmelterMixin {

    @Shadow(remap = false)
    public abstract ISpecialBeeEffect getEffect();

    @WrapOperation(
            method = "onUpdate()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/item/EntityItem;setItem(Lnet/minecraft/item/ItemStack;)V",
                    remap = true
            ),
            require = 1,
            allow = 1,
            remap = true
    )
    private void capnsbeeaddon$preserveHoneySmelterRemainder(
            EntityItem target, ItemStack converted, Operation<Void> original
    ) {
        if (getEffect() != EffectHoneyCombIngotConvert.INSTANCE) {
            original.call(target, converted);
            return;
        }

        ItemStack input = target.getItem();
        // The native conversion returns one fresh honeycomb ingot without
        // changing the input. Fail closed if another patch changes that result.
        if (target.world.isRemote || target.isDead || input.isEmpty()
                || converted.isEmpty() || converted.getCount() != 1
                || converted.getItem() != BeeMod.instance.itemIngredients
                || converted.getMetadata() != ItemIngredients.IngredientType.INGOTHONEYCOLM.meta) {
            return;
        }
        if (input.getCount() == 1) {
            original.call(target, converted);
            return;
        }

        EntityItem product = new EntityItem(
                target.world, target.posX, target.posY, target.posZ);
        // Copy only item-entity state, not Entity NBT (which contains its UUID).
        // Replace the serialized input before reading; even oversized input
        // counts are never round-tripped through NBT or clamped.
        NBTTagCompound drop = new NBTTagCompound();
        target.writeEntityToNBT(drop);
        drop.setTag("Item", converted.writeToNBT(new NBTTagCompound()));
        product.readEntityFromNBT(drop);
        product.motionX = target.motionX;
        product.motionY = target.motionY;
        product.motionZ = target.motionZ;
        product.setNoGravity(target.hasNoGravity());

        // FakeHousingPlayer.addProduct ignores spawnEntity's return value.
        // Keep the input unless the world accepts this single output entity.
        if (target.world.spawnEntity(product)) {
            input.shrink(1);
            target.setItem(input);
        }
        // Skip native replacement in both cases. onUpdate still ends the swarm.
    }
}
