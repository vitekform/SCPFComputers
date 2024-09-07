package cz.vitekform.computers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import cz.vitekform.computers.cmds.registerBlockAsComputer;
import de.tr7zw.nbtapi.NBT;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Computers extends JavaPlugin {

    public MongoClient mongoClient;
    public MongoDatabase database;
    private MongoCollection<Document> collection;

    @Override
    public void onEnable() {
        Bukkit.getLogger().info(ChatColor.YELLOW + "Probíhá inicializace počítačů...");
        if (!NBT.preloadApi()) {
            getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("register_pc").setExecutor(new registerBlockAsComputer());
        Bukkit.getPluginManager().registerEvents(new handlers(), this);
        super.onEnable();
        Bukkit.getLogger().info(ChatColor.GREEN + "Veškerá inicializace dokončena!");
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatColor.YELLOW + "Probíhá vypínání počítačů...");
        super.onDisable();
        Bukkit.getLogger().info(ChatColor.RED + "Vypínání dokončeno!");
    }

    public void syncDB() {
        collection = database.getCollection("computers");
    }

    public MongoCollection<Document> getCollection() {
        syncDB();
        return collection;
    }
}
