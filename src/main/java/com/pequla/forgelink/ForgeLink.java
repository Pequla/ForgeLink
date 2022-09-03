package com.pequla.forgelink;

import com.pequla.forgelink.utils.WebClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("forgelink")
public class ForgeLink {

    public ForgeLink() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new LoginEventHandler());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        WebClient.getInstance().sendWebhookMessage("**Server started**");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Do something when the server stops
        WebClient.getInstance().sendWebhookMessage("**Server stopped**");
    }
}
