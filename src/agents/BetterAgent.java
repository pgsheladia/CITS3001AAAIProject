package agents;
import loveletter.*;
import java.util.Arrays;
import java.util.Random;

/**
 * An interface for representing an agent in the game Love Letter
 * All agent's must have a 0 parameter constructor
 * */
public class BetterAgent implements Agent {

    private Random rand;
    private State current;
    private int myIndex;

    //0 place default constructor
    public BetterAgent() {
        rand = new Random();
    }

    /**
     * Reports the agents name
     * */
    public String toString() {return "Better Agent";}

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

    public Action playGuardCard(Card c, Card inHand, Card guess, int target) {
        Action act = null;
        if(inHand == Card.GUARD || c == Card.GUARD) { // if we have a guard
            if(guess != null && guess != Card.GUARD) { // if we know their non-Guard card
                try {
                    System.out.println("~~~~~~~ Played Guard bc we know a player's card: target-"+target+" guess: "+guess.toString()+" ~~~~~~~");
                    act = Action.playGuard(myIndex, target, guess);
                } catch(IllegalActionException e) {}
            } else {
                // choose the player with highest score and the card with higher chances of them having it
                int[] remainingCards = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
                remainingCards = getUnseenCards(c, inHand);
                target = getHighScorePlayer();
                int maxCard = max(remainingCards);
                try {
                    System.out.println("~~~~~~~ Played Guard and chose high scoring trarget & guessed card from probability: target-"+target+" guess: "+Card.values()[maxCard].toString()+" ~~~~~~~");
                    act = Action.playGuard(myIndex, target, Card.values()[maxCard]);
                } catch(IllegalActionException e) {}
            }
        }
        return act;
    }

