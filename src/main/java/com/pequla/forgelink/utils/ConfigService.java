package com.pequla.forgelink.utils;

import com.mojang.logging.LogUtils;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.*;
import java.util.Properties;

@Getter
public class ConfigService {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static ConfigService instance;
    private String token;
    private String guildId;
    private String channelId;
    private int systemColor;
    private int joinColor;
    private int leaveColor;
    private int deathColor;
    private int advancementColor;

    private ConfigService() {
        File config = new File("discord.properties");
        Properties prop = new Properties();
        if (!config.exists()) {
            try (OutputStream output = new FileOutputStream(config)) {
                prop.setProperty("discord.token", "<bot-token-goes-here>");
                prop.setProperty("discord.guild", "797899107124510731");
                prop.setProperty("discord.channel", "849391102541037598");
                prop.setProperty("color.system", "65535");
                prop.setProperty("color.join", "65280");
                prop.setProperty("color.leave", "16711680");
                prop.setProperty("color.death", "8388736");
                prop.setProperty("color.advancement", "65535");
                prop.store(output, "ForgeLink configuration file");
            } catch (IOException e) {
                LOGGER.error("Failed to generate the configuration file");
                LOGGER.error(e.getMessage(), e);
            }
        }
        try (InputStream input = new FileInputStream(config)) {
            prop.load(input);
            this.token = prop.getProperty("discord.token");
            this.guildId = prop.getProperty("discord.guild");
            this.channelId = prop.getProperty("discord.channel");
            this.systemColor = Integer.parseInt(prop.getProperty("color.system"));
            this.joinColor = Integer.parseInt(prop.getProperty("color.join"));
            this.leaveColor = Integer.parseInt(prop.getProperty("color.leave"));
            this.deathColor = Integer.parseInt(prop.getProperty("color.death"));
            this.advancementColor = Integer.parseInt(prop.getProperty("color.advancement"));
        } catch (IOException e) {
            LOGGER.error("Failed to read properties");
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static ConfigService getInstance() {
        if (instance == null) instance = new ConfigService();
        return instance;
    }
}
