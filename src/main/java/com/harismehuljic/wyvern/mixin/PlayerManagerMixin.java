package com.harismehuljic.wyvern.mixin;

import com.harismehuljic.wyvern.Wyvern;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(at = @At("HEAD"), method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V")
    private void onBroadcastMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
        Objects.requireNonNull(sender.getDisplayName());

        String messageContents = message.getSignedContent();
        String discordMsg;

        String playerName = String.format("**%s**", sender.getDisplayName().getString());
        String realName = sender.getGameProfile().getName();

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
