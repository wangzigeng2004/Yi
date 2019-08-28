package codes.nibby.yi.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single game 'state' in the game tree.
 * Each node has 1 parent and 0+ children.
 *
 * Internally the game states are stored as a string of <strong>m x n</strong> integers
 * where <strong>m</strong> is the board width and <strong>n</strong> is the board height.
 *
 * The numbers on the state register represent:
 * <ul>
 *     <li><strong>0</strong>: Nothing</li>
 *     <li><strong>1</strong>: Black stone</li>
 *     <li><strong>2</strong>: White stone</li>
 * </ul>
 *
 * The object for displayed stones on the board are found in <strong>Game</strong> class.
 *
 * @author Kevin Yang
 * Created on 23 August 2019
 */
public class GameNode {

    /*
        Game node data:
     */
    // Current move that is played
    // This is an array of size 2:
    // move[0] = moveX, move[1] = moveY
    private int[] currentMove;
    // Stone color
    private int color;

    /** List of custom-added black stones in this node for demonstration purposes */
    private List<Integer> helpersBlack;
    /** List of custom-added white stones in this node for demonstration purposes */
    private List<Integer> helpersWhite;
    /** List of captures at this node. */
    private int prisonersBlack, prisonersWhite;

    /** The position of the ko square */
    private int koPoint = -1;

    // Game tree hierarchy
    /** The parent node */
    private GameNode parent = null;

    /** Children nodes, with index 0 being the main variation child. */
    private List<GameNode> children = new ArrayList<>();

    /** Board stone positions at this point. */
    private int[] stoneData;

    public GameNode() {
        this(null);
    }

    public GameNode(GameNode parent) {
        this.parent = parent;
    }

    public void addChild(GameNode node) {
        addChild(node, false);
    }

    /**
     * Append a child node to this node, with the option of making the child node
     * part of the main branch.
     *
     * @param node Child node to be appended.
     * @param makeMainChild Whether to make child node part of the main variation.
     */
    public void addChild(GameNode node, boolean makeMainChild) {
        if (makeMainChild)
            children.add(0, node);
        else
            children.add(node);
    }

    private boolean hasChildren() {
        return children.size() > 0;
    }

    public GameNode getParent() {
        return parent;
    }

    public List<GameNode> getChildren() {
        return children;
    }

    public boolean hasParent() {
        return parent != null;
    }

    /**
     *
     *
     * @return The distance to the root.
     */
    public int getMoveNumber() {
        int steps = 0;
        GameNode current = this;
        while (current.hasParent()) {
            current = current.getParent();
            steps++;
        }
        return steps;
    }

    /**
     * Builds a sequential array of game states that leads up to the current state.
     *
     * @return An array of game states, with root at index 0.
     */
    public GameNode[] getNodeHistory() {
        int size = getMoveNumber() + 1;
        GameNode[] result = new GameNode[size];
        GameNode current = this;
        for (int step = size - 1; step > 0; step--) {
            result[step] = current;
            current = current.getParent();
        }
        return result;
    }

    public int[] getStoneData() {
        return stoneData;
    }

    public void setStoneData(int[] stoneData) {
        this.stoneData = stoneData;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public int[] getCurrentMove() {
        return currentMove;
    }

    public void setCurrentMove(int[] currentMove) {
        this.currentMove = currentMove;
    }

    public int getKoPoint() {
        return koPoint;
    }

    public void setKoPoint(int koPoint) {
        this.koPoint = koPoint;
    }

    public int getPrisonersBlack() {
        return prisonersBlack;
    }

    public void setPrisonersBlack(int prisonersBlack) {
        this.prisonersBlack = prisonersBlack;
    }

    public int getPrisonersWhite() {
        return prisonersWhite;
    }

    public void addPrisonersBlack(int blackPrisoners) {
        prisonersBlack += blackPrisoners;
    }

    public void addPrisonersWhite(int whitePrisoners) {
        prisonersWhite += whitePrisoners;
    }

    public void setPrisonersWhite(int prisonersWhite) {
        this.prisonersWhite = prisonersWhite;
    }

    public List<Integer> getHelpersBlack() {
        return helpersBlack;
    }

    public void setHelpersBlack(List<Integer> helpersBlack) {
        this.helpersBlack = helpersBlack;
    }

    public List<Integer> getHelpersWhite() {
        return helpersWhite;
    }

    public void setHelpersWhite(List<Integer> helpersWhite) {
        this.helpersWhite = helpersWhite;
    }
}
