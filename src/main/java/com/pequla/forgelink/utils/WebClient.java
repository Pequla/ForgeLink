package com.pequla.forgelink.utils;

import com.google.gson.Gson;
import com.pequla.forgelink.dto.DataModel;
import com.pequla.forgelink.dto.ErrorModel;
import com.pequla.forgelink.dto.WebhookMessage;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

public class WebClient {

    private static WebClient instance;
    private final HttpClient client;
    private final Gson gson;
    private String guildId;
    private String webhookUrl;

    public WebClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.gson = new Gson();

        // Reading properties
        File config = new File("discord.properties");
        Properties prop = new Properties();
        if (!config.exists()) {
            try (OutputStream output = new FileOutputStream(config)) {
                prop.setProperty("discord.guild", "264801645370671114");
                prop.setProperty("discord.webhook", "https://discord.com/webhook");
                prop.store(output, "ForgeLink configuration file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (InputStream input = new FileInputStream(config)) {
            prop.load(input);
            this.guildId = prop.getProperty("discord.guild");
            this.webhookUrl = prop.getProperty("discord.webhook");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static WebClient getInstance() {
        if (instance == null) instance = new WebClient();
        return instance;
    }

    public void sendWebhookMessage(WebhookMessage message) {
        // When webhook url is not present skip method
        if (webhookUrl == null) return;

        // Send webhook
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(message)))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(rsp -> {
                });
    }

    public void sendWebhookMessage(DataModel model, String content) {
        WebhookMessage message = new WebhookMessage();
        message.setContent(content);
        message.setUsername(model.getNickname());
        message.setAvatar_url(model.getAvatar());
        sendWebhookMessage(message);
    }

    public void sendWebhookMessage(String content) {
        WebhookMessage message = new WebhookMessage();
        message.setContent(content);
        sendWebhookMessage(message);
    }

    public DataModel getPlayerData(UUID uuid) throws IOException, InterruptedException {
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
