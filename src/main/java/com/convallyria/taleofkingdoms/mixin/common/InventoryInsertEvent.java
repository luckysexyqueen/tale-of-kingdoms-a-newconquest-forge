package com.convallyria.taleofkingdoms.mixin.common;

import com.convallyria.taleofkingdoms.common.event.InventoryInsertCallback;
import com.convallyria.taleofkingdoms.mixin.common.accessor.PlayerInventoryAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slot.class)
public abstract class InventoryInsertEvent {

    @Shadow @Final
    public Container container;

    @Shadow public abstract int getContainerSlot();

    @Inject(method = "set",
            at = @At("HEAD"),
            cancellable = true)
    public void insertStack(ItemStack itemStack, CallbackInfo ci) {
        if (container instanceof InventoryMenu) {
            InventoryMenu inventoryMenu = (InventoryMenu) container;
            PlayerInventoryAccessor accessor = (PlayerInventoryAccessor) inventoryMenu;
            if (!InventoryInsertCallback.EVENT.invoker().insertStack(accessor.getOwner(), getContainerSlot(), itemStack)) {
                ci.cancel();
            }
        }
    }
}
