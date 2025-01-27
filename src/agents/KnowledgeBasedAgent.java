package agents;
import loveletter.*;
import java.util.Arrays;
import java.util.Random;

/**
 * An interface for representing an agent in the game Love Letter
 * All agent's must have a 0 parameter constructor
 * */
public class KnowledgeBasedAgent implements Agent {

    private Random rand;
    private State current;
    private int myIndex;

    //0 place default constructor
    public KnowledgeBasedAgent() {
        rand = new Random();
    }

    /**
     * Reports the agents name
     * */
    public String toString() {return "Knowledge Based Agent";}

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
     * */
    public Action playCard(Card c) {
        Action act = null;
        int target = -1; // target player
        // int noTarget = -1; // stores the player which should not be targeted
        Card guess = null; // for Guard action
        Card inHand = current.getCard(myIndex); // the current card

        // if we know someone's card, store it for later use
        for(int i=0; i<current.numPlayers(); i++) {
            if(i != myIndex && !current.eliminated(i) && !current.handmaid(i) && current.getCard(i) != null) {
                target = i;
                guess = current.getCard(i);
                break;
            }
        }

        act = playCompulsoryCard(c, inHand);
        if(act != null) {
            return act;
        }
        act = playGuardCard(c, inHand, guess, target);
        if(act != null) {
            return act;
        }
        act = playPriestCard(c, inHand);
        if(act != null) {
            return act;
        }
        act = playBaronCard(c, inHand, guess, target);
        if(act != null) {
            return act;
        }
        act = playHandmaidCard(c, inHand);
        if(act != null) {
            return act;
        }
        act = playPrinceCard(c, inHand, guess, target);
        if(act != null) {
            return act;
        }
        act = playKingCard(c, inHand);
        if(act != null) {
            return act;
        }
        act = playCountessCard(c, inHand);
        if(act != null) {
            return act;
        }
        // do nothing for the Princess
        return act;
    }

    /**
     * Perform Guard action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @param guess the known card of a player
     * @param target the player whose card we know
     * @return the action the agent chooses to perform
     * */
    public Action playGuardCard(Card c, Card inHand, Card guess, int target) {
        Action act = null;
        if(inHand == Card.GUARD || c == Card.GUARD) { // if we have a guard
            if(guess != null && guess != Card.GUARD) { // if we know their non-Guard card
                try {
                    act = Action.playGuard(myIndex, target, guess);
                } catch(IllegalActionException e) {}
            } else {
                // choose the player with highest score and the card with higher chances of them having it
                int[] remainingCards = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
                remainingCards = getUnseenCards(inHand);
                target = getHighScorePlayer();
                int maxCard = max(remainingCards);
                try {
                    act = Action.playGuard(myIndex, target, Card.values()[maxCard]);
                } catch(IllegalActionException e) {}
            }
        }
        return act;
    }

