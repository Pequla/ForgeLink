package com.pequla.forgelink;

import com.mojang.logging.LogUtils;
import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.dto.WebhookModel;
import com.pequla.forgelink.utils.WebService;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<UUID, DataModel> playerData = new HashMap<>();

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        try {
            WebService client = WebService.getInstance();
            DataModel model = client.getPlayerData(player.getUUID());
            playerData.put(player.getUUID(), model);
            sendPlayerWebhook(player, playerCountFormatter(player, true));
            LOGGER.info(player.getName() + " joined as " + model.getNickname() + "(ID: " + model.getId() + ")");
        } catch (Exception e) {
            LOGGER.error(player.getName() + " login was rejected: " + e.getMessage(), e);
            ServerPlayer sp = (ServerPlayer) event.getPlayer();
            sp.connection.disconnect(new TextComponent("You are not whitelisted"));
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();
        LOGGER.info("Dispatching leave message");
        sendPlayerWebhook(player, playerCountFormatter(player, false));
        playerData.remove(player.getUUID());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            LOGGER.info("Dispatching death message");
            sendPlayerWebhook(player, "died");
        }
    }

    private void sendPlayerWebhook(Player player, String content) {
        String name = player.getName().getString();
        UUID uuid = player.getUUID();
        String tag = playerData.get(uuid).getName();
        WebService service = WebService.getInstance();
        service.sendWebhook(WebhookModel.builder()
                .avatar_url("https://visage.surgeplay.com/face/" + service.cleanUUID(uuid))
                .username(name)
                .content("**" + name + " (" + tag + ")** " + content)
                .build());
    }

    private String playerCountFormatter(Player player, boolean join) {
        String base = ((join) ? "joined" : "left") + " the game";
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
