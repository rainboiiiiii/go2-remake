package com.go2super.service.battle;

import com.go2super.service.battle.astar.AStar;
import com.go2super.service.battle.astar.Node;

import java.util.List;

public class Pathfinder {

    public static List<Node> getPathing(BattleCell starter, BattleCell target, int movement, int[][] obstacles) {

        Node initialNode = new Node(starter.getX(), starter.getY());
        Node finalNode = new Node(target.getX(), target.getY());

        int rows = 25;
        int cols = 25;

        AStar finder = new AStar(rows, cols, initialNode, finalNode, movement);
        finder.setBlocks(obstacles);

        return finder.findPath();

    }

}
