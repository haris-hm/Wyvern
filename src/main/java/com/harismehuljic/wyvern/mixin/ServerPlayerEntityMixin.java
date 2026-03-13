package com.harismehuljic.wyvern.mixin;

import com.harismehuljic.wyvern.Wyvern;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("HEAD"), method = "sendMessageToClient")
    private void onSendMessageToClient(Text message, boolean overlay, CallbackInfo ci) {
        Wyvern.LOGGER.info("Client-bound message: {}", message.toString());
//        String discordMsg = MessageFormatter.formatDiscordMessage(this.player, message.getSignedContent());
//
//        if (Wyvern.CONFIG_DATA.allowChatMessages()) {
//            Wyvern.DISCORD_BOT.sendMessageInGuild(discordMsg);
//        }
    }
}
