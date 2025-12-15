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
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
    private String formatMessage(String format, String playerName, String messageContents) {
        return format.replace("{username}", playerName).replace("{message}", messageContents);
    }

    @Inject(at = @At("HEAD"), method = "handleDecoratedMessage")
    private void onHandleDecoratedMessage(SignedMessage message, CallbackInfo ci) {
        Objects.requireNonNull(this.player.getDisplayName());

        String messageContents = message.getSignedContent();

        String playerName = this.player.getDisplayName().getString();
        String realName = this.player.getGameProfile().name();
        boolean nicknamed = !realName.equals(playerName);

        String playerNameBolded = nicknamed ? String.format("**%s** (*%s*)", playerName, realName) : String.format("**%s**", playerName);

        String discordMsg = formatMessage(Wyvern.CONFIG_DATA.getMessageFormat(), playerNameBolded, messageContents);

        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
            Wyvern.DISCORD_BOT.sendMessageInGuild(discordMsg);
        }
    }
}
