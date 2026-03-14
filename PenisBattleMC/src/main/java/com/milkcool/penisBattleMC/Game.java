package com.milkcool.penisBattleMC;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.milkcool.penisBattleMC.Constants.*;
import static com.milkcool.penisBattleMC.TeamUtils.getTeamWool;
import static com.milkcool.penisBattleMC.TeamUtils.setInventory;

public class Game {
    private World world;
    private int state = 0;
    private BukkitRunnable runnable;
    private int timeSinceLast = 0;
    private JavaPlugin plugin;

    Game(World world, JavaPlugin plugin) {
        this.world = world;
        this.world.setGameRule(GameRule.ADVANCE_TIME, false);
        this.world.setGameRule(GameRule.ADVANCE_WEATHER, false);
        this.world.setGameRule(GameRule.SPAWN_MOBS, false);
        this.world.setGameRule(GameRule.SPAWN_MONSTERS, false);
        this.world.setGameRule(GameRule.SPAWN_WANDERING_TRADERS, false);
        this.world.setGameRule(GameRule.SPAWN_PHANTOMS, false);
        this.world.setGameRule(GameRule.KEEP_INVENTORY, true);
        this.world.setGameRule(GameRule.IMMEDIATE_RESPAWN, true);
        this.world.getPersistentDataContainer().set(teamPoints, PersistentDataType.INTEGER_ARRAY, defaultScores);
        this.plugin = plugin;

        int[] pos = new int[2 * 2];
        for(int i = 0; i < 2; i++) {
            pos[i * 2] = (int)getX(i);
            pos[i * 2 + 1] = (int)getZ(i);

            for(int x = -2; x <= 2; x++)
                for(int z = -2; z <= 2; z++)
                    if(x == -2 || x == 2 || z == -2 || z == 2)
                        this.world.getBlockAt(pos[i * 2] + x, 0, pos[i * 2 + 1] + z).setType(Material.WHITE_TERRACOTTA);
        }
        this.world.getPersistentDataContainer().set(teamSpawnLocations, PersistentDataType.INTEGER_ARRAY, pos);

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Game.this.updateState();
            }
        };
        runnable.runTaskTimer(plugin, 0L, 20L);
    }

    int getState() {
        return state;
    }
    World getWorld() {
        return world;
    }

    private double getX(int teamLoc) {
        return Math.cos(2 * Math.PI * (teamLoc % 2) / 2) * 20;
    }
    private double getZ(int teamLoc) {
        return Math.sin(2 * Math.PI * (teamLoc % 2) / 2) * 20;
    }

    void startGame() {
        state = 1;
        timeSinceLast = 0;

        int t0 = (int) Math.floor(world.getPlayers().size() / 2.0f);
        int t1 = (int) Math.ceil(world.getPlayers().size() / 2.0f);
        for(Player player : world.getPlayers()) {
            int team = Math.random() < (double) t0 / (t0 + t1) ? 0 : 1;
            int teamLoc = (team + 1) % 2;
            if(team == 0) t0--;
            else t1--;
            player.getPersistentDataContainer().set(playerTeam, PersistentDataType.INTEGER, team);
            Location loc = new Location(world, getX(teamLoc), 1, getZ(teamLoc));
            player.teleport(loc);
            player.setRespawnLocation(loc, true);
            player.setHealth(20);
            player.getPersistentDataContainer().set(ballsPresent, PersistentDataType.BOOLEAN, true);
            setInventory(team, player);
            player.sendMessage(plugin.getConfig().getString("msg_game_start"));
        }
    }

    void addPlayer(Player player) {
        Location loc = new Location(world, 0, 1, 0);
        player.teleport(loc);
        player.setRespawnLocation(loc, true);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.getInventory().clear();
        ItemUtils.setInventoryLeave(player, plugin);
    }

    void endGame() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Location loc = Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation();
                world.getPlayers().forEach(player -> {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.setRespawnLocation(loc, true);
                    player.teleport(loc);
                    player.getInventory().clear();
                    ItemUtils.setInventoryStart(player, plugin);
                });

                Bukkit.unloadWorld(world, false);
                try {
                    FileUtils.deleteDirectory(new File(world.getName()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskLater(plugin, 100L);
        int[] teams = world.getPersistentDataContainer().get(teamPoints, PersistentDataType.INTEGER_ARRAY);
        assert teams != null;
        int maxTeam = 0, maxScore = teams[0];
        for(int i = 1; i < teams.length; i++) {
            if(teams[i] > maxScore) {
                maxScore = teams[i];
                maxTeam = i;
            }
        }
        int finalMaxTeam = maxTeam;
        world.getPlayers().forEach(player -> {
            player.sendTitle(teams[0] == teams[1]
                    ? plugin.getConfig().getString("msg_res_tie")
                    : finalMaxTeam == 0
                    ? plugin.getConfig().getString("msg_res_red")
                    : plugin.getConfig().getString("msg_res_blue"), "", 10, 80, 10);
        });
        state = 2;
    }

    void updateState() {
        List<Player> players = world.getPlayers();
        if(state == 0) {
            int playersCount = players.size();
            players.forEach(player -> {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        playersCount < 2 ? new TextComponent(plugin.getConfig().getString("msg_bar_wait"))
                        : new TextComponent(plugin.getConfig().getString("msg_bar_starting_in").replaceAll("%n", Integer.toString(gameStart - timeSinceLast))));
            });
            if(playersCount < 2) {
                timeSinceLast = 0;
                return;
            }
            if(timeSinceLast >= gameStart || playersCount == 6) {
                startGame();
                return;
            }
        } else if(state == 1) {
            PersistentDataContainer container = world.getPersistentDataContainer();
            int[] scores = container.get(teamPoints, PersistentDataType.INTEGER_ARRAY);
            assert scores != null;
            players.forEach(player -> {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new ComponentBuilder(plugin.getConfig().getString("msg_bar_ingame").replaceAll("%n", Integer.toString(gameDuration - timeSinceLast)))
                            .append(" " + plugin.getConfig().getString("msg_bar_red_short") + " " + scores[0]).color(ChatColor.RED)
                            .append(" " + plugin.getConfig().getString("msg_bar_blue_short") + " " + scores[1]).color(ChatColor.BLUE).create()
                );
            });
            if(timeSinceLast >= gameDuration) {
                endGame();
                return;
            }
        }
        timeSinceLast++;
    }
}
