package com.pequla.forgelink;

import com.pequla.forgelink.utils.ConfigService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.security.auth.login.LoginException;
import java.time.Instant;

@Mod("forgelink")
public class ForgeLink {

    private final ConfigService config = ConfigService.getInstance();
    private JDA jda;

    public ForgeLink() {
        try {
            this.jda = JDABuilder.createDefault(config.getToken())
                    .setActivity(Activity.playing("Modded Minecraft"))
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .build();
        } catch (LoginException e) {
            e.printStackTrace();
        }

        // Registering event handlers
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new LoginEventHandler(this));
    }

    public void sendEmbed(EmbedBuilder builder) {
        TextChannel channel = jda.getTextChannelById(config.getChannelId());
        if (channel != null) {
            channel.sendMessageEmbeds(builder.setTimestamp(Instant.now()).build()).queue();
        }
    }

    public void sendSystemEmbed(String text) {
        sendEmbed(new EmbedBuilder()
                .setColor(config.getSystemColor())
                .setDescription(MarkdownUtil.bold(text)));
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
        sendSystemEmbed("Server loading");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        sendSystemEmbed("Server started");
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        // Do something when the server stops
        sendSystemEmbed("Server stopped");
    }
}
