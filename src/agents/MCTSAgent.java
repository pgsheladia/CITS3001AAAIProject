package agents;
import loveletter.*;
import java.util.Random;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;


/**
 * An agent which uses Monte Carlo Tree Search to find the best possible move.
 * The following references were used:
 * https://www.baeldung.com/java-monte-carlo-tree-search
 * https://github.com/eugenp/tutorials/blob/master/algorithms-miscellaneous-1/src/main/java/com/baeldung/algorithms/mcts/montecarlo/MonteCarloTreeSearch.java
 * */
public class MCTSAgent implements Agent {

    private Random rand;
    private State current;
    private int myIndex;


    //0 place default constructor
    public MCTSAgent() {
        rand = new Random();
    }

    /**
     * Reports the agents name
     * */
    public String toString() {return "MCTS Agent";}

    /**
     * Method called at the start of a round
     * @param start the starting state of the round
     **/
    public void newRound(State start) {
        current = start;
        myIndex = current.getPlayerIndex();
    }

    /**
     * Method called when any agent performs an action. 
     * @param act the action an agent performs
     * @param results the state of play the agent is able to observe.
     * **/
    public void see(Action act, State results) {
        current = results;
    }

    /**
     * Perform an action after drawing a card from the deck
     * @param c the card drawn from the deck
     * @return the action the agent chooses to perform
     * @throws IllegalActionException when the Action produced is not legal.
     * */
    public Action playCard(Card c) {
        long start = System.currentTimeMillis();
        long end = start + 900; // 900 milliseconds allowed as a time limit
        
        NodeState rootNodeState = new NodeState(current);
        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.setState(rootNodeState);

        while(System.currentTimeMillis() < end) {
            // 1. Selection - traverses the nodes based on their UCB scores
            Node promisingNode = selectPromisingNode(rootNode);

            // 2. Expansion - explores all the possible actions/states of a node
            if (promisingNode.getState().roundOver() == false) {
                int playerInd = promisingNode.getState().getPlayerIndex();
                Card inHand = promisingNode.getState().getCard(playerInd);
                expandNode(promisingNode, c, inHand, playerInd);
            }

            // 3. Simulation - randomly selects a child node
            Node nodeToExplore = promisingNode;
            if (promisingNode.getChildren().size() > 0) {
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            int playoutResult = simulateRandomPlayout(nodeToExplore);

            // 4. Backpropagation - propagates back to the parent
            backPropogation(nodeToExplore, playoutResult);
        }

        Node winnerNode = rootNode.getChildWithMaxScore();
        tree.setRoot(winnerNode);
        return winnerNode.getState().getAction();
    }

    /**
     * Selects a child Node with the highest UCB score from a parent node
     * @param rootNode parent Node provided
     * @return child Node with the highest UCB score
     * **/
    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (node.getChildren().size() != 0) {
            node = getUCBNode(node);
        }
        return node;
    }

    /**
     * Expands the selected node
     * @param node Node to be expanded
     * @param c the card drawn from the deck
     * @param inHand the card already in hand of a player
     * @param playerInd the index of a player
     * **/
    private void expandNode(Node node, Card c, Card inHand, int playerInd) {
        List<NodeState> possibleStates = node.getState().getAllPossibleStates(c, inHand, playerInd);
        possibleStates.forEach(state -> {
            Node newNode = new Node(state);
            newNode.setParent(node);
            node.getChildren().add(newNode);
        });
    }

    /**
     * Simulates the selected random node
     * @param node Node that is selected for the random playout
     * @return the winner of the round
     * **/
    private int simulateRandomPlayout(Node node) {
        Node tempNode = new Node(node);
        NodeState tempState = tempNode.getState();
        int roundStatus = tempState.roundWinner();

        if (roundStatus != myIndex) {
            tempNode.getParent().setWinScore(Integer.MIN_VALUE);
            return roundStatus;
        }
        while (roundStatus == -1) {
            tempState.setPlayerNumber();
            Card c = tempState.drawCard();
            int playerInd = tempState.getPlayerIndex();
            Card inHand = tempState.getCard(playerInd);
            tempState.randomPlay(c, inHand, playerInd);
            roundStatus = tempState.roundWinner();
        }

        return roundStatus;
    }

    /**
     * Backpropagate from a node to the root node and increments the visit score and
     * the total score for each node in the path.
     * @param nodeToExplore Node from which to start propagating
     * @param playerNo player who is performing the action
     * **/
    private void backPropogation(Node nodeToExplore, int playerNo) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.incrementVisit();
            if (tempNode.getState().getPlayerIndex() == playerNo)
                tempNode.addScore(10);
            tempNode = tempNode.getParent();
        }
    }

    /**
     * Calculates the UCB score for a node
     * @param totalVisit number of times the parent node was visited
     * @param nodeWinScore the total score of node
     * @param nodeVisit number of times the node has been selected from its parent
     * @return the UCB score
     * **/
    private static double UCB(int totalVisit, double nodeWinScore, int nodeVisit) {
        if (nodeVisit == 0) {
            return Integer.MAX_VALUE;
        }
        return (nodeWinScore / (double) nodeVisit) + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
    }

    /**
     * Finds the Node with the highest UCB score for selection process
     * @param node the node to be calculated
     * @return the Node with the highest UCB score
     * **/
    public static Node getUCBNode(Node node) {
        int parentVisit = node.getVisitCount();
        return Collections.max(
          node.getChildren(),
          Comparator.comparing(c -> UCB(parentVisit, c.getWinScore(), c.getVisitCount())));
    }
}


