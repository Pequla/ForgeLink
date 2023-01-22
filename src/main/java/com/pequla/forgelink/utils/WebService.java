package com.pequla.forgelink.utils;

import com.google.gson.Gson;
import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.dto.ErrorModel;
import com.pequla.forgelink.dto.WebhookModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class WebService {

    private static WebService instance;
    private final HttpClient client;
    private final Gson gson;

    public WebService() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.gson = new Gson();
    }

    public static WebService getInstance() {
        if (instance == null) instance = new WebService();
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

    public void sendWebhook(WebhookModel model) {
        //82.208.22.205:3300
        String webhook = ConfigService.getInstance().getWebhookUrl();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://82.208.22.205:3300/api/webhook"))
                .header("Content-Type", "application/json")
                .header("webhook", webhook)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(model)))
                .build();
        client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
    }

    public String cleanUUID(UUID uuid) {
        return uuid.toString().replace("-", "");
    }
}
