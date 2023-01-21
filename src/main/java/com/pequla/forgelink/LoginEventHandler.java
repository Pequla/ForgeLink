package com.pequla.forgelink;

import com.mojang.logging.LogUtils;
import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.utils.WebClient;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class LoginEventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<UUID, DataModel> playerData = new HashMap<>();
    private final ForgeLink mod;

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        try {
            WebClient client = WebClient.getInstance();
            DataModel model = client.getPlayerData(player.getUUID());
            playerData.put(player.getUUID(), model);
            mod.sendMessage(playerCountFormatter(player, true));
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
        mod.sendMessage(playerCountFormatter(player, false));
        playerData.remove(player.getUUID());
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            mod.sendMessage(generatePlayerName(player) + "died");
        }
    }

    private String playerCountFormatter(Player player, boolean join) {
        String base = generatePlayerName(player) + ((join) ? "joined" : "left") + " the game";
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

    private String generatePlayerName(Player player) {
        String name = player.getName().getString();
        String tag = playerData.get(player.getUUID()).getName();
        return "**" + name + " (" + tag + ")** ";
    }
}
