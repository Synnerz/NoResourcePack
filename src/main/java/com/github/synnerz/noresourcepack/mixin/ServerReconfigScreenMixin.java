package com.github.synnerz.noresourcepack.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerReconfigScreen.class)
public class ServerReconfigScreenMixin {
    @Shadow
    private Button disconnectButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void nrp$onReconfigScreen(CallbackInfo ci) {
        disconnectButton.active = true;
    }
}
