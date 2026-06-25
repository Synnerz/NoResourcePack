package com.github.synnerz.noresourcepack.mixin;

import com.github.synnerz.noresourcepack.NoResourcePack;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TooltipRenderUtil.class)
public class ToolTipRenderUtilMixin {
    @Shadow
    @Final
    private static Identifier BACKGROUND_SPRITE;

    @Shadow
    @Final
    private static Identifier FRAME_SPRITE;

    @Inject(method = "getBackgroundSprite", at = @At("HEAD"), cancellable = true)
    private static void nrp$getBackgroundSprite(Identifier style, CallbackInfoReturnable<Identifier> cir) {
        if (!NoResourcePack.INSTANCE.getVanillaTooltip() || style == null) return;
        if (!style.getNamespace().startsWith("hypixel_skyblock")) return;
        cir.setReturnValue(BACKGROUND_SPRITE);
        cir.cancel();
    }

    @Inject(method = "getFrameSprite", at = @At("HEAD"), cancellable = true)
    private static void nrp$getFrameSprite(Identifier style, CallbackInfoReturnable<Identifier> cir) {
        if (!NoResourcePack.INSTANCE.getVanillaTooltip() || style == null) return;
        if (!style.getNamespace().startsWith("hypixel_skyblock")) return;
        cir.setReturnValue(FRAME_SPRITE);
        cir.cancel();
    }
}
