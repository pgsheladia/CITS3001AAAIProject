package loveletter;
import java.util.Random;
import java.io.PrintStream;
import agents.RandomAgent;

/**
 * A class for running a single game of LoveLetter.
 * An array of 4 agents is provided, a deal is initialised and players takes turns until the game ends and the score is reported.
 * @author Tim French
 * */
public class LoveLetter{

  private Agent rando;
  private Random random;
  private PrintStream ps;
  // for testing purposes
  private static int[] playerWins = new int[4];
  private static boolean testing = true;
  private static int numRounds = 10000;

  /**
   * Constructs a LoveLetter game.
   * @param seed a seed for the random number generator.
   * @param ps a PrintStream object to record the events of the game
   * **/
  public LoveLetter(long seed, PrintStream ps){
    this.random = new Random(seed);
    this.ps = ps;
    rando = new RandomAgent();
  }

  /**
   * Constructs a LoveLetter game.
   * Defauklt construct with system random seed, and System.out as the PrintStream
   * **/
  public LoveLetter(){
    this(0,System.out);
    this.ps = System.out;
  }


  /**
   * Plays a game of LoveLetter
   * @param agents the players in the game
   * @return scores of each agent as an array of integers
   * **/
  public int[] playGame(Agent[] agents){
    boolean gameOver = false;
    int winner=0;
    int numPlayers = agents.length;
    State gameState = new State(random, agents);//the game state
    State[] playerStates = new State[numPlayers];
    try{
      while(!gameState.gameOver()){
        for(int i = 0; i<numPlayers; i++){
          playerStates[i] = gameState.playerState(i);
          agents[i].newRound(playerStates[i]);
        }
        while(!gameState.roundOver()){
          if(!testing) {
            System.out.println("Cards are:\nplayer 0:"+gameState.getCard(0)+"\nplayer 1:"+gameState.getCard(1)+"\nplayer 2:"+gameState.getCard(2)+"\nplayer 3:"+gameState.getCard(3));
          }       
          Card topCard = gameState.drawCard(); 
          if(!testing) {
            System.out.println("Player "+gameState.nextPlayer()+" draws the "+topCard);
          }
          Action act = agents[gameState.nextPlayer()].playCard(topCard);
          try{
            if(testing) {
              gameState.update(act,topCard);
            } else {
              ps.println(gameState.update(act,topCard));
            }
          }
          catch(IllegalActionException e){
            ps.println("ILLEGAL ACTION PERFORMED BY PLAYER "+agents[gameState.nextPlayer()]+
              "("+gameState.nextPlayer()+")\nRandom Move Substituted");
            rando.newRound(gameState.playerState(gameState.nextPlayer()));
            act = rando.playCard(topCard);
            ps.println(gameState.update(act,topCard));
          }
          for(int p = 0; p<numPlayers; p++)
            agents[p].see(act,playerStates[p]);
        }
        if(!testing) {
          System.out.println("New Round, scores are:\nplayer 0:"+gameState.score(0)+"\nplayer 1:"+gameState.score(1)+"\nplayer 2:"+gameState.score(2)+"\nplayer 3:"+gameState.score(3));
        }        
        gameState.newRound();
      }
      playerWins[gameState.gameWinner()]++;
      if(!testing) {
        ps.println("Player "+gameState.gameWinner()+" wins the Princess's heart!");
      }
      int[] scoreboard = new int[numPlayers];
      for(int p = 0; p<numPlayers; p++)scoreboard[p] = gameState.score(p);
      return scoreboard;
    }catch(IllegalActionException e){
      ps.println("Something has gone wrong.");
      e.printStackTrace();
      return null;
    } 
  }

  /**
   * This main method is provided to run a simple test game with provided agents.
   * The agent implementations should be in the default package.
   * */
  public static void main(String[] args){
    // to run a larger test
    if(testing) {
      for(int i=0; i<numRounds; i++) {
        Agent[] agents = {new agents.RandomAgent(),new agents.RandomAgent(), new agents.SimpleReflexAgent(), new agents.SimpleReflexAgent()};
        LoveLetter env = new LoveLetter();
        env.playGame(agents);
      }
      int totalWins = (playerWins[0] + playerWins[1] + playerWins[2] + playerWins[3]);
      System.out.println("\tAgent 0, (Random):\t\t"+playerWins[0]+" out of "+totalWins);
      System.out.println("\tAgent 1, (Random):\t\t"+playerWins[1]+" out of "+totalWins);
      System.out.println("\tAgent 2, (Simple Reflex):\t"+playerWins[2]+" out of "+totalWins);
      System.out.println("\tAgent 3, (Simple Reflex):\t"+playerWins[3]+" out of "+totalWins);
      float randomTotal = (float) (playerWins[0] + playerWins[1]) / numRounds * 100;
      System.out.println("Random win %:\t\t" + randomTotal);
      float agentTotal = (float) (playerWins[2] + playerWins[3]) / numRounds * 100;
      System.out.println("Simple Reflex win %:\t" + agentTotal);
    } else {
      Agent[] agents = {new agents.RandomAgent(),new agents.RandomAgent(), new agents.SimpleReflexAgent(), new agents.SimpleReflexAgent()};
      LoveLetter env = new LoveLetter();
      StringBuffer log = new StringBuffer("A simple game for four random agents:\n");
      int[] results = env.playGame(agents);
      env.ps.print("The final scores are:\n");
      for(int i= 0; i<agents.length; i++)
        env.ps.print("\t Agent "+i+", \""+agents[i]+"\":\t "+results[i]+"\n");
    }
  }
}


