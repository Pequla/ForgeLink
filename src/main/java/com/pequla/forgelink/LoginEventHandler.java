package com.pequla.forgelink;

import com.mojang.logging.LogUtils;
import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.utils.WebClient;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class LoginEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<UUID, DataModel> playerData = new HashMap<>();
    private final ForgeLink mod;

    // TODO: migrate class from webhooks to discord bot
    // Create util method for sending player join/leave info
    // Update discord bot activity based on online players

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        try {
            WebClient client = WebClient.getInstance();
            DataModel model = client.getPlayerData(player.getUUID());
            LOGGER.info("Dispatching join message");
            mod.sendSystemEmbed(playerCountFormatter(player, true));

            // Caching data
            playerData.put(player.getUUID(), model);
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
        DataModel model = playerData.get(player.getUUID());
        LOGGER.info("Dispatching leave message");
        mod.sendSystemEmbed(playerCountFormatter(player, false));
        playerData.remove(player.getUUID());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            DataModel model = playerData.get(player.getUUID());
            mod.sendSystemEmbed("**" + player.getName().getString() + "** died");
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
