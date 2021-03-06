package de.craftlancer.equipcontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EquipControl extends JavaPlugin implements Listener
{
    FileConfiguration config;
    List<Integer> armor = new ArrayList<Integer>();
    List<Integer> weapon = new ArrayList<Integer>();
    HashMap<Integer, HashMap<String, String>> weaponnew = new HashMap<Integer, HashMap<String, String>>();
    HashMap<Integer, HashMap<String, String>> armornew = new HashMap<Integer, HashMap<String, String>>();
    String nopermweap;
    String nopermhelmet;
    String nopermchestplate;
    String nopermleggings;
    String nopermboots;
    String nopermnamedarmor;
    String nopermnamedweap;
    boolean checkarmorondmg = false;
    boolean armorschedule = false;
    boolean useItemName = true;
    long timer = 600;
    Logger log = Logger.getLogger("Minecraft");
    
    @Override
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        config = getConfig();
        
        if (!new File(this.getDataFolder(), "config.yml").exists())
            saveDefaultConfig();
        
        loadConfig();
        
        if (armorschedule)
            getServer().getScheduler().runTaskTimer(this, new Runnable()
            {
                @Override
                public void run()
                {
                    for (Player p : getServer().getOnlinePlayers())
                        checkArmor(p);
                }
            }, timer, timer);
    }
    
    @Override
    public void onDisable()
    {
        config = null;
        armor = null;
        weapon = null;
        armornew = null;
        weaponnew = null;
        
        getServer().getScheduler().cancelTasks(this);
    }
    
    private void loadConfig()
    {
        String w = "checked_weapons";
        String a = "checked_armor";
        
        armor = config.getIntegerList(a + ".list");
        weapon = config.getIntegerList(w + ".list");
        
        for (String key : config.getConfigurationSection(w).getKeys(false))
            try
            {
                int id = Integer.parseInt(key);
                
                HashMap<String, String> helpmap = new HashMap<String, String>();
                
                for (String k : config.getConfigurationSection(w + "." + key).getKeys(false))
                    helpmap.put(config.getString(w + "." + key + "." + k), k);
                
                weaponnew.put(id, helpmap);
            }
            catch (NumberFormatException e)
            {
                log.warning("Invaild ItemID: " + key);
            }
        
        for (String key : config.getConfigurationSection(a).getKeys(false))
            try
            {
                int id = Integer.parseInt(key);
                
                HashMap<String, String> helpmap = new HashMap<String, String>();
                
                for (String k : config.getConfigurationSection(a + "." + key).getKeys(false))
                    helpmap.put(config.getString(a + "." + key + "." + k), k);
                
                armornew.put(id, helpmap);
            }
            catch (NumberFormatException e)
            {
                log.warning("Invaild ItemID: " + key);
            }
        
        nopermweap = setColored(config.getString("string.weapon", "You don't have the needed permissions to use this weapon"));
        nopermhelmet = setColored(config.getString("string.helmet", "You don't have the needed permissions to wear this helmet"));
        nopermchestplate = setColored(config.getString("string.chest", "You don't have the needed permissions to wear this chestplate"));
        nopermleggings = setColored(config.getString("string.leggings", "You don't have the needed permissions to wear this leggings"));
        nopermboots = setColored(config.getString("string.boots", "You don't have the needed permissions to wear this boots"));
        nopermnamedweap = setColored(config.getString("string.namedweapon", "You don't have the needed permissions to use %item%"));
        nopermnamedarmor = setColored(config.getString("string.namedarmor", "You don't have the needed permissions to wear %item%"));
        
        checkarmorondmg = config.getBoolean("CheckArmorOnDamage", false);
        armorschedule = config.getBoolean("CheckArmorPeriodical", false);
        timer = config.getLong("CheckArmorTimer", 30) * 20;
        useItemName = config.getBoolean("useItemName", false);
    }
    
    @EventHandler
    public void onInventory(InventoryCloseEvent event)
    {
        if (event.getView().getType() == InventoryType.CRAFTING)
            checkArmor((Player) event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        checkArmor(event.getPlayer());
    }
    
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (checkarmorondmg && event.getEntity() instanceof Player)
            checkArmor((Player) event.getEntity());
        
        if (event.getDamager() instanceof Player)
            event.setCancelled(checkWeapon((Player) event.getDamager()));
        else if (event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof Player)
            event.setCancelled(checkWeapon((Player) ((Arrow) event.getDamager()).getShooter()));
    }
    
    @EventHandler
    public void onRightClick(final PlayerInteractEvent e)
    {
        if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e.getAction().equals(Action.RIGHT_CLICK_AIR)))
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    checkArmor(e.getPlayer());
                }
            }.runTaskLater(this, 0);        
    }
    
    /**
     * check if a player uses a disallowed armor
     * 
     * @param player
     *            the checked player
     */
    protected void checkArmor(Player player)
    {
        PlayerInventory pinv = player.getInventory();
        ItemStack[] ac = pinv.getArmorContents();
        
        for (int i = 0; i < ac.length; i++)
        {
            ItemStack item = ac[i];
            
            if (item != null && (armornew.containsKey(item.getTypeId()) || (armor != null && armor.contains(item.getTypeId()))))
            {
                String s = getArmorExtraPerm(item);
                if (!player.hasPermission("equipcontrol.armor." + item.getTypeId() + s) && !player.hasPermission("equipcontrol.armor." + item.getType().name() + s))
                {
                    if (pinv.firstEmpty() >= 0)
                        pinv.addItem(item);
                    else
                        player.getWorld().dropItem(player.getLocation(), item);
                    
                    ac[i] = new ItemStack(0);
                    
                    if (!s.equalsIgnoreCase(""))
                        player.sendMessage(nopermnamedarmor.replace("%item%", item.getItemMeta().getDisplayName()));
                    else
                        player.sendMessage(getSlotMessage(i));
                }
            }
        }
        
        pinv.setArmorContents(ac);
    }
    
    /**
     * check if a player uses a disallowed weapon
     * 
     * @param player
     *            the checked player
     * @return true when he is allowed to, false when not
     */
    public boolean checkWeapon(Player player)
    {
        ItemStack item = player.getItemInHand();
        String s = getWeaponExtraPerm(item);
        
        if (weaponnew.containsKey(item.getTypeId()) || (weapon != null && weapon.contains(item.getTypeId())))
            if (!player.hasPermission("equipcontrol.weapon." + item.getTypeId() + s) && !player.hasPermission("equipcontrol.weapon." + item.getType().name() + s))
            {
                if (!s.equalsIgnoreCase(""))
                    player.sendMessage(nopermnamedweap.replace("%item%", item.getItemMeta().getDisplayName()));
                else
                    player.sendMessage(nopermweap);
                
                return true;
            }
        
        return false;
    }
    
    private String getArmorExtraPerm(ItemStack i)
    {
        return (i.hasItemMeta() && i.getItemMeta().hasDisplayName() && armornew.containsKey(i.getTypeId()) && armornew.get(i.getTypeId()).containsKey(i.getItemMeta().getDisplayName())) ? "." + armornew.get(i.getTypeId()).get(i.getItemMeta().getDisplayName()) : "";
    }
    
    private String getWeaponExtraPerm(ItemStack i)
    {
        return (i.hasItemMeta() && i.getItemMeta().hasDisplayName() && weaponnew.containsKey(i.getTypeId()) && weaponnew.get(i.getTypeId()).containsKey(i.getItemMeta().getDisplayName())) ? "." + weaponnew.get(i.getTypeId()).get(i.getItemMeta().getDisplayName()) : "";
    }
    
    private String getSlotMessage(int i)
    {
        switch (i)
        {
            case 0:
                return nopermboots;
            case 1:
                return nopermleggings;
            case 2:
                return nopermchestplate;
            case 3:
                return nopermhelmet;
            default:
                return "ERROR, WRONG SLOT NUMBER";
        }
    }
    
    public static String setColored(String string)
    {
        string = string.replace("&0", ChatColor.BLACK.toString());
        string = string.replace("&1", ChatColor.DARK_BLUE.toString());
        string = string.replace("&2", ChatColor.DARK_GREEN.toString());
        string = string.replace("&3", ChatColor.DARK_AQUA.toString());
        string = string.replace("&4", ChatColor.DARK_RED.toString());
        string = string.replace("&5", ChatColor.DARK_PURPLE.toString());
        string = string.replace("&6", ChatColor.GOLD.toString());
        string = string.replace("&7", ChatColor.GRAY.toString());
        string = string.replace("&8", ChatColor.DARK_GRAY.toString());
        string = string.replace("&9", ChatColor.BLUE.toString());
        string = string.replace("&a", ChatColor.GREEN.toString());
        string = string.replace("&b", ChatColor.AQUA.toString());
        string = string.replace("&c", ChatColor.RED.toString());
        string = string.replace("&d", ChatColor.LIGHT_PURPLE.toString());
        string = string.replace("&e", ChatColor.YELLOW.toString());
        string = string.replace("&f", ChatColor.WHITE.toString());
        string = string.replace("&k", ChatColor.MAGIC.toString());
        string = string.replace("&l", ChatColor.BOLD.toString());
        string = string.replace("&m", ChatColor.STRIKETHROUGH.toString());
        string = string.replace("&n", ChatColor.UNDERLINE.toString());
        string = string.replace("&o", ChatColor.ITALIC.toString());
        string = string.replace("&r", ChatColor.RESET.toString());
        return string;
    }
}
