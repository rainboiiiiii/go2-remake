package com.go2super.database.entity;

import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleShipCache;
import com.go2super.database.entity.sub.BattleTag;
import com.go2super.database.entity.type.MatchType;
import com.go2super.logger.BotLogger;
import com.go2super.packet.Packet;
import com.go2super.service.BattleService;
import com.go2super.service.battle.BattleCell;
import com.go2super.service.battle.Pathfinder;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.type.Target;
import com.go2super.service.battle.type.TargetInterval;
import com.go2super.socket.util.RandomUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Document(collection = "game_matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    private ObjectId id;

    private MatchType matchType;
    private long startDate;

    private int matchId;
    private int galaxyId = -1;

    private int round = 0;
    private int index = 0;

    private long lastAction = 0L;
    private long estimatedDuration = 0L;

    private boolean ended;

    private List<Integer> fleetsInvolved = new ArrayList<>();
    private List<Integer> playersInvolved = new ArrayList<>();

    private List<BattleShipCache> cache = new ArrayList<>();

    private List<BattleFleet> allyFleets = new ArrayList<>();
    private List<BattleFleet> enemyFleets = new ArrayList<>();

    private List<BattleTag> tags = new ArrayList<>();
    // private List<Packet> history = new ArrayList<>();

    public void next() {

        // System.out.println(getFleetsSorted());
        List<BattleFleet> sorted = getFleetsSorted(); // [1. Order all fleets by speed]
        int index = 0;

        // todo [2. calculate remaining roundStart commanders]

        while(index < sorted.size()) {

            // get the fleet
            BattleFleet fleet = sorted.get(index++);

            if(fleet.isDestroyed())
                continue;

            attack(fleet, sorted);

        }

    }

    private void attack(BattleFleet fleet, List<BattleFleet> allFleets) {

        BattleCell[][] cells = getCells(allFleets);
        BattleFleet target = findTarget(fleet, cells);

        fleet.calculate();
        target.calculate();

        int movement = fleet.calculateMovement();
        int range = fleet.getCurrentRange();

        Target config = fleet.getFleetTarget();

        Node starterNode = fleet.getNode();

        List<Node> path = Pathfinder.getPathing(fleet.getCell(cells), target.getCell(cells), range, getBlocks(fleet, cells));
        List<Node> follow = new ArrayList<>();

        for(Node node : path) {

            if(node.getHeuristic(starterNode) == 0)
                continue;

            int distance = node.getHeuristic(target.getNode());

            if(movement > 0) {

                if(config == Target.MIN_RANGE && distance > range)
                    break;

                if(config == Target.MAX_RANGE && distance < range)
                    break;

                movement--;
                follow.add(node);
                continue;

            }

            break;

        }

        System.out.println(follow);
        print(cells);

        System.exit(-1);

        // let the madness begin

    }

    private BattleFleet findTarget(BattleFleet starter, BattleCell[][] cells) {

        int startX = starter.getPosX();
        int startY = starter.getPosY();

        Target target = starter.getFleetTarget();
        TargetInterval interval = starter.getFleetTargetInterval();

        BotLogger.dev("STARTER [" + starter.getPosX() + ", " + starter.getPosY() + "] :: " + target + " - " + interval + " (Range MIN=" + starter.getMinRange() + ", MAX=" + starter.getMaxRange() + ")");

        BattleFleet result = null;
        BattleCell targetCell = null;

        for(int i = 0; i < 25; i++)
            for(int j = 0; j < 25; j++) {

                BattleCell cell = cells[i][j];

                if(cell == null || cell.isEmpty() || !cell.hasEnemies(starter))
                    continue;

                BotLogger.dev("[" + cell.getAllFleets().size() + "] Enemies in (X=" + i + ", Y=" + j + ", D=" + cell.getDistance(starter) + ")");

                if(targetCell == null) {

                    targetCell = cell;
                    continue;

                }

                BattleCell select = compareCells(starter, interval, targetCell, cell);

                if(select == targetCell)
                    continue;

                targetCell = select;
                BotLogger.dev("CHANGED");

            }

        result = targetCell.getEnemyCandidate(starter);
        BotLogger.dev("Target = " + result);
        return result;

    }

    private BattleCell compareCells(BattleFleet starter, TargetInterval interval, BattleCell current, BattleCell compare) {

        int currentDistance = current.getDistance(starter);
        int compareDistance = compare.getDistance(starter);

        int currentAttribute = current.getEnemyAttribute(starter.isAttacker(), interval);
        int compareAttribute = current.getEnemyAttribute(starter.isAttacker(), interval);

        switch (interval) {

            case CLOSEST:
                if (currentDistance > compareDistance) {
                    return compare;
                }
                return current;

            case COMMANDER:
            case MAX_ATTACK_POWER:
            case MAX_DURABILITY:
                if (compareAttribute > currentAttribute) {
                    return compare;
                }
                return current;

            case MIN_ATTACK_POWER:
            case MIN_DURABILITY:
                if (compareAttribute < currentAttribute) {
                    return compare;
                }
                return current;

            case DEFENSIVE_BUILDINGS:
                break;

            default:
                break;

        }

        return current;

    }

    public void print(BattleCell[][] cells) {

        System.out.println();

        for(int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {

                BattleCell cell = cells[i][j];
                String represent = "..";

                if(cell != null)
                    if(!cell.isEmpty())
                        represent = cell.getCharacter();
                    else
                        represent = "OB";

                System.out.print(represent + " ");

            }
            System.out.println();
        }

    }

    public int[][] getBlocks(BattleFleet starter, BattleCell[][] matrix) {

        List<Pair<Integer, Integer>> obstacles = new ArrayList<>();

        for(int i = 0; i < 25; i++)
            for(int j = 0; j < 25; j++) {

                BattleCell current = matrix[i][j];

                if(current == null)
                    continue;

                if(current.hasEnemies(starter) && !current.hasAllies(starter))
                    obstacles.add(Pair.of(current.getX(), current.getY()));

            }

        int[][] blocks = new int[obstacles.size()][2];

        for(int i = 0; i < obstacles.size(); i++) {

            Pair<Integer, Integer> position = obstacles.get(i);
            blocks[i] = new int[]{position.getKey(), position.getValue()};

        }

        return blocks;

    }

    public BattleCell getCell(BattleFleet fleet, BattleCell[][] matrix) {
        return matrix[fleet.getPosX()][fleet.getPosY()];
    }

    public BattleCell[][] getCells(List<BattleFleet> fleets) {

        BattleCell[][] cells = new BattleCell[25][25];

        for(BattleFleet fleet : fleets) {

            if(cells[fleet.getPosX()][fleet.getPosY()] == null)
                cells[fleet.getPosX()][fleet.getPosY()] = new BattleCell();

            BattleCell cell = cells[fleet.getPosX()][fleet.getPosY()];
            cell.addFleet(fleet);

        }

        return cells;

    }

    public List<BattleFleet> getFleetsSorted() {

        List<BattleFleet> fleets = Stream.concat(allyFleets.stream(), enemyFleets.stream())
                                         .collect(RandomUtil.toShuffledList())
                                         .stream()
                                         .sorted(Comparator.naturalOrder())
                                         .collect(Collectors.toList());

        return fleets;

    }

    public void save() {
        BattleService.getInstance().getMatchRepo().save(this);
    }

}
