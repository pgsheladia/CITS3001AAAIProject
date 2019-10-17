package agents;
import loveletter.*;
import java.util.Random;

/**
 * An interface for representing an agent in the game Love Letter
 * All agent's must have a 0 parameter constructor
 * */
public class SimpleReflexAgent implements Agent {

    private Random rand;
    private State current;
    private int myIndex;

    //0 place default constructor
    public SimpleReflexAgent() {
        rand = new Random();
    }

    /**
     * Reports the agents name
     * */
    public String toString() {return "Simple Reflex Agent";}

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
        Action act = null;
        Card play;
        while(!current.legalAction(act, c)) {
            Card inHand = current.getCard(myIndex);
            // play the card with the lowest value, if equal then discard the card in hand
            if(inHand.value() <= c.value()) {
                play = inHand;
            } else {
                play = c;
            }

            // If we have Prince or King and Countess, we must play Countess
            if((inHand == Card.PRINCE || inHand == Card.KING) && c == Card.COUNTESS) {
                play = c;
            }
            else if((c == Card.PRINCE || c == Card.KING) && inHand == Card.COUNTESS) {
                play = inHand;
            }

            // targets a random player in the round
            int target = rand.nextInt(current.numPlayers());
            // System.out.println("target: " + target + "\tplay: " + play);
            try {
                switch(play) {
                case GUARD:
                    // only guess a non-guard card
                    int randomNum = rand.nextInt(8);
                    while(Card.values()[randomNum] == Card.GUARD) {
                        randomNum = rand.nextInt(8);
                    }
                    act = Action.playGuard(myIndex, target, Card.values()[randomNum]);
                    break;
                case PRIEST:
                    act = Action.playPriest(myIndex, target);
                    break;
                case BARON:  
                    act = Action.playBaron(myIndex, target);
                    break;
                case HANDMAID:
                    act = Action.playHandmaid(myIndex);
                    break;
                case PRINCE:
                    act = Action.playPrince(myIndex, target);
                    break;
                case KING:
                    act = Action.playKing(myIndex, target);
                    break;
                case COUNTESS:
                    act = Action.playCountess(myIndex);
                    break;
                default:
                    act = null; // never play Princess
                }
            } catch(IllegalActionException e) {/*do nothing, just try again*/}  
        }
        return act;
    }
}


