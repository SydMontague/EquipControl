package de.syd.equipcontrol;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class EquipControl extends JavaPlugin implements Listener
{
    FileConfiguration config;
    List<Integer> armor;
    List<Integer> weapon;
    String nopermweap;
    String nopermhelmet;  
    String nopermchestplate;   
    String nopermleggings;   
    String nopermboots;     
    Logger log = Logger.getLogger("Minecraft");
    
    public void onEnable()
    {
        getServer().getPluginManager().registerEvents(this, this);
        config = getConfig();
        if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
            saveDefaultConfig();
        weapon = config.getIntegerList("checked_weapons");
        armor = config.getIntegerList("checked_armor");
        nopermweap = config.getString("string.weapon", "You don't have the needed permissions to use this weapon");
        nopermhelmet = config.getString("string.helmet", "You don't have the needed permissions to wear this helemet");
        nopermchestplate = config.getString("string.chest", "You don't have the needed permissions to wear this chestplate");
        nopermleggings = config.getString("string.leggings", "You don't have the needed permissions to wear this leggings");
        nopermboots = config.getString("string.boots", "You don't have the needed permissions to wear this boots");
    
    }
    
    public void onDisable()
    {
        config = null;
        armor = null;
        weapon = null;
    }
    
    /**
     * Checks ArmorSlots for forbidden equipment on closing Inventory
     * 
     * @param event
     */
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
    
    /**
     * check if a player uses a unallowed armor
     * 
     * @param player
     *            - the checked player
     */
    private void checkArmor(Player player)
    {
        PlayerInventory pinv = player.getInventory();
        ItemStack helmet = pinv.getHelmet();
        ItemStack chestplate = pinv.getChestplate();
        ItemStack leggings = pinv.getLeggings();
        ItemStack boots = pinv.getBoots();
        
        if (helmet != null && armor.contains(helmet.getTypeId()))
            if (!player.hasPermission("equipcontrol.armor." + helmet.getTypeId()) && !player.hasPermission("equipcontrol.armor." + helmet.getType().name()))
            {
                if (pinv.firstEmpty() >= 0)
                    pinv.addItem(helmet);
                else
                    player.getWorld().dropItem(player.getLocation(), helmet);
                pinv.setHelmet(new ItemStack(0));
                player.sendMessage(nopermhelmet);
            }
        
        if (chestplate != null && armor.contains(chestplate.getTypeId()))
            if (!player.hasPermission("equipcontrol.armor." + chestplate.getTypeId()) && !player.hasPermission("equipcontrol.armor." + chestplate.getType().name()))
            {
                if (pinv.firstEmpty() >= 0)
                    pinv.addItem(chestplate);
                else
                    player.getWorld().dropItem(player.getLocation(), chestplate);
                pinv.setChestplate(new ItemStack(0));
                player.sendMessage(nopermchestplate);
            }
        
        if (leggings != null && armor.contains(leggings.getTypeId()))
            if (!player.hasPermission("equipcontrol.armor." + leggings.getTypeId()) && !player.hasPermission("equipcontrol.armor." + leggings.getType().name()))
            {
                if (pinv.firstEmpty() >= 0)
                    pinv.addItem(leggings);
                else
                    player.getWorld().dropItem(player.getLocation(), leggings);
                pinv.setLeggings(new ItemStack(0));
                player.sendMessage(nopermleggings);
            }
        
        if (boots != null && armor.contains(boots.getTypeId()))
            if (!player.hasPermission("equipcontrol.armor." + boots.getTypeId()) && !player.hasPermission("equipcontrol.armor." + boots.getType().name()))
            {
                if (pinv.firstEmpty() >= 0)
                    pinv.addItem(boots);
                else
                    player.getWorld().dropItem(player.getLocation(), boots);
                pinv.setBoots(new ItemStack(0));
                player.sendMessage(nopermboots);
            }
    }
    
    /**
     * Checks Weapon on Damage
     * 
     * @param event
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (event.getDamager() instanceof Player)
        {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getItemInHand();
            if (weapon.contains(item.getTypeId()))
                if (!player.hasPermission("equipcontrol.weapon." + item.getTypeId()) && !player.hasPermission("equipcontrol.weapon." + item.getType().name()))
                {
                    event.setCancelled(true);
                    player.sendMessage(nopermweap);
                }
        }
        else if (event.getDamager() instanceof Arrow && weapon.contains(261))
        {
            if (((Arrow) event.getDamager()).getShooter() instanceof Player)
            {
                Player player = (Player) ((Arrow) event.getDamager()).getShooter();
                if (!player.hasPermission("equipcontrol.weapon.261") && !player.hasPermission("equipcontrol.weapon.bow"))
                {
                    event.setCancelled(true);
                    player.sendMessage(nopermweap);
                }
            }
        }
    }
}
