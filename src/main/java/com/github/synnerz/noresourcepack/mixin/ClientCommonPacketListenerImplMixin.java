package com.github.synnerz.noresourcepack.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin {
    @WrapOperation(
            method = "handleResourcePackPush",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/common/ClientboundResourcePackPushPacket;required()Z"
            )
    )
    private boolean nrp$onResourcePackPush(ClientboundResourcePackPushPacket instance, Operation<Boolean> original) {
        String url = instance.url();
        if (!url.startsWith("https://resourcepacks2.hypixel.net/SkyBlockResourcePack")) {
            return original.call(instance);
        }

        return false;
    }
}
