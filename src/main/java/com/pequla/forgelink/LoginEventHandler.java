package com.pequla.forgelink;

import com.mojang.logging.LogUtils;
import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.utils.WebClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<UUID, DataModel> playerData = new HashMap<>();

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        try {
            WebClient client = WebClient.getInstance();
            DataModel model = client.getPlayerData(player.getUUID());
            client.sendWebhookMessage(model, playerCountFormatter(player, true));

            // Caching data
            playerData.put(player.getUUID(), model);
            LOGGER.info(model.toString());
        } catch (Exception e) {
            LOGGER.error(player.getName() + " login was rejected: " + e.getMessage(), e);
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();
        DataModel model = playerData.get(player.getUUID());
        WebClient.getInstance().sendWebhookMessage(model, playerCountFormatter(player, false));
        playerData.remove(player.getUUID());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            DataModel model = playerData.get(player.getUUID());
            WebClient.getInstance().sendWebhookMessage(model, "**"+player.getName().getString() + "** died");
        }
    }

    private String playerCountFormatter(Player player, boolean join) {
        String name = player.getName().getString();
        String base = "**" + name + "** " + ((join) ? "joined" : "left") + " the game";
        MinecraftServer server = player.getServer();
        if (server != null) {
            PlayerList list = server.getPlayerList();
            int count = list.getPlayerCount();
            if (!join) count = count - 1;
            if (count == 0) {
                return base + ", server is **empty**";
            }
            return base + ", **" + count + "** out of **" + list.getMaxPlayers() + "** online";
        }
        return base;
    }

}
