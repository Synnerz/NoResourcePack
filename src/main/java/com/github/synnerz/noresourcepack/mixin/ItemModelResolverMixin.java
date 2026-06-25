package com.github.synnerz.noresourcepack.mixin;

import com.github.synnerz.noresourcepack.ItemsData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @WrapOperation(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object devonian$onAppendItemLayers(ItemStack instance, DataComponentType dataComponentType, Operation<Object> original) {
        Identifier modelId = (Identifier) original.call(instance, dataComponentType);
        return ItemsData.INSTANCE.fromModelId(instance, modelId);
    }

    @WrapOperation(method = "shouldPlaySwapAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object devonian$onShouldPlaySwapAnimation(ItemStack instance, DataComponentType dataComponentType, Operation<Object> original) {
        Identifier modelId = (Identifier) original.call(instance, dataComponentType);
        return ItemsData.INSTANCE.fromModelId(instance, modelId);
    }

    @WrapOperation(method = "swapAnimationScale", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object devonian$onSwapAnimationScale(ItemStack instance, DataComponentType dataComponentType, Operation<Object> original) {
        Identifier modelId = (Identifier) original.call(instance, dataComponentType);
        return ItemsData.INSTANCE.fromModelId(instance, modelId);
    }

    @WrapOperation(method = "appendItemLayers", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModel;update(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/item/ItemModelResolver;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/ItemOwner;I)V"))
    private void nrp$onAppendLayer(ItemModel instance, ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, ClientLevel clientLevel, ItemOwner itemOwner, int i, Operation<Void> original) {
        GameProfile gameProfile = ItemsData.INSTANCE.gameProfile(itemStack);
        if (gameProfile == null) {
            original.call(instance, itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, i);
            return;
        }

        ItemStack stack = itemStack.copy();
        stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile));
        original.call(instance, itemStackRenderState, stack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, i);
    }
}
