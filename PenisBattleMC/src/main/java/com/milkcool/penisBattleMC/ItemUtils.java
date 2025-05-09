package com.milkcool.penisBattleMC;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemUtils {
    public static final ItemStack itemStart = new ItemStack(Material.SHEARS);
    public static final ItemStack itemLeave = new ItemStack(Material.RED_BED);

    public static ItemStack getItemStart(JavaPlugin plugin) {
        ItemMeta meta = itemStart.getItemMeta();
        meta.setItemName(plugin.getConfig().getString("msg_item_join"));
        itemStart.setItemMeta(meta);
        return itemStart;
    }
    public static ItemStack getItemLeave(JavaPlugin plugin) {
        ItemMeta meta = itemLeave.getItemMeta();
        meta.setItemName(plugin.getConfig().getString("msg_item_leave"));
        itemLeave.setItemMeta(meta);
        return itemLeave;
    }

    public static void setInventoryStart(Player player, JavaPlugin plugin) {
        Inventory inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(4, getItemStart(plugin));
    }
    public static void setInventoryLeave(Player player, JavaPlugin plugin) {
        Inventory inventory = player.getInventory();
        inventory.clear();
        inventory.setItem(4, getItemLeave(plugin));
    }
}
