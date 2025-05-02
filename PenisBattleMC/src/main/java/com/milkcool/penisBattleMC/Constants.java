package com.milkcool.penisBattleMC;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.SequencedCollection;
import java.util.stream.Collectors;

public class Constants {
    public static final NamespacedKey playerTeam = new NamespacedKey("penisbattle", "team");
    public static final NamespacedKey teamPoints = new NamespacedKey("penisbattle", "points");
    public static final Material[] woolColors = {
            Material.BLACK_WOOL,
            Material.BLUE_WOOL,
            Material.CYAN_WOOL,
            Material.BROWN_WOOL,
            Material.GRAY_WOOL,
            Material.GREEN_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.LIME_WOOL,
            Material.MAGENTA_WOOL,
            Material.ORANGE_WOOL,
            Material.PINK_WOOL,
            Material.PURPLE_WOOL,
            Material.RED_WOOL,
            Material.WHITE_WOOL,
            Material.YELLOW_WOOL
    };

    public static final int maxPlayers = 6;

    public static final int[] defaultScores = {0, 0, 0, 0};
    public static final int gameStart = 30;
    public static final int gameDuration = 120;
}
