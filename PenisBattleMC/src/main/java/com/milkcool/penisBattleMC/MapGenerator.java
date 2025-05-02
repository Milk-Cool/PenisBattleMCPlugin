package com.milkcool.penisBattleMC;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class MapGenerator extends ChunkGenerator {
    private static final int widthChunks = 4;

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkGenerator.ChunkData chunkData) {
        chunkData.setRegion(0, 0, 0, 16, 1, 16, chunkX < 0 ? Material.RED_TERRACOTTA : Material.BLUE_TERRACOTTA);
        if(chunkX <= -widthChunks / 2 - 1)
            chunkData.setRegion(0, 1, 0, 16, 384, 16, Material.RED_STAINED_GLASS);
        if(chunkX >= widthChunks / 2)
            chunkData.setRegion(0, 1, 0, 16, 384, 16, Material.BLUE_STAINED_GLASS);
        if(chunkZ <= -widthChunks / 2 - 1)
            chunkData.setRegion(0, 1, 0, 16, 384, 16, chunkX < 0 ? Material.RED_STAINED_GLASS : Material.BLUE_STAINED_GLASS);
        if(chunkZ >= widthChunks / 2)
            chunkData.setRegion(0, 1, 0, 16, 384, 16, chunkX < 0 ? Material.RED_STAINED_GLASS : Material.BLUE_STAINED_GLASS);
    }
}
