package cz.vitekform.computers;

import cz.vitekform.computers.enums.ComputerStatus;
import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class handlers implements Listener {
    @EventHandler
    public void whenPlayerRightClicks(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            if (event.getClickedBlock() != null && !event.getClickedBlock().getType().equals(Material.AIR)) {
                ItemStack heldItem = event.getPlayer().getItemInHand();
                    int x = event.getClickedBlock().getX();
                    int y = event.getClickedBlock().getY();
                    int z = event.getClickedBlock().getZ();
                    String composed = x + "." + y + "." + z;

                    AtomicInteger level = new AtomicInteger(5); // This is placeholder
                    AtomicInteger override_ = new AtomicInteger(); // This is also a placeholder

                    if (heldItem == null || heldItem.isEmpty()) {
                        event.getPlayer().sendMessage(Component.text(ChatColor.RED + "Musíš držet kartu aby jsi se mohl přihlásit!"));
                        return;
                    }

                    NBT.get(heldItem, nbt -> {
                        override_.set(nbt.getOrDefault("override", 0));
                        level.set(nbt.getOrDefault("level", 0));
                    });

                    boolean override = override_.get() == 1;

                    Block clickedBlock = event.getClickedBlock();

                    World w = clickedBlock.getLocation().getWorld();
                    Plugin plugin = Computers.getProvidingPlugin(Computers.class);
                    NamespacedKey key = new NamespacedKey(plugin, "computers.registered_blocks");
                    NamespacedKey overrideKey = new NamespacedKey(plugin, "computers." + composed + ".perms.override");
                    NamespacedKey accessLevelKey = new NamespacedKey(plugin, "computers." + composed + ".perms.level");

                    PersistentDataContainer pdc = w.getPersistentDataContainer();

                    int level_int = level.intValue();

                    pdc.set(overrideKey, PersistentDataType.BOOLEAN, override);
                    pdc.set(accessLevelKey, PersistentDataType.INTEGER, level_int);

                    String currentContent = "";
                    if (pdc.has(key, PersistentDataType.STRING)) {
                        currentContent = pdc.get(key, PersistentDataType.STRING);
                    }

                    if (currentContent.contains(composed)) {
                        if (heldItem.getType().equals(Material.IRON_INGOT)) {
                            NamespacedKey key1 = new NamespacedKey(plugin, "computers.open_cooldown");
                            if (event.getPlayer().getPersistentDataContainer().has(key1)) {
                                return;
                            }
                            event.getPlayer().getPersistentDataContainer().set(key1, PersistentDataType.BOOLEAN, true);
                            openComputerMenu(event.getPlayer(), clickedBlock);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    event.getPlayer().getPersistentDataContainer().remove(key1);
                                }
                            }.runTaskLater(plugin, 20 *3);
                        }
                        else {
                            event.getPlayer().sendMessage(Component.text(ChatColor.RED + "Musíš držet kartu aby jsi se mohl přihlásit!"));
                        }
                    }
            }
        }
    }

    public void openComputerMenu(Player p, Block b) {
        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        String composed = x + "." + y + "." + z;

        Plugin plugin = Computers.getProvidingPlugin(Computers.class);

        NamespacedKey status_key = new NamespacedKey(plugin, "computers.pc." + composed + ".status");
        PersistentDataContainer pdc = b.getLocation().getWorld().getPersistentDataContainer();

        ComputerStatus status = ComputerStatus.OFFLINE;
        if (pdc.has(status_key)) {
            int status_code = pdc.get(status_key, PersistentDataType.INTEGER);
            if (status_code == -1) {
                status = ComputerStatus.BROKEN;
            }
            else if (status_code == 0) {
                status = ComputerStatus.STARTING;
            }
            else if (status_code == 1) {
                status = ComputerStatus.RUNNING;
            }
        }
        if (status == ComputerStatus.BROKEN) {
            p.sendMessage(Component.text(ChatColor.RED + "Tenhle počítač vypadá nějak rozbitě a nechce se spustit!"));
        }
        else if (status == ComputerStatus.STARTING) {
            p.sendMessage(Component.text(ChatColor.YELLOW + "Počítač se načítá..."));
        }
        else if (status == ComputerStatus.RUNNING) {
            openDesktopMenu(p, b);
        }
        else {
            startComputer(p, b);
        }
    }

    public void startComputer(Player p, Block b) {
        p.sendMessage(Component.text(ChatColor.YELLOW + "Probíhá spouštění SCPF.OS AlpineBear 1.5.6"));

        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        String composed = x + "." + y + "." + z;

        Plugin plugin = Computers.getProvidingPlugin(Computers.class);

        NamespacedKey status_key = new NamespacedKey(plugin, "computers.pc." + composed + ".status");
        PersistentDataContainer pdc = b.getLocation().getWorld().getPersistentDataContainer();


        Random rnd = new Random();
        long msTime = rnd.nextInt(90, 180);
        new BukkitRunnable() {
            @Override
            public void run() {
                pdc.set(status_key, PersistentDataType.INTEGER, 1);
                p.sendMessage(Component.text(ChatColor.GREEN + "Počítač byl načten!"));
            }
        }.runTaskLater(plugin, msTime);
    }

    public void openDesktopMenu(Player p, Block b) {
        Plugin plugin = Computers.getProvidingPlugin(Computers.class);
        World w = b.getWorld();

        int x = b.getX();
        int y = b.getY();
        int z = b.getZ();
        String composed = x + "." + y + "." + z;

        NamespacedKey overrideKey = new NamespacedKey(plugin, "computers." + composed + ".perms.override");
        NamespacedKey accessLevelKey = new NamespacedKey(plugin, "computers." + composed + ".perms.level");
        NamespacedKey locationKey = new NamespacedKey(plugin, "computers.id.location");

        PersistentDataContainer pdc = w.getPersistentDataContainer();

        boolean override = pdc.get(overrideKey, PersistentDataType.BOOLEAN);
        int accessLevel = pdc.get(accessLevelKey, PersistentDataType.INTEGER);

        p.closeInventory();

        Inventory gui = Bukkit.createInventory(p, 54, "SCPF.OS AlpineBear 1.5.6");

        ItemStack logout = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta logoutMeta = logout.getItemMeta();
        logoutMeta.setDisplayName(ChatColor.RED + "Ukončit relaci");
        List<String> logoutLore = new ArrayList<>();
        logoutLore.add(ChatColor.YELLOW + "Stisknutím tohoto tlačitka ukončite");
        logoutLore.add(ChatColor.YELLOW + "aktuální relaci tohoto počítače");

        PersistentDataContainer ipdc = logoutMeta.getPersistentDataContainer();
        NamespacedKey overrideKeyI = new NamespacedKey(plugin, "computers.id.override_perm");
        NamespacedKey accessLevelKeyI = new NamespacedKey(plugin, "computers.id.access_perm");

        ipdc.set(overrideKeyI, PersistentDataType.BOOLEAN, override);
        ipdc.set(accessLevelKeyI, PersistentDataType.INTEGER, accessLevel);
        ipdc.set(locationKey, PersistentDataType.STRING, composed);

        logoutMeta.setLore(logoutLore);
        logout.setItemMeta(logoutMeta);

        ItemStack nothing = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta nothingMeta = nothing.getItemMeta();
        nothingMeta.setDisplayName(ChatColor.BLACK + " ");
        nothing.setItemMeta(nothingMeta);

        ItemStack voiceIntercom = new ItemStack(Material.STICK);
        ItemMeta voiceIntercomMeta = voiceIntercom.getItemMeta();
        voiceIntercomMeta.setDisplayName(ChatColor.BLUE + "Voice Intercom");
        List<String> voiceIntercomLore = new ArrayList<>();
        voiceIntercomLore.add(ChatColor.BLUE + "Rozhlásí vámi napsanou zprávu po celé Site-22");
        voiceIntercomLore.add(ChatColor.RED + "Zneužití se trestá!");
        if (accessLevel >= 2 || override) {
            voiceIntercomLore.add(ChatColor.GREEN + "Úroveň přístupu: Dostatečná");
        }
        else {
            voiceIntercomLore.add(ChatColor.RED + "Úroveň přístupu: Nedostatečná");
        }
        voiceIntercomMeta.setLore(voiceIntercomLore);
        voiceIntercom.setItemMeta(voiceIntercomMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (i < 9) {
                gui.setItem(i, nothing);
            }
            else if (i == gui.getSize() - 1) {
                gui.setItem(i, logout);
            }
            else if (i == 9) {
                gui.setItem(i, voiceIntercom);
            }
        }

        p.openInventory(gui);
    }

    @EventHandler
    public void handleDesktop(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player p) {
            if (event.getView().getTitle().equalsIgnoreCase("SCPF.OS AlpineBear 1.5.6")) {
                ItemStack clickedItem = event.getCurrentItem();
                Plugin plugin = Computers.getProvidingPlugin(Computers.class);

                ItemStack logout = event.getClickedInventory().getItem(53);
                ItemMeta logoutMeta = logout.getItemMeta();

                PersistentDataContainer ipdc = logoutMeta.getPersistentDataContainer();
                NamespacedKey overrideKeyI = new NamespacedKey(plugin, "computers.id.override_perm");
                NamespacedKey accessLevelKeyI = new NamespacedKey(plugin, "computers.id.access_perm");
                NamespacedKey locationKey = new NamespacedKey(plugin, "computers.id.location");

                boolean override = ipdc.get(overrideKeyI, PersistentDataType.BOOLEAN);
                int accessLevel = ipdc.get(accessLevelKeyI, PersistentDataType.INTEGER);
                String locationS = ipdc.get(locationKey, PersistentDataType.STRING);
                String[] locationSParts = locationS.split("\\.");

                int x = Integer.parseInt(locationSParts[0]);
                int y = Integer.parseInt(locationSParts[1]);
                int z = Integer.parseInt(locationSParts[2]);

                if (clickedItem == null) {
                    return;
                }
                if (clickedItem.getType().equals(Material.STICK)) {
                    if (accessLevel >= 2 || override) {
                        NamespacedKey cooldownKey = new NamespacedKey(plugin, "computers.intercom.voice.cooldown");
                        if (p.getPersistentDataContainer().has(cooldownKey)) {
                            long now = new Date().getTime();
                            long to = p.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG);
                            if (to >= now) {
                                long msToExpire = to - now;
                                double seconds = (double) msToExpire / 1000;
                                p.closeInventory();
                                p.sendMessage(ChatColor.RED + "Voice Intercom nemůžeš ještě použít " + seconds + " sekund!");
                                return;
                            }
                            else {
                                p.getPersistentDataContainer().remove(cooldownKey);
                            }
                        }
                        p.closeInventory();
                        p.sendMessage(ChatColor.YELLOW + "Zadej zprávu která se odešle.\nPokud toto chceš zrušit napiš CANCEL");
                        NamespacedKey key = new NamespacedKey(plugin, "computers.intercom.voice.active");
                        p.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
                    }
                    else {
                        p.closeInventory();
                        p.sendMessage(ChatColor.RED + "Nemáš dostatečnou úroveň přístupu!");
                        p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
                    }
                }
                else if (clickedItem.getType().equals(Material.REDSTONE_BLOCK)) {
                    Block b = event.getWhoClicked().getWorld().getBlockAt(x, y, z);
                    NamespacedKey status_key = new NamespacedKey(plugin, "computers.pc." + locationS + ".status");
                    PersistentDataContainer pdc = b.getLocation().getWorld().getPersistentDataContainer();
                    pdc.remove(status_key);
                    p.closeInventory();
                    p.sendMessage(ChatColor.RED + "Relace ukončena!");
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void whenPlayerTypesInChat(PlayerChatEvent event) {
        Plugin plugin = Computers.getProvidingPlugin(Computers.class);
        NamespacedKey key = new NamespacedKey(plugin, "computers.intercom.voice.active");

        if (event.getPlayer().getPersistentDataContainer().has(key)) {
            if (event.getMessage().equals("CANCEL")) {
                event.setCancelled(true);
                event.getPlayer().getPersistentDataContainer().remove(key);
                event.getPlayer().sendMessage("Akce zrušena!");
            }
            ConsoleCommandSender ccs = Bukkit.getConsoleSender();
            String cmd = "intercom " + event.getMessage();
            Bukkit.dispatchCommand(ccs, cmd);
            event.setCancelled(true);
            event.getPlayer().getPersistentDataContainer().remove(key);
            NamespacedKey cooldownKey = new NamespacedKey(plugin, "computers.intercom.voice.cooldown");
            event.getPlayer().getPersistentDataContainer().set(cooldownKey, PersistentDataType.LONG, new Date().getTime() + 15 * 60 * 1000);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Zpráva byla úspěšně přehrána!");
        }
    }
}