package cz.vitekform.computers.cmds;

import cz.vitekform.computers.Computers;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class registerBlockAsComputer implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p) {
            if (sender.hasPermission(new Permission("computers.register_new", "Permission to register new computer"))) {
                Block targetBlock = p.getTargetBlock(null, 10);
                if (targetBlock.getType().equals(Material.AIR)) {
                    p.sendMessage(ChatColor.RED + "Block na který míříte musí být normální Solid Block!");
                }
                else {
                    World w = targetBlock.getLocation().getWorld();

                    Plugin plugin = Computers.getProvidingPlugin(Computers.class);
                    NamespacedKey key = new NamespacedKey(plugin, "computers.registered_blocks");

                    PersistentDataContainer pdc = w.getPersistentDataContainer();
                    String currentContent = "";
                    if (pdc.has(key, PersistentDataType.STRING)) {
                        currentContent = pdc.get(key, PersistentDataType.STRING);
                    }
                    int x = targetBlock.getX();
                    int y = targetBlock.getY();
                    int z = targetBlock.getZ();

                    String composedString = x + "." + y + "." + z;
                    if (currentContent.contains(composedString)) {
                        p.sendMessage(Component.text(ChatColor.RED + "Tento blok už je registrován jako počítač!"));
                    }
                    else {
                        currentContent += (composedString + " ");
                        pdc.set(key, PersistentDataType.STRING, currentContent);

                        p.sendMessage(Component.text(ChatColor.GREEN + "Počítač na souřadnicích " + composedString + " byl zaregistrován!"));
                    }
                }
            }
            else {
                p.sendMessage(Component.text(ChatColor.RED + "Na toto nemáš oprávnění!"));
            }
        }
        else {
            Bukkit.getLogger().info(ChatColor.RED + "Tento příkaz může použít pouze hráč!");
        }
        return false;
    }
}
