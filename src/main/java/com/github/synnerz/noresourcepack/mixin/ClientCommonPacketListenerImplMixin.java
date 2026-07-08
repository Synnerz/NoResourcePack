package com.github.synnerz.noresourcepack.mixin;

import com.github.synnerz.noresourcepack.NoResourcePack;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin {
    @Shadow
    @Final
    @Nullable
    protected ServerData serverData;

    @Inject(
            method = "handleResourcePackPush",
            at = @At("HEAD"),
            cancellable = true
    )
    private void nrp$onPackPush(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (
                !packet.url().contains("resourcepacks.hypixel.net") ||
                serverData == null ||
                serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED
        ) {
            NoResourcePack.INSTANCE.setVanillaTooltip(false);
            return;
        }

        ClientCommonPacketListenerImpl impl = (ClientCommonPacketListenerImpl) (Object) this;

        impl.send(new ServerboundResourcePackPacket(
                packet.id(),
                ServerboundResourcePackPacket.Action.ACCEPTED
        ));
        impl.send(new ServerboundResourcePackPacket(
                packet.id(),
                ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED
        ));
        NoResourcePack.INSTANCE.setVanillaTooltip(true);

        ci.cancel();
    }
}
