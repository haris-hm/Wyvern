package com.harismehuljic.wyvern.mixin;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.util.MessageFormatter;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonPacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(at = @At("HEAD"), method = "broadcastChatMessage")
    private void onHandleDecoratedMessage(PlayerChatMessage message, CallbackInfo ci) {
        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
            Wyvern.DISCORD_BOT.sendCustomMessage(
                    MessageFormatter.formatName(this.player),
                    MessageFormatter.getHelmetUrl(this.player),
                    message.signedContent()
            );
        }
    }
}
