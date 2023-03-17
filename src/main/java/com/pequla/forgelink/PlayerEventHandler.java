package com.pequla.forgelink;

import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.dto.WebhookModel;
import com.pequla.forgelink.utils.TextComponent;
import com.pequla.forgelink.utils.WebService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PlayerEventHandler {

    private static final Logger LOGGER = LogManager.getLogger(ForgeLink.class.getSimpleName());
    private final Map<String, DataModel> playerData = new HashMap<>();

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        try {
            WebService client = WebService.getInstance();
            DataModel model = client.getPlayerData(player.getStringUUID());
            playerData.put(player.getStringUUID(), model);
            sendPlayerWebhook(player, playerCountFormatter(player, true));
            LOGGER.info(player.getName() + " joined as " + model.getNickname() + " (ID: " + model.getId() + ")");
        } catch (Exception e) {
            LOGGER.error(player.getName() + " login was rejected: " + e.getMessage(), e);
            ServerPlayer sp = (ServerPlayer) player;
            sp.connection.disconnect(new TextComponent("You are not whitelisted").getAsComponent());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (playerData.containsKey(player.getStringUUID())) {
            LOGGER.info("Dispatching leave message");
            sendPlayerWebhook(player, playerCountFormatter(player, false));
            playerData.remove(player.getStringUUID());
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            if (playerData.containsKey(player.getStringUUID())) {
                LOGGER.info("Dispatching death message");
                String msg = event.getSource().getLocalizedDeathMessage(event.getEntity()).getString();
                sendPlayerWebhook(player, msg.replace(player.getName().getString() + " ", ""));
            }
        }
    }

    @SubscribeEvent
    public void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        Player player = event.getEntity();
        if (playerData.containsKey(player.getStringUUID())) {
            MinecraftServer server = event.getEntity().getServer();
            if (server != null && server.getPlayerList().getPlayerAdvancements((ServerPlayer) event.getEntity()).getOrStartProgress(event.getAdvancement()).isDone()) {
                if (event.getAdvancement() != null && event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceChat()) {
                    String title = event.getAdvancement().getDisplay().getTitle().getString();
                    String desc = event.getAdvancement().getDisplay().getDescription().getString();
                    LOGGER.info("Dispatching advancement message");
                    sendPlayerWebhook(player, "just made the advancement **" + title + "**"
                            + System.lineSeparator() + "*" + desc + "*");
                }
            }
        }
    }

    private void sendPlayerWebhook(Player player, String content) {
        String name = player.getName().getString();
        String uuid = player.getStringUUID();
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
