package com.milkcool.penisBattleMC;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.List;

import static com.milkcool.penisBattleMC.Constants.woolColors;

public class PenisChecker {
    private static final int[][] positions = {
            {0, 0, 0},
            {0, 1, 0},
            {0, 2, 0},
            {-1, 2, 0},
            {1, 2, 0},
            {0, 2, -1},
            {0, 2, 1}
    };
    private static final int[][] doublePositions = {
            {0, 0, 0},
            {0, 1, 0},
            {0, 2, 0},
            {-1, 2, 0},
            {1, 2, 0},
            {0, 2, -1},
            {0, 2, 1},
            {-1, 1, 0},
            {1, 1, 0},
            {0, 1, -1},
            {0, 1, 1},
            {-1, 0, 0},
            {1, 0, 0},
            {0, 0, -1},
            {0, 0, 1},
            {-2, 2, 0},
            {2, 2, 0},
            {0, 2, -2},
            {0, 2, 2},
    };
    private Block block;

    PenisChecker(Block block) {
        this.block = block;
    }

    Block checkHead(Material woolColor) {
        Material[] woolsArray = woolColor == null ? woolColors : new Material[]{woolColor};
        List<Material> wools = Arrays.asList(woolsArray);
        if(this.block.getType() != Material.PINK_WOOL) return null;
        if(this.block.getRelative(0, -1, 0).getType() == Material.PINK_WOOL) return null;
        if(!wools.contains(this.block.getRelative(0, -1, 0).getType())) return null;
        wools = Arrays.asList(this.block.getRelative(0, -1, 0).getType());
        if(!wools.contains(this.block.getRelative(0, -2, 0).getType())) return null;
        if(!(wools.contains(this.block.getRelative(-1, -2, 0).getType())
                && wools.contains(this.block.getRelative(1, -2, 0).getType())
                || wools.contains(this.block.getRelative(0, -2, -1).getType())
                && wools.contains(this.block.getRelative(0, -2, 1).getType()))) return null;
        return this.block;
    }

    Block checkAny(Material woolColor) {
        for(int[] position : positions) {
            Block potential = new PenisChecker(block.getRelative(position[0], position[1], position[2])).checkHead(woolColor);
            if(potential != null) return potential;
        }
        return null;
    }

    Block checkDoubleHead(Material woolColor) {
        boolean head = checkHead(woolColor) != null;
        if(!head) return null;

        if(
                block.getRelative(-1, -2, 0).getType() == woolColor && block.getRelative(0, -2, -1).getType() == woolColor
                || block.getRelative(-1, -2, 0).getType() == woolColor && block.getRelative(0, -2, 1).getType() == woolColor
                || block.getRelative(1, -2, 0).getType() == woolColor && block.getRelative(0, -2, -1).getType() == woolColor
                || block.getRelative(1, -2, 0).getType() == woolColor && block.getRelative(0, -2, 1).getType() == woolColor

                || block.getRelative(-1, -2, 0).getType() == woolColor && block.getRelative(-2, -2, 0).getType() == woolColor
                || block.getRelative(1, -2, 0).getType() == woolColor && block.getRelative(2, -2, 0).getType() == woolColor
                || block.getRelative(0, -2, -1).getType() == woolColor && block.getRelative(0, -2, -2).getType() == woolColor
                || block.getRelative(0, -2, 1).getType() == woolColor && block.getRelative(0, -2, 2).getType() == woolColor

                || block.getRelative(1, 0, 0).getType() == woolColor
                || block.getRelative(-1, 0, 0).getType() == woolColor
                || block.getRelative(0, 0, -1).getType() == woolColor
                || block.getRelative(0, 0, 1).getType() == woolColor
                || block.getRelative(1, -1, 0).getType() == woolColor
                || block.getRelative(-1, -1, 0).getType() == woolColor
                || block.getRelative(0, -1, -1).getType() == woolColor
                || block.getRelative(0, -1, 1).getType() == woolColor
        ) return block;
        return null;
    }

    Block checkDoubleAny(Material woolColor) {
        for(int[] position : doublePositions) {
            Block potential = new PenisChecker(block.getRelative(position[0], position[1], position[2])).checkDoubleHead(woolColor);
            if(potential != null) return potential;
        }
        return null;
    }
}