    /**
     * Perform Priest action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @return the action the agent chooses to perform
     * */
    public Action playPriestCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.PRIEST || c == Card.PRIEST) {
            // choose the player with the highest score
            int target = getHighScorePlayer();
            try {
                act = Action.playPriest(myIndex, target);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    /**
     * Perform Baron action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @param guess the known card of a player
     * @param target the player whose card we know
     * @return the action the agent chooses to perform
     * */
    public Action playBaronCard(Card c, Card inHand, Card guess, int target) {
        Action act = null;
        if(inHand == Card.BARON || c == Card.BARON) {
            if(guess != null) { // if we know a card
                if(inHand == Card.BARON && guess.value() < c.value()) { // if known card has smaller value than our card
                    try {
                        act = Action.playBaron(myIndex, target);
                    } catch(IllegalActionException e) {}
                } else if(c == Card.BARON && guess.value() < inHand.value()) {
                    try {
                        act = Action.playBaron(myIndex, target);
                    } catch(IllegalActionException e) {}
                } else { // the known card has higher value
                    if(playersLeft() == 2) { // one other player, therefore play the other card if possible
                        act = playGuardCard(c, inHand, guess, target);
                        if(act != null) {
                            return act;
                        }
                        act = playPriestCard(c, inHand);
                        if(act != null) {
                            return act;
                        }
                        act = playHandmaidCard(c, inHand);
                        if(act != null) {
                            return act;
                        }
                        act = playPrinceCard(c, inHand, guess, target);
                        if(act != null) {
                            return act;
                        }
                        act = playKingCard(c, inHand);
                        if(act != null) {
                            return act;
                        }
                        act = playCountessCard(c, inHand);
                        if(act != null) {
                            return act;
                        }
                        // else we lose the round either way as the other card must be Princess or Baron
                    }
                    act = playRandom(c, Card.values()[2], -1);
                }
            } else {
                act = playRandom(c, Card.values()[2], -1);
            }
        }
        return act;
    }

    /**
     * Perform Handmaid action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @return the action the agent chooses to perform
     * */
    public Action playHandmaidCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.HANDMAID || c == Card.HANDMAID) {
            try {
                act = Action.playHandmaid(myIndex);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    /**
     * Perform Prince action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @param guess the known card of a player
     * @param target the player whose card we know
     * @return the action the agent chooses to perform
     * */
    public Action playPrinceCard(Card c, Card inHand, Card guess, int target) {
        Action act = null;
        if(inHand == Card.PRINCE || c == Card.PRINCE) {
            if(guess != null && guess == Card.PRINCESS) { // if we know that a player has a princess
                try {
                    act = Action.playPrince(myIndex, target); // we can eliminate them
                } catch(IllegalActionException e) {}
            } else {
                if(inHand == Card.PRINCESS || c == Card.PRINCESS) { // if we also have a Princess
                    act = playRandom(c, Card.values()[4], myIndex); // don't target ourselves
                } else {
                    act = playRandom(c, Card.values()[4], -1);
                }
            }
        }
        return act;
    }

    /**
     * Perform King action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @return the action the agent chooses to perform
     * */
    public Action playKingCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.KING || c == Card.KING) {
            act = playRandom(c, Card.values()[5], -1);
        }
        return act;
    }

    /**
     * Perform Countess action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @return the action the agent chooses to perform
     * */
    public Action playCountessCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.COUNTESS || c == Card.COUNTESS) {
            // if the programs gets to this statement, we have to play Countess as the other card must be Princess
            try {
                act = Action.playCountess(myIndex);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    /**
     * Perform a compulsory action
     * @param c the card drawn from the deck
     * @param inHand the card already in hand
     * @return the compulsory action the agent chooses to perform
     * */
    public Action playCompulsoryCard(Card c, Card inHand) {
        Action act = null;
        // If we have Prince or King and Countess, we must play Countess
        if((inHand == Card.PRINCE || inHand == Card.KING) && c == Card.COUNTESS) {
            try {
                act = Action.playCountess(myIndex);
            } catch(IllegalActionException e) {}
        }
        else if((c == Card.PRINCE || c == Card.KING) && inHand == Card.COUNTESS) {
            try {
                act = Action.playCountess(myIndex);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    /**
     * Makes careful random target choices
     * @param c the card drawn from the deck
     * @param play the card that will be played
     * @param noTarget the player that must not be selected
     * @return the action the agent chooses to perform
     * @throws IllegalActionException when the Action produced is not legal.
     * */
    public Action playRandom(Card c, Card play, int noTarget) {
        Action act = null;
        while(!current.legalAction(act, c)) {
            int target = rand.nextInt(current.numPlayers());
            // choose a new target if current target is eliminated or protected by handmaid
            while(current.eliminated(target) || current.handmaid(target) || target == myIndex) {
                target = rand.nextInt(current.numPlayers());
                // if all other players are protected by handmaid, then choose a target that is not elimitated
                if(current.allHandmaid(myIndex) && !current.eliminated(target) && target != myIndex) {
                    break;
                }
            }
            if(noTarget != -1) { // we can't choose noTarget
                target = noTarget;
                while(current.handmaid(target) || target == noTarget) { // hence choose another player
                    target = rand.nextInt(current.numPlayers());
                }
            }
            // if current card is Prince and all other players are protected by Handmaid
            if(play == Card.PRINCE && current.allHandmaid(myIndex)) {
                target = myIndex; // we must choose ourself
            }

            try {
                switch(play) {
                case BARON:  
                    act = Action.playBaron(myIndex, target);
                    break;
                case PRINCE:
                    act = Action.playPrince(myIndex, target);
                    break;
                case KING:
                    act = Action.playKing(myIndex, target);
                    break;
                default:
                    act = null; // never play Princess
                }
            } catch(IllegalActionException e) {/*do nothing, just try again*/}  
        }
        return act;
    }

    /**
     * Gets all the unseen cards from the current deck (includes the cards in
     * other players' hands and cards from the current deck)
     * @param inHand the card already in hand
     * @return array containing the cards that have not been played
     * */
    public int[] getUnseenCards(Card inHand) {
        Card[] unseenCards = current.unseenCards();
        int[] remainingCards = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
        
        for(Card unseen : unseenCards) {
            if(unseen == Card.GUARD) {
                remainingCards[0]++;
            } else if(unseen == Card.PRIEST) {
                remainingCards[1]++;
            } else if(unseen == Card.BARON) {
                remainingCards[2]++;
            } else if(unseen == Card.HANDMAID) {
                remainingCards[3]++;
            } else if(unseen == Card.PRINCE) {
                remainingCards[4]++;
            } else if(unseen == Card.KING) {
                remainingCards[5]++;
            } else if(unseen == Card.COUNTESS) {
                remainingCards[6]++;
            } else { // must be PRINCESS
                remainingCards[7]++;
            }
        }
        // subtract our inHand card
        remainingCards[inHand.value()-1]--;

        return remainingCards;
    }

    /**
     * Finds the index of maximum from an array
     * @param a the array for which to find the index of maximum
     * @return index of maximum integer
     * */
    public int max(int[] a) {
        int max = -1;
		int index = -1;

        // we start from i=1 as we want a non-Guard card
		for (int i = 1; i < a.length; i++) {
			if (max < a[i]) {
				max = a[i];
				index = i;
			}
        }
        return index;
    }

    /**
     * Finds the player (that is still in the round) with the highest score
     * @return index of the player with the highest score
     * */
    public int getHighScorePlayer() {
        int index = -1;
        int num = current.numPlayers();
        int[][] scores = new int[num][2]; // 1st column = player index, 2nd column = score

        for(int i=0; i<num; i++) {
            scores[i][0] = i;
            scores[i][1] = current.score(i);
        }

        // sort the 2D array
        Arrays.sort(scores, new java.util.Comparator<int[]>() {
            public int compare(int[] a, int[] b) {
                return Integer.compare(b[1], a[1]);
            }
        });

        for(int i=0; i<num; i++) {
            // check if the player is not protected by Handmaid and they're still in the round
            if(scores[i][0] != myIndex && !current.handmaid(scores[i][0]) && !current.eliminated(scores[i][0])) {
                index = scores[i][0];
                break;
            }
        }
        if(current.allHandmaid(myIndex)) { // if all other players are protected by Handmaid
            for(int i=0; i<num; i++) {
                if(scores[i][0] != myIndex && !current.eliminated(scores[i][0])) {
                    index = scores[i][0]; // choose the highest score player that is not eliminated
                    break;
                }
            }
        }

        return index;
    }

    /**
     * Finds the number of players left in the current round
     * @return number of players left in the current round
     * */
    public int playersLeft() {
        int players = 0;
        for(int i=0; i<current.numPlayers(); i++) {
            if(!current.eliminated(i)) {
                players++;
            }
        }
        return players;
    }
}


