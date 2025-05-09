package com.milkcool.penisBattleMC;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TeamUtils {
    public static Material getTeamWool(Integer team) {
        if(team == null) return Material.BLACK_WOOL;
        if(team == 0) return Material.RED_WOOL;
        if(team == 1) return Material.BLUE_WOOL;
        if(team == 2) return Material.LIME_WOOL;
        if(team == 3) return Material.YELLOW_WOOL;
        return Material.BLACK_WOOL;
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
    }
}
