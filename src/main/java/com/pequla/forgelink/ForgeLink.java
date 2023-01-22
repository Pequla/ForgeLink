package com.pequla.forgelink;

import com.pequla.forgelink.dto.WebhookModel;
import com.pequla.forgelink.utils.WebService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("forgelink")
public class ForgeLink {

    public ForgeLink() {
        // Registering event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }

    private void sendSystemWebhook(String s) {
        WebService.getInstance().sendWebhook(WebhookModel.builder().content(s).build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        sendSystemWebhook("Server started");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Do something when the server stops
        sendSystemWebhook("Server stopped");
    }
}
