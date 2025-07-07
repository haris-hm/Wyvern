package com.harismehuljic.wyvern.mixin;

import com.harismehuljic.wyvern.Wyvern;
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

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    @Shadow
    public ServerPlayerEntity player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(at = @At("HEAD"), method = "handleDecoratedMessage")
    private void onHandleDecoratedMessage(SignedMessage message, CallbackInfo ci) {
        Objects.requireNonNull(this.player.getDisplayName());

        String messageContents = message.getSignedContent();
        String discordMsg;

        String playerName = String.format("%s", this.player.getDisplayName().getString());
        String realName = this.player.getGameProfile().getName();

        Wyvern.LOGGER.info("Player name: {}, Real Name: {}, realName.equals(playerName): {}",
                playerName, realName, realName.equals(playerName));

        if (!realName.isEmpty() && realName.equals(playerName)) {
            discordMsg = String.format("**%s**  》%s", playerName, messageContents);
        }
        else {
            discordMsg = String.format("**%s** (*%s*)  》%s", playerName, realName, messageContents);
        }

        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
            Wyvern.DISCORD_BOT.sendMessageInGuild(discordMsg);
        }
    }
}
