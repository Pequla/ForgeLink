package com.pequla.forgelink.utils;

import com.google.gson.Gson;
import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.dto.ErrorModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class WebClient {

    private static WebClient instance;
    private final HttpClient client;
    private final Gson gson;

    public WebClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.gson = new Gson();
    }

    public static WebClient getInstance() {
        if (instance == null) instance = new WebClient();
        return instance;
    }

    public DataModel getPlayerData(UUID uuid) throws IOException, InterruptedException {
        String guildId = ConfigService.getInstance().getGuildId();
        String url = "https://link.samifying.com/api/user/" + guildId + "/" + cleanUUID(uuid);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpResponse<String> rsp = client.send(req, HttpResponse.BodyHandlers.ofString());

        // Generating response based of status codes
        int code = rsp.statusCode();
        if (code == 200) {
            // Response is OK
            return gson.fromJson(rsp.body(), DataModel.class);
        }
        if (code == 500) {
            throw new RuntimeException(gson.fromJson(rsp.body(), ErrorModel.class).getMessage());
        }
        throw new RuntimeException("Response code " + code);
    }

    public String cleanUUID(UUID uuid) {
        return uuid.toString().replace("-", "");
    }
}
