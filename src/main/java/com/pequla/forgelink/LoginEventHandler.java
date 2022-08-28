package com.pequla.forgelink;

import com.google.gson.Gson;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginEventHandler {

    public static final String GUILD_ID = "264801645370671114";
    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    @SubscribeEvent
    public void login(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getStringUUID();

        try {
            URL url = new URL("https://link.samifying.com/api/user/" + GUILD_ID + "/" + uuid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if (status != 200) rejectPlayerLogin(player, event);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            Gson gson = new Gson();
            PlayerData data = gson.fromJson(in.readLine(), PlayerData.class);
            playerData.put(player.getUUID(), data);
            System.out.println(data);
        } catch (Exception e) {
            rejectPlayerLogin(player, event);
        }
    }

    @SubscribeEvent
    public void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getPlayer().getUUID();
        playerData.remove(uuid);
    }

    private void rejectPlayerLogin(Player player, PlayerEvent.PlayerLoggedInEvent event) {
        System.out.println("PLAYER " + player.getName() + " LOGIN WAS REJECTED");
        event.setResult(Event.Result.DENY);
    }

}
