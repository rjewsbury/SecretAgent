import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.util.*;
import java.util.logging.*;

public class Model extends GridWorldModel
{
	public static final int VIRUS_CARD = 1 << 3;
	public static final int ANTI_VIRUS_CARD = 1 << 4;
	
	private static final int GRID_WIDTH = 13;
	private static final int GRID_HEIGHT = 13;
	
	private static final int VIRUS_CARDS = 11;
	private static final int ANTI_VIRUS_CARDS = 6;
	
	private static final int NUM_PLAYERS = 6;
	
	private int numVirusCards;
	private int numAntiVirusCards;
	List<Integer> deck;
	List<Integer> discard;
	List<Integer>[] hand;
	
	private int kernelID = -1;
	private int schedulerID = -1;
	private int exKernelID = -1;
	private int exSchedulerID = -1;
	
	public Model()
	{
		super(GRID_WIDTH, GRID_HEIGHT, NUM_PLAYERS);
		//agents sit in static locations
		setAgPos(0, 2, 2);
		setAgPos(1, 6, 2);
		setAgPos(2, 10, 2);
		setAgPos(3, 2, 10);
		setAgPos(4, 6, 10);
		setAgPos(5, 10, 10);
		//initially no cards are played
		numVirusCards = 0;
		numAntiVirusCards = 0;
		//set up the deck
		initDecks();
		//set up the hands
		hand = new List[NUM_PLAYERS];
		for(int i = 0; i < NUM_PLAYERS; i++)
			hand[i] = new ArrayList<>(3);
		//set the kernel
		kernelID = 0;
	}
		
	private void initDecks()
	{
		deck = new ArrayList<>(20);
		discard = new ArrayList<>(20);
		for(int i = 0; i < VIRUS_CARDS; i++)
			discard.add(VIRUS_CARD);
		for(int i = 0; i < ANTI_VIRUS_CARDS; i++)
			discard.add(ANTI_VIRUS_CARD);
		shuffleDiscard();
	}
	
	private void shuffleDiscard()
	{
		deck.addAll(discard);
		discard.clear();
		Collections.shuffle(deck);
	}
	
	//getters -----------------------------------------
	
	public List<Integer> getHand(int ag){ return hand[ag]; }
	
	public int getKernel(){ return kernelID; }
	
	public int getScheduler(){ return schedulerID; }
	
	public int getExKernel(){ return exKernelID; }
	
	public int getExScheduler(){ return exSchedulerID; }
	
	public int getNumVirus(){ return numVirusCards; }
	
	public int getNumAntiVirus(){ return numAntiVirusCards; }
	
	public int getHeldVirus(int ag){
		int heldVirus = 0;
		for(Integer ID : hand[ag])
			if(ID == VIRUS_CARD)
				heldVirus++;
		return heldVirus;
	}
	
	public int getHeldAntiVirus(int ag){
		int heldAntiVirus = 0;
		for(Integer ID : hand[ag])
			if(ID == ANTI_VIRUS_CARD)
				heldAntiVirus++;
		return heldAntiVirus;
	}
	
	//actions -----------------------------------------
	
	public boolean passKernel(int ag){
		if(!hand[kernelID].isEmpty() || !hand[schedulerID].isEmpty())
			return false;
		
		exKernelID = kernelID;
		exSchedulerID = schedulerID;
		kernelID = (kernelID + 1) % NUM_PLAYERS;
		schedulerID = -1;
		
		return true;
	}
	
	public boolean drawThree(int ag){
		if(ag != kernelID)
			return false;
		
		for(int i = 0; i < 3; i++){
			if(deck.isEmpty())
				shuffleDiscard();
			hand[ag].add(deck.remove(0));
		}
		
		return true;
	}
	
	public boolean passCards(int ag)
	{
		if(ag != kernelID || schedulerID < 0 || schedulerID >= NUM_PLAYERS)
			return false;
		
		hand[schedulerID].addAll(hand[ag]);
		hand[ag].clear();
		
		return true;
	}
	
	public boolean discardVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(VIRUS_CARD));
		if(success)
			discard.add(VIRUS_CARD);
		return success;
	}
	
	public boolean discardAntiVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(ANTI_VIRUS_CARD));
		if(success)
			discard.add(ANTI_VIRUS_CARD);
		return success;
	}
	
	public boolean playVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(VIRUS_CARD));
		if(success)
			numVirusCards++;
		return success;
	}
	
	public boolean playAntiVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(ANTI_VIRUS_CARD));
		if(success)
			numAntiVirusCards++;
		return success;	
	}
}