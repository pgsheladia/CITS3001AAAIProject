package agents;

/**
 * A class representing a Tree for Monte Carlo Tree Search (MCTS) Agent
 * This class is a modified version of the class from the following reference:
 * https://github.com/eugenp/tutorials/blob/master/algorithms-miscellaneous-1/src/main/java/com/baeldung/algorithms/mcts/tree/Tree.java
 * */
public class Tree {
    Node root; // the root node

    /**
     * Constructs a default Tree
     * **/
    public Tree() {
        root = new Node();
    }

    /**
     * Constructs a Tree with the specified root
     * @param root the root to be assigned
     * **/
    public Tree(Node root) {
        this.root = root;
    }

    /**
     * returns the root of Tree
     * @return the root to Tree
     * **/
    public Node getRoot() {
        return root;
    }

    /**
     * Sets the root of Tree
     * @param root the root to be assigned
     * **/
    public void setRoot(Node root) {
        this.root = root;
    }
}