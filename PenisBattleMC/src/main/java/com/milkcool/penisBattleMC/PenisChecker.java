package com.milkcool.penisBattleMC;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.milkcool.penisBattleMC.Constants.woolColors;

public class PenisChecker {
    // Long penis checking:
    // if we have a block near us (x+-1 or z+-1), find the center and start from there
    // if there aren't any blocks near us, go down until we find the center
    private static final int[][][] positionsBase = {
            {
                {0, 0, 0},
                {1, 0, 0},
                {-1, 0, 0}
            },
            {
                {0, 0, 0},
                {0, 0, 1},
                {0, 0, -1}
            },
    };
    private static final int[][] positionsBaseAny = {
//            {0, 0, 0},
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1}
    };
    private static final int[][] doublePositionsStem = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1}
    };

    private Block block;
    public int penisLength = -1;

    PenisChecker(Block block) {
        this.block = block;
    }

    Block checkBase(Material woolColor) {
        penisLength = -1;
        boolean foundAny = false;
        if(woolColor == null) woolColor = block.getType();
        for(int[][] positionBase : positionsBase) {
            boolean found = true;
            for(int[] position : positionBase) {
                if(block.getRelative(position[0], position[1], position[2]).getType() != woolColor) {
                    found = false;
                    break;
                }
            }
            if(found) {
                foundAny = true;
                break;
            }
        }
        if(!foundAny) return null;
        int py = 1;
        while(block.getRelative(0, py, 0).getType() == woolColor && py < 384) py++;
        if(block.getRelative(0, py, 0).getType() != Material.PINK_WOOL) return null;
        if(py < 2) return null;
        penisLength = py;
        return block;
    }

    Block checkAny(Material woolColor) {
        penisLength = -1;
        ArrayList<Material> allowedWools = new ArrayList<>();
        for(Material wool : woolColors) {
            if(wool != Material.PINK_WOOL && (woolColor == null || woolColor == wool)) allowedWools.add(wool);
        }
        if(!allowedWools.contains(block.getType()) && block.getType() != Material.PINK_WOOL) return null;
        Block startingBlock = null;
        for(int[] position : positionsBaseAny) {
            Block near = block.getRelative(position[0], position[1], position[2]);
            if(!allowedWools.contains(near.getType())) continue;
            Block potential = block.getRelative(position[0], position[1] + 1, position[2]);
            if(!allowedWools.contains(potential.getType())) continue;
            startingBlock = near;
        }
        if(startingBlock == null) {
            int my = 1;
            Block last = null;
            while(block.getY() - my > 0
                    && (last = new PenisChecker(block.getRelative(0, -my, 0)).checkBase(woolColor)) == null
                    && block.getRelative(0, -my, 0).getType() != Material.PINK_WOOL) my++;
            if(last != null) startingBlock = block.getRelative(0, -my, 0);
        }
        if(startingBlock == null) startingBlock = block;
        PenisChecker checker = new PenisChecker(startingBlock);
        Block res = checker.checkBase(woolColor);
        if(res == null) return null;
        penisLength = checker.penisLength;
        return startingBlock;
    }

    Block checkDoubleBase(Material woolColor) {
        if(checkBase(woolColor) == null) return null;

        if(block.getRelative(1, 0, 0).getType() == woolColor && block.getRelative(2, 0, 0).getType() == woolColor) return block;
        if(block.getRelative(-1, 0, 0).getType() == woolColor && block.getRelative(-2, 0, 0).getType() == woolColor) return block;
        if(block.getRelative(0, 0, 1).getType() == woolColor && block.getRelative(0, 0, 2).getType() == woolColor) return block;
        if(block.getRelative(0, 0, -1).getType() == woolColor && block.getRelative(0, 0, -2).getType() == woolColor) return block;

        if(block.getRelative(1, 0, 0).getType() == woolColor && block.getRelative(0, 0, 1).getType() == woolColor) return block;
        if(block.getRelative(0, 0, 1).getType() == woolColor && block.getRelative(-1, 0, 0).getType() == woolColor) return block;
        if(block.getRelative(-1, 0, 0).getType() == woolColor && block.getRelative(0, 0, -1).getType() == woolColor) return block;
        if(block.getRelative(0, 0, -1).getType() == woolColor && block.getRelative(1, 0, 0).getType() == woolColor) return block;

        int py = 1;
        while(block.getRelative(0, py, 0).getType() != Material.AIR && py < 384) {
            if(block.getRelative(1, py, 0).getType() == woolColor) return block;
            if(block.getRelative(0, py, 1).getType() == woolColor) return block;
            if(block.getRelative(0, py, -1).getType() == woolColor) return block;
            if(block.getRelative(-1, py, 0).getType() == woolColor) return block;
            py++;
        }

        return null;
    }

    Block checkDoubleAny(Material woolColor) {
        for(int[] position : doublePositionsStem) {
            Block near = block.getRelative(position[0], position[1], position[2]);
            PenisChecker checker = new PenisChecker(near);
            Block res = checker.checkAny(woolColor);
            if(res != null && new PenisChecker(res).checkDoubleBase(woolColor) != null) return res;
        }
        return null;
    }
}
