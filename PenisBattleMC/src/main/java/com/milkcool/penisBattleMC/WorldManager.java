package com.milkcool.penisBattleMC;

import org.bukkit.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.structure.Structure;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getStructureManager;

import static com.milkcool.penisBattleMC.Constants.worldInGameFlag;

public class WorldManager {
    private ArrayList<World> worlds = new ArrayList<World>();
    private JavaPlugin plugin;

    private static NamespacedKey loadStructureFromRes(String path) {
        InputStream is = WorldManager.class.getClassLoader().getResourceAsStream(path);
        assert is != null;
        Structure structure;
        try {
            structure = getStructureManager().loadStructure(is);
        } catch (IOException e) {
            getLogger().severe("structure load fail!!!");
            e.printStackTrace();
            return null;
        }
        String baseName = path.substring(path.lastIndexOf("/") + 1).split("\\.")[0];
        NamespacedKey key = new NamespacedKey("penis", baseName);
        getStructureManager().registerStructure(key, structure);
        return key;
    }

    WorldManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final NamespacedKey[][] maps = {
            {
                    loadStructureFromRes("structures/map_desert0.nbt"),
                    loadStructureFromRes("structures/map_desert1.nbt"),
                    loadStructureFromRes("structures/map_desert2.nbt"),
                    loadStructureFromRes("structures/map_desert3.nbt"),
            },
            {
                    loadStructureFromRes("structures/map_church0.nbt"),
                    loadStructureFromRes("structures/map_church1.nbt"),
                    loadStructureFromRes("structures/map_church2.nbt"),
                    loadStructureFromRes("structures/map_church3.nbt"),
            },
    };

    public static NamespacedKey[][] getMaps() {
        return maps;
    }

    public void pregenerateWorlds(int n) {
        for(int i = 0; i < n; i++) {
            World world = new WorldCreator("penis_" + i).environment(World.Environment.NORMAL).generator(new MapGenerator()).createWorld();
            worlds.add(world);

            genMap(world);
        }
    }

    public World getWorld() {
        Random rand = new Random();
        ArrayList<Integer> indicies = new ArrayList<Integer>();
        for(int i = 0; i < worlds.size(); i++)
            indicies.add(i);
        while(indicies.size() > 0) {
            int idx = indicies.get(rand.nextInt(indicies.size()));
            World w = worlds.get(idx);
            Boolean fl = w.getPersistentDataContainer().get(worldInGameFlag, PersistentDataType.BOOLEAN);
            if (fl == null || !fl)
                return w;
            indicies.removeIf(x -> x == idx);
        }
        return null;
    }

    public static void genMap(World world) {
        NamespacedKey[][] maps = WorldManager.getMaps();
        String[] spl = world.getName().split("_");
        int i = Integer.parseInt(spl[spl.length - 1]);
        for (int j = 0; j < maps.length; j++)
            if (i % (maps.length + 1) == j + 1) {
                getLogger().info("generating map " + j + " in world " + world.getName());
                Structure s = getStructureManager().getStructure(maps[j][0]);
                getStructureManager().getStructure(maps[j][0]).place(new Location(world, -32, 0, -32), false, StructureRotation.NONE, Mirror.NONE, -1, 1.0F, new Random());
                getStructureManager().getStructure(maps[j][1]).place(new Location(world, 0, 0, -32), false, StructureRotation.NONE, Mirror.NONE, -1, 1.0F, new Random());
                getStructureManager().getStructure(maps[j][2]).place(new Location(world, -32, 0, 0), false, StructureRotation.NONE, Mirror.NONE, -1, 1.0F, new Random());
                getStructureManager().getStructure(maps[j][3]).place(new Location(world, 0, 0, 0), false, StructureRotation.NONE, Mirror.NONE, -1, 1.0F, new Random());
            }
    }
}
