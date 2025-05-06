package com.milkcool.penisBattleMC;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

import static com.milkcool.penisBattleMC.Constants.*;
import static com.milkcool.penisBattleMC.TeamUtils.getTeamWool;

public final class PenisBattleMC extends JavaPlugin implements Listener, CommandExecutor {
    private List<Game> games = new ArrayList<>();

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getCommand("play")).setExecutor(this);
        getLogger().info("PenisBattle enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PenisBattle disabled!");
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        if(event.getPlayer().getWorld().getName().equals("world") && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }
        if(!event.getPlayer().getWorld().getName().startsWith("penis_")) return;
        Block head = event.getBlock();
        Integer team = event.getPlayer().getPersistentDataContainer().get(playerTeam, PersistentDataType.INTEGER);
        if(team == null) return;
        World world = event.getPlayer().getWorld();
        int[] teamLocations = world.getPersistentDataContainer().get(teamSpawnLocations, PersistentDataType.INTEGER_ARRAY);
        assert teamLocations != null;
        for(int i = 0; i < teamLocations.length; i += 2)
            if(Math.abs(event.getBlock().getX() - teamLocations[i]) <= 2 && Math.abs(event.getBlock().getZ() - teamLocations[i + 1]) <= 2) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You can't place blocks here!");
            }

        Material teamWool = getTeamWool(team);
        PenisChecker checker = new PenisChecker(head);
        if(checker.checkDoubleAny(teamWool) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You can't place blocks like that!");
            return;
        }
        Block penisHead = checker.checkAny(teamWool);
        if(penisHead == null) return;
        PersistentDataContainer worldContainer = event.getPlayer().getWorld().getPersistentDataContainer();
        int[] scores = worldContainer.get(teamPoints, PersistentDataType.INTEGER_ARRAY);
        if(scores == null) return;
        scores[team]++;
        worldContainer.set(teamPoints, PersistentDataType.INTEGER_ARRAY, scores);
        event.getPlayer().sendMessage("penis detected! (+1)");
        event.getPlayer().giveExp(10);

        event.getPlayer().getWorld().spawnParticle(Particle.END_ROD, penisHead.getLocation().add(0.5, 2.5, 0.5), 33, 0.1, 3, 0.1, 0.1);
    }

    @EventHandler
    public void onCut(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Player)) return;
        PersistentDataContainer container = event.getRightClicked().getPersistentDataContainer();
        if(!Objects.equals(container.get(ballsPresent, PersistentDataType.BOOLEAN), true)) return;
        Player player = event.getPlayer();
        ItemStack stack = player.getInventory().getItem(EquipmentSlot.HAND);
        if(stack == null || stack.getType() != Material.SHEARS) return;
        Integer team = player.getPersistentDataContainer().get(playerTeam, PersistentDataType.INTEGER);
        if(team == null) return;

        PersistentDataContainer worldContainer = event.getPlayer().getWorld().getPersistentDataContainer();
        int[] scores = worldContainer.get(teamPoints, PersistentDataType.INTEGER_ARRAY);
        if(scores == null) return;
        scores[team]++;
        worldContainer.set(teamPoints, PersistentDataType.INTEGER_ARRAY, scores);
        container.set(ballsPresent, PersistentDataType.BOOLEAN, false);

        player.sendMessage("Balls cut off! (+1)");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        String msg = event.getDeathMessage();
        for(Player p : world.getPlayers()) {
            p.sendMessage(msg == null ? player.getName() + " died" : msg);
        }
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().getWorld().getName().equals("world") && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
            return;
        }
        if(!event.getPlayer().getWorld().getName().startsWith("penis_")) return;
        Block block = event.getBlock();
        if(!Arrays.stream(woolColors).toList().contains(block.getType())) {
            event.setCancelled(true);
            return;
        }
        Integer team = event.getPlayer().getPersistentDataContainer().get(playerTeam, PersistentDataType.INTEGER);
        if(team == null) return;
        Material teamWool = getTeamWool(team);
        PenisChecker checker = new PenisChecker(block);
        if(checker.checkAny(teamWool) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You can't destroy your own penises!");
            return;
        }
        if(checker.checkAny(null) == null) return;
        PersistentDataContainer worldContainer = event.getPlayer().getWorld().getPersistentDataContainer();
        int[] scores = worldContainer.get(teamPoints, PersistentDataType.INTEGER_ARRAY);
        if(scores == null) return;
        scores[team]++;
        worldContainer.set(teamPoints, PersistentDataType.INTEGER_ARRAY, scores);
        event.getPlayer().sendMessage("penis destroyed! (+1)");
        event.getPlayer().giveExp(10);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(player.getWorld().getName().equals("world")) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().teleport(Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation());
        event.getPlayer().getInventory().clear();
        event.getPlayer().setHealth(20);
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        for(Player player : event.getPlayer().getWorld().getPlayers()) {
            Integer team = player.getPersistentDataContainer().get(playerTeam, PersistentDataType.INTEGER);
            player.spigot().sendMessage(new ComponentBuilder("<")
                    .append(event.getPlayer().getName()).color((team == null
                                            ? ChatColor.WHITE
                                            : team == 0
                                            ? ChatColor.RED
                                            : ChatColor.BLUE).asBungee())
                    .append("> ").reset().append(event.getMessage()).build());
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(!player.getWorld().getName().startsWith("penis_")) return;
        Integer team = player.getPersistentDataContainer().get(playerTeam, PersistentDataType.INTEGER);
        if(team == null) return;
        if(event.getItem().getItemStack().getType() == Material.PINK_WOOL) return;
        for(Material wool : woolColors)
            if(event.getItem().getItemStack().getType() == wool) {
                event.setCancelled(true);
                event.getItem().remove();
                player.getInventory().addItem(new ItemStack(getTeamWool(team), event.getItem().getItemStack().getAmount()));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.25F, 1.0F);
                break;
            }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(!event.getPlayer().getWorld().getName().startsWith("penis_")) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 127));
            }
        }.runTaskLater(this, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        switch(command.getName()) {
            case "play":
                for(Game game : games) {
                    if(game.getState() == 0
                            && game.getWorld().getPlayers().size() < maxPlayers) {
                        game.addPlayer(player);
                        return true;
                    }
                }
                Game game = new Game(new WorldCreator("penis_" + random.nextLong()).environment(World.Environment.NORMAL).generator(new MapGenerator()).createWorld(), this);
                game.addPlayer(player);
                games.add(game);
                break;
            case "pbadm_start":
                for(Game game2 : games) {
                    if(game2.getState() == 0)
                        game2.startGame();
                }
                break;
        }

        return true;
    }

    @Override
    public void onLoad() {
        final File cwd = new File(".");
        final File[] dirs = cwd.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File _dir, String name) {
                return name.startsWith("penis_");
            }
        });
        assert dirs != null;
        for(File dir : dirs) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