    public Action playPriestCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.PRIEST || c == Card.PRIEST) {
            // choose the player with the highest score
            int target = getHighScorePlayer();
            try {
                System.out.println("~~~~~~~ Played Priest and chose high scoring target: target-"+target+" ~~~~~~~");
                act = Action.playPriest(myIndex, target);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    public Action playBaronCard(Card c, Card inHand, Card guess, int target) {
        Action act = null;
        if(inHand == Card.BARON || c == Card.BARON) {
            if(guess != null) { // if we know a card
                if(inHand == Card.BARON && guess.value() < c.value()) { // if known card has smaller value than our card
                    try {
                        System.out.println("~~~~~~~ Played Baron and chose target with already known low score card: target-"+target+" ~~~~~~~");
                        act = Action.playBaron(myIndex, target);
                    } catch(IllegalActionException e) {}
                } else if(c == Card.BARON && guess.value() < inHand.value()) {
                    try {
                        System.out.println("~~~~~~~ Played Baron and chose target with already known low score card: target-"+target+" ~~~~~~~");
                        act = Action.playBaron(myIndex, target);
                    } catch(IllegalActionException e) {}
                } else { // the known card has higher value
                    if(playersLeft() == 2) { // one other player, therefore play the other card if possible
                        System.out.println("~~~~~~~ One other player left, playing card other than Baron ~~~~~~~");
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
                    System.out.println("~~~~~~~ Played Baron, target a random person ~~~~~~~");
                    act = playRandom(c, Card.values()[2], -1);
                }
            } else {
                System.out.println("~~~~~~~ Played Baron, target a random person ~~~~~~~");
                act = playRandom(c, Card.values()[2], -1);
            }
        }
        return act;
    }

    public Action playHandmaidCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.HANDMAID || c == Card.HANDMAID) {
            try {
                act = Action.playHandmaid(myIndex);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    public Action playPrinceCard(Card c, Card inHand, Card guess, int target) {
        Action act = null;
        if(inHand == Card.PRINCE || c == Card.PRINCE) {
            if(guess != null && guess == Card.PRINCESS) { // if we know that a player has a princess
                try {
                    System.out.println("~~~~~~~ Played Prince and chose target with already known Princess card: target-"+target+" ~~~~~~~");
                    act = Action.playPrince(myIndex, target); // we can eliminate them
                } catch(IllegalActionException e) {}
            } else {
                if(inHand == Card.PRINCESS || c == Card.PRINCESS) { // if we also have a Princess
                    System.out.println("~~~~~~~ Played Prince, Princess also in hand, so dont target ourselves ~~~~~~~");
                    act = playRandom(c, Card.values()[4], myIndex); // don't target ourselves
                } else {
                    System.out.println("~~~~~~~ Played Prince, target a random person ~~~~~~~");
                    act = playRandom(c, Card.values()[4], -1);
                }
            }
        }
        return act;
    }

    public Action playKingCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.KING || c == Card.KING) {
            System.out.println("~~~~~~~ Played King, target a random person ~~~~~~~");
            act = playRandom(c, Card.values()[5], -1);
        }
        return act;
    }

    public Action playCountessCard(Card c, Card inHand) {
        Action act = null;
        if(inHand == Card.COUNTESS || c == Card.COUNTESS) {
            // if the programs gets to this statement, we have to play Countess as the other card must be Princess
            try {
                System.out.println("~~~~~~~ Played Countess bc the other card is Princess ~~~~~~~");
                act = Action.playCountess(myIndex);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    public Action playCompulsoryCard(Card c, Card inHand) {
        Action act = null;
        // If we have Prince or King and Countess, we must play Countess
        if((inHand == Card.PRINCE || inHand == Card.KING) && c == Card.COUNTESS) {
            try {
                System.out.println("~~~~~~~ Played Countess (bc Prince or King also in hand) ~~~~~~~");
                act = Action.playCountess(myIndex);
            } catch(IllegalActionException e) {}
        }
        else if((c == Card.PRINCE || c == Card.KING) && inHand == Card.COUNTESS) {
            try {
                System.out.println("~~~~~~~ Played Countess (bc Prince or King also in hand) ~~~~~~~");
                act = Action.playCountess(myIndex);
            } catch(IllegalActionException e) {}
        }
        return act;
    }

    // makes careful random target choices, noTarget = the target we should not choose if possible otherwise
    // choosing it may eliminate us
    public Action playRandom(Card c, Card play, int noTarget) {
        System.out.println("~~~~~~~ Random target selection ~~~~~~~");
        Action act = null;
        while(!current.legalAction(act, c)) {
            int target = rand.nextInt(current.numPlayers());
            // choose a new target if current target is eliminated or protected by handmaid
            int count = 1, forceTarget = 0;
            while(current.eliminated(target) || current.handmaid(target) || target == myIndex) {
                target = rand.nextInt(current.numPlayers());
                if(!current.eliminated(target) && target != myIndex) { // save the target which can be used if program stuck in while loop
                    forceTarget = target;
                }
                if(count == 50) { // to stop the from being stuck in this while loop
                    target = forceTarget;
                    break;
                }
                count++;
            }
            count = 1;
            if(noTarget != -1) { // we can't choose noTarget
                System.out.println("~~~~~~~ Can't choose noTarget: "+noTarget+" ~~~~~~~");
                target = noTarget;
                while(target == noTarget) { // hence choose another player
                    target = rand.nextInt(current.numPlayers());
                    System.out.println("~~~~~~~ New random: "+target+" ~~~~~~~");
                    if(count == 50 && play == Card.BARON) { // to stop the program from being stuck in this while loop
                        target = noTarget;
                        break;
                    }
                    count++;
                }
            }
            // if current card is Prince and all other players are protected by Handmaid
            if(play == Card.PRINCE && current.allHandmaid(myIndex)) {
                System.out.println("~~~~~~~ Prince & all other protected > targeted ourself-"+myIndex+"  ~~~~~~~");
                target = myIndex; // we must choose ourself
            }

            System.out.println("~~~~~~~ Random target: "+target+" ~~~~~~~");

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

    // get all the cards that have not been played, i.e. these cards must either be
    // in the current deck or in other players' hands
    public int[] getUnseenCards(Card c, Card inHand) {
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
        // subtract our drawn card and inHand card
        // remainingCards[c.value()-1]--;
        remainingCards[inHand.value()-1]--;

        System.out.println("~~~~~~~ RemainingCards: " + Arrays.toString(remainingCards) + " ~~~~~~~");
        return remainingCards;
    }

    // returns the index of an item with maximum value
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

    // returns the index of the player with high score that is still in the round
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
        System.out.println("~~~~~~~ 2D array: "+Arrays.deepToString(scores)+" ~~~~~~~");

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

        System.out.println("~~~~~~~ High score player: "+index+" ~~~~~~~");
        return index;
    }

    // return the number of players left in the round
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


