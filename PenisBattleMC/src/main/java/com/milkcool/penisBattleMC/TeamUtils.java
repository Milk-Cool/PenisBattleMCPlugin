package com.milkcool.penisBattleMC;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class TeamUtils {
    public static Material getTeamWool(Integer team) {
        if(team == null) return Material.BLACK_WOOL;
        if(team == 0) return Material.RED_WOOL;
        if(team == 1) return Material.BLUE_WOOL;
        if(team == 2) return Material.LIME_WOOL;
        if(team == 3) return Material.YELLOW_WOOL;
        return Material.BLACK_WOOL;
    }

    public static Color getColor(Integer team) {
        if(team == null) return Color.BLACK;
        if(team == 0) return Color.RED;
        if(team == 1) return Color.BLUE;
        if(team == 2) return Color.LIME;
        if(team == 3) return Color.YELLOW;
        return Color.BLACK;
    }

    public static void setInventory(Integer team, Player player) {
        if(team == null) return;
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.addItem(new ItemStack(Material.WOODEN_SWORD, 1));
        inventory.addItem(new ItemStack(Material.SHEARS, 1));
        inventory.addItem(new ItemStack(getTeamWool(team), 64));
        inventory.addItem(new ItemStack(Material.PINK_WOOL, 10));
        inventory.addItem(new ItemStack(Material.BOW, 1));
        inventory.addItem(new ItemStack(Material.ARROW, 16));
        inventory.addItem(new ItemStack(Material.COOKED_BEEF, 16));

        ItemStack helmet = new ItemStack(getTeamWool(team));
        helmet.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        inventory.setHelmet(helmet);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta metaChestplate = (LeatherArmorMeta) chestplate.getItemMeta();
        metaChestplate.setColor(getColor(team));
        chestplate.setItemMeta(metaChestplate);
        chestplate.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        inventory.setChestplate(chestplate);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta metaLeggings = (LeatherArmorMeta) leggings.getItemMeta();
        metaLeggings.setColor(getColor(team));
        leggings.setItemMeta(metaLeggings);
        leggings.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        inventory.setLeggings(leggings);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta metaBoots = (LeatherArmorMeta) boots.getItemMeta();
        metaBoots.setColor(getColor(team));
        boots.setItemMeta(metaBoots);
        boots.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        inventory.setBoots(boots);
    }
}
