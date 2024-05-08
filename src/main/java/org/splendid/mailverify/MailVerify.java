package org.splendid.mailverify;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MailVerify extends JavaPlugin implements Listener {

    private Connection connection;
    private String host, database, port, username, password, table;
    private String successMessage, usageMessage, playerOnlyMessage, emailNotVerifiedWarning, kickMessage;
    private int kickDelayMinutes;

    private Map<UUID, Boolean> warnedPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        loadConfig();
        connectToDatabase();
        createTableIfNotExists();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MailVerify eklentisi başarıyla etkinleştirildi.");
    }

    @Override
    public void onDisable() {
        disconnectFromDatabase();
        getLogger().info("MailVerify eklentisi devre dışı bırakıldı.");
    }

    private void loadConfig() {
        host = getConfig().getString("mysql.host");
        port = getConfig().getString("mysql.port");
        database = getConfig().getString("mysql.database");
        username = getConfig().getString("mysql.username");
        password = getConfig().getString("mysql.password");
        table = getConfig().getString("mysql.table");
        successMessage = colorize(getConfig().getString("messages.success_message"));
        usageMessage = colorize(getConfig().getString("messages.usage_message"));
        playerOnlyMessage = colorize(getConfig().getString("messages.player_only_message"));
        emailNotVerifiedWarning = colorize(getConfig().getString("messages.email_not_verified_warning"));
        kickMessage = colorize(getConfig().getString("messages.kick_message"));
        kickDelayMinutes = getConfig().getInt("kick_delay_minutes");
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private void connectToDatabase() {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        try {
            connection = DriverManager.getConnection(url, username, password);
            getLogger().info("Veritabanına başarıyla bağlanıldı." + host + ":" + port + "/" + database);
        } catch (SQLException e) {
            getLogger().warning("Veritabanına bağlanırken bir hata oluştu: " + e.getMessage());  //veri tabanına bağlanmaz ise bu hatayı vericektir
        }
    }

    private void disconnectFromDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                getLogger().info("Veritabanı bağlantısı kapatıldı." + host + ":" + port + "/" + database);
            }
        } catch (SQLException e) {
            getLogger().warning("Veritabanı bağlantısı kapatılırken bir hata oluştu: " + e.getMessage());
        }
    }

    private void createTableIfNotExists() {
        try {
            String query = "CREATE TABLE IF NOT EXISTS " + table + " (uuid VARCHAR(36) PRIMARY KEY, username VARCHAR(16), email VARCHAR(255), ip_address VARCHAR(45))";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
            statement.close();
            getLogger().info("Tablo kontrol edildi ve varsa oluşturuldu: " + table);
        } catch (SQLException e) {
            getLogger().warning("Tablo oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("eposta")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length != 1) {
                    player.sendMessage(usageMessage);
                    return false;
                }
                String email = args[0];
                String ipAddress = player.getAddress().getAddress().getHostAddress();
                saveEmail(player.getUniqueId(), player.getName(), email, ipAddress);
                player.sendMessage(successMessage);
            } else {
                sender.sendMessage(playerOnlyMessage);
            }
            return true;
        }
        return false;
    }

    private void saveEmail(UUID uuid, String username, String email, String ipAddress) {
        try {
            if (isEmailExists(uuid)) {
                getLogger().warning("E-posta zaten kayıtlı.");
                return;
            }

            String query = "INSERT INTO " + table + " (uuid, username, email, ip_address) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uuid.toString());
            statement.setString(2, username);
            statement.setString(3, email);
            statement.setString(4, ipAddress);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            getLogger().warning("E-posta kaydedilirken bir hata oluştu: " + e.getMessage());
        }
    }

    private boolean isEmailExists(UUID uuid) {
        try {
            String query = "SELECT * FROM " + table + " WHERE uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            boolean exists = resultSet.next();
            statement.close();
            return exists;
        } catch (SQLException e) {
            getLogger().warning("E-posta kontrol edilirken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!warnedPlayers.containsKey(uuid) && !isEmailVerified(uuid)) {
            warnedPlayers.put(uuid, true);
            player.sendMessage(emailNotVerifiedWarning);
            BukkitTask kickTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isEmailVerified(uuid)) {
                        player.kickPlayer(kickMessage);
                    }
                    warnedPlayers.remove(uuid);
                }
            }.runTaskLater(this, kickDelayMinutes * 1200L); // 1200 ticks = 1 dakika
        }
    }

    private boolean isEmailVerified(UUID uuid) {
        try {
            String query = "SELECT * FROM " + table + " WHERE uuid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            boolean emailVerified = resultSet.next();
            statement.close();
            return emailVerified;
        } catch (SQLException e) {
            getLogger().warning("E-posta doğrulanırken bir hata oluştu: " + e.getMessage());
            return false;
        }
    }
}
