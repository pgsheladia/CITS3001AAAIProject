package agents;
import loveletter.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * A class representing a Node in a Tree for Monte Carlo Tree Search (MCTS) Agent
 * This class is a modified version of the class from the following reference:
 * https://github.com/eugenp/tutorials/blob/master/algorithms-miscellaneous-1/src/main/java/com/baeldung/algorithms/mcts/tree/Node.java
 * */
public class Node {
    private NodeState nodeState;
    private Node parent;
    private List<Node> children;
    private int visitCount; // how many times this node has been visited
    private double winScore; // keeps track of the win score for a node

    /**
     * Constructs a default Node
     * Default construct nodeState and the children
     * **/
    public Node() {
        Random random = new Random(0);
        Agent[] agents = new Agent[4];
        this.nodeState = new NodeState(random, agents);
        children = new ArrayList<>();
    }

    /**
     * Constructs a Node with an existing NodeState
     * @param nodeState Nodestate to be copied to the new constructed Node
     * **/
    public Node(NodeState nodeState) {
        this.nodeState = nodeState;
        children = new ArrayList<>();
    }

    /**
     * Constructs a Node from an existing node
     * @param node node to be copied to the new constructed node
     * **/
    public Node(Node node) {
        this.children = new ArrayList<>();
        this.nodeState = new NodeState(node.getState());
        if (node.getParent() != null) {
            this.parent = node.getParent();
        }
        List<Node> children = node.getChildren();
        for (Node child : children) {
            this.children.add(new Node(child));
        }
    }

    /**
     * returns the NodeState of node
     * @return Nodestate of current node
     * **/
    public NodeState getState() {
        return nodeState;
    }

    /**
     * returns the parent of node
     * @return parent of current node
     * **/
    public Node getParent() {
        return parent;
    }

    /**
     * returns the children of node
     * @return children Node array of current node
     * **/
    public List<Node> getChildren() {
        return children;
    }

    /**
     * returns a random child from the children of node
     * @return a random child of current node
     * **/
    public Node getRandomChildNode() {
        int noOfPossibleMoves = this.children.size();
        int selectRandom = (int) (Math.random() * noOfPossibleMoves);
        return this.children.get(selectRandom);
    }

    /**
     * returns the child with the highest score
     * @return Node with the highest score
     * **/
    public Node getChildWithMaxScore() {
        return Collections.max(this.children, Comparator.comparing(c -> {
            return c.getVisitCount();
        }));
    }

    /**
     * returns how many times the node has been visited
     * @return the number of times the node has been visited
     * **/
    public int getVisitCount() {
        return visitCount;
    }

    /**
     * returns the score of node
     * @return the score of node
     * **/
    public double getWinScore() {
        return winScore;
    }

    /**
     * Sets the NodeState of node
     * @param nodeState the Nodestate to be assigned
     * **/
    public void setState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    /**
     * Sets the parent of node
     * @param parent the parent to be assigned
     * **/
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Sets the children of node
     * @param children the children to be assigned
     * **/
    public void setchildren(List<Node> children) {
        this.children = children;
    }

    /**
     * Sets the visitCount of node
     * @param visitCount visitCount to be assigned
     * **/
    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    /**
     * Sets the score of node
     * @param winScore the score to be assigned
     * **/
    public void setWinScore(double winScore) {
        this.winScore = winScore;
    }

    /**
     * Increments the thenumber of times node has been visited
     * **/
    public void incrementVisit() {
        this.visitCount++;
    }

    /**
     * Adds score passed to the score of the node
     * @param score the score to be added
     * **/
    public void addScore(double score) {
        if (this.winScore != Integer.MIN_VALUE) {
            this.winScore += score;
        }
    }
}