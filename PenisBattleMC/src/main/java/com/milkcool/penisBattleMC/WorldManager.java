package com.milkcool.penisBattleMC;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.Random;

import com.milkcool.penisBattleMC.MapGenerator;
import org.bukkit.WorldCreator;
import org.bukkit.persistence.PersistentDataType;

import static com.milkcool.penisBattleMC.Constants.worldInGameFlag;

public class WorldManager {
    private ArrayList<World> worlds = new ArrayList<World>();

    public void pregenerateWorlds(int n) {
        for(int i = 0; i < n; i++)
            worlds.add(new WorldCreator("penis_" + i).environment(World.Environment.NORMAL).generator(new MapGenerator()).createWorld());
    }

    public World getWorld() {
        for(World w : worlds) {
            Boolean fl = w.getPersistentDataContainer().get(worldInGameFlag, PersistentDataType.BOOLEAN);
            if (fl == null || !fl)
                return w;
        }
        return null;
    }
}
