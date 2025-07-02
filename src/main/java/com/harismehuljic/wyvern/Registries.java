package com.harismehuljic.wyvern;

import com.harismehuljic.wyvern.event.AllowChatMessageEvent;
import com.harismehuljic.wyvern.event.GameMessageEvent;
import com.harismehuljic.wyvern.event.ModServerLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Registries {
    public static void registerAllRegistries() {
        registerCommands();
        registerEvents();
    }

    private static void registerCommands() {
    }

    private static void registerEvents() {
        // Server Message events
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(new AllowChatMessageEvent());
        ServerMessageEvents.GAME_MESSAGE.register(new GameMessageEvent());

        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(new ModServerLifecycleEvents.ServerStartingEvent());
        ServerLifecycleEvents.SERVER_STARTED.register(new ModServerLifecycleEvents.ServerStartedEvent());
        ServerLifecycleEvents.SERVER_STOPPING.register(new ModServerLifecycleEvents.ServerStoppingEvent());
        ServerLifecycleEvents.SERVER_STOPPED.register(new ModServerLifecycleEvents.ServerStoppedEvent());
    }
}
