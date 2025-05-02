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
        this.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        this.world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        this.world.setGameRule(GameRule.DO_INSOMNIA, false);
        this.world.setGameRule(GameRule.KEEP_INVENTORY, true);
        this.world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        this.world.getPersistentDataContainer().set(teamPoints, PersistentDataType.INTEGER_ARRAY, defaultScores);
        this.plugin = plugin;

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

    void startGame() {
        state = 1;
        timeSinceLast = 0;
        int n = 0;
        for(Player player : world.getPlayers()) {
            player.getPersistentDataContainer().set(playerTeam, PersistentDataType.INTEGER, n % 2);
            Location loc = new Location(world, Math.cos(2 * Math.PI * ((n + 1) % 2) / 2) * 30, 1, Math.sin(2 * Math.PI * ((n + 1) % 2) / 2) * 30);
            player.teleport(loc);
            player.setRespawnLocation(loc, true);
            player.setHealth(20);
            setInventory(n % 2, player);
            player.sendMessage("game start!");
            n++;
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
            player.sendTitle(finalMaxTeam == 0 ? "Red team wins!" : "Blue team wins!", "", 10, 80, 10);
        });
        state = 2;
    }

    void updateState() {
        List<Player> players = world.getPlayers();
        if(state == 0) {
            int playersCount = players.size();
            players.forEach(player -> {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        playersCount < 2 ? new TextComponent("Waiting for players...")
                        : new TextComponent("Game starting in " + (gameStart - timeSinceLast) + " seconds"));
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
                    new ComponentBuilder("Game ends in " + (gameDuration - timeSinceLast) + " seconds")
                            .append(" R " + scores[0]).color(ChatColor.RED)
                            .append(" B " + scores[1]).color(ChatColor.BLUE).create()
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
