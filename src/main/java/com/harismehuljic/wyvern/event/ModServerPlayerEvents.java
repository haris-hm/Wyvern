package com.harismehuljic.wyvern.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ModServerPlayerEvents {
    public static class ServerPlayerJoinEvent implements ServerPlayerEvents.Join {

        @Override
        public void onJoin(ServerPlayerEntity player) {
            
        }
    }

    public static class ServerPlayerLeaveEvent implements ServerPlayerEvents.Leave {

        @Override
        public void onLeave(ServerPlayerEntity player) {

        }
    }
}
