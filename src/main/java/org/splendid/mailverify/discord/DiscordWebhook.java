package org.splendid.mailverify.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordWebhook {

    private final String webhookUrl;
    private final JavaPlugin plugin;
    private WebhookClient client;

    public DiscordWebhook(String webhookUrl, JavaPlugin plugin) {
        this.webhookUrl = webhookUrl;
        this.plugin = plugin;
    }

    public void sendDiscordMessage(String playerName, String email, String ipAddress) {
        String message = String.format("**E-Posta Doğrulama**\n \nOyuncu: %s\nE-posta: **%s**\nIP Adresi: %s", playerName, email, ipAddress);

        try {
            if (client == null) {
                client = new WebhookClientBuilder(webhookUrl).build();
            }

            Player player = Bukkit.getPlayer(playerName);
            assert player != null;
            String headImageUrl = String.format("https://crafatar.com/avatars/%s?overlay", player.getUniqueId());
            WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder()
                    .setColor(7185242)
                    .setDescription(message)
                    .setAuthor(new WebhookEmbed.EmbedAuthor(playerName, null, null))
                    .setThumbnailUrl(headImageUrl);

            WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder().addEmbeds(embedBuilder.build());
            client.send(messageBuilder.build());

            plugin.getLogger().info("Discord mesajı başarıyla gönderildi.");
        } catch (Exception e) {
            plugin.getLogger().warning("Discord mesajı gönderilirken bir hata oluştu: " + e.getMessage());
        }
    }
}
