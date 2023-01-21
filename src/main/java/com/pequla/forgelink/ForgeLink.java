package com.pequla.forgelink;

import com.pequla.forgelink.dto.WebhookModel;
import com.pequla.forgelink.utils.WebClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod("forgelink")
public class ForgeLink {

    private final WebClient client = WebClient.getInstance();

    public ForgeLink() {
        // Registering event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new LoginEventHandler(this));
    }

    public void sendMessage(String s) {
        client.sendWebhook(WebhookModel.builder().content(s).build());
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        sendMessage("Server loading");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        sendMessage("Server started");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Do something when the server stops
        sendMessage("Server stopped");
    }
}
