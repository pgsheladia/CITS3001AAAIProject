package agents;
import loveletter.*;
import java.util.Random;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;


/**
 * An agent which user Monte Carlo Tree Search to
 * An interface for representing an agent in the game Love Letter
 * All agent's must have a 0 parameter constructor
 * */
public class MCTSAgent implements Agent {

    private Random rand;
    private State current;
    private int myIndex;
    private static final int WIN_SCORE = 10;
    private int level;


    //0 place default constructor
    public MCTSAgent() {
        rand = new Random();
        this.level = 3;
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
        long end = start + 60 * (2 * (this.level - 1) + 1);
        
        NodeState rootNodeState = new NodeState(current);
        Tree tree = new Tree();
        Node rootNode = tree.getRoot();
        rootNode.setState(rootNodeState);

        while(System.currentTimeMillis() < end) {
            // 1. Selection
            Node promisingNode = selectPromisingNode(rootNode);

            // 2. Expansion
            if (promisingNode.getState().roundOver() == false) {
                int playerInd = promisingNode.getState().getPlayerIndex();
                Card inHand = promisingNode.getState().getCard(playerInd);
                expandNode(promisingNode, c, inHand, playerInd);
            }

            // 3. Simulation
            Node nodeToExplore = promisingNode;
            if (promisingNode.getChildren().size() > 0) {
                nodeToExplore = promisingNode.getRandomChildNode();
            }
            int playoutResult = simulateRandomPlayout(nodeToExplore);

            // 4. Backpropagation
            backPropogation(nodeToExplore, playoutResult);
        }

        Node winnerNode = rootNode.getChildWithMaxScore();
        tree.setRoot(winnerNode);
        return winnerNode.getState().getAction();
    }

    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (node.getChildren().size() != 0) {
            node = getUCBNode(node);
        }
        return node;
    }

    private void expandNode(Node node, Card c, Card inHand, int playerInd) {
        List<NodeState> possibleStates = node.getState().getAllPossibleStates(c, inHand, playerInd);
        possibleStates.forEach(state -> {
            Node newNode = new Node(state);
            newNode.setParent(node);
            node.getChildren().add(newNode);
        });
    }

    private int simulateRandomPlayout(Node node) {
        Node tempNode = new Node(node);
        NodeState tempState = tempNode.getState();
        int roundStatus = tempState.roundWinner();

        if (roundStatus != myIndex) {
            tempNode.getParent().setWinScore(Integer.MIN_VALUE);
            return roundStatus;
        }
        while (roundStatus == -1) {
            tempState.setPlayerNo();
            Card c = tempState.drawCard();
            int playerInd = tempState.getPlayerIndex();
            Card inHand = tempState.getCard(playerInd);
            tempState.randomPlay(c, inHand, playerInd);
            roundStatus = tempState.roundWinner();
        }

        return roundStatus;
    }

    private void backPropogation(Node nodeToExplore, int playerNo) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.incrementVisit();
            if (tempNode.getState().getPlayerIndex() == playerNo)
                tempNode.addScore(WIN_SCORE);
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


