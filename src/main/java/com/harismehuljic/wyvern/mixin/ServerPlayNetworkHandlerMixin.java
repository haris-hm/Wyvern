package com.harismehuljic.wyvern.mixin;

import com.harismehuljic.wyvern.Wyvern;
import com.harismehuljic.wyvern.util.MessageFormatter;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    @Shadow
    public ServerPlayerEntity player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(at = @At("HEAD"), method = "handleDecoratedMessage")
    private void onHandleDecoratedMessage(SignedMessage message, CallbackInfo ci) {
        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
            Wyvern.DISCORD_BOT.sendCustomMessage(
                    MessageFormatter.formatName(this.player),
                    MessageFormatter.getHelmetUrl(this.player),
                    message.getSignedContent()
            );
        }
    }
}
