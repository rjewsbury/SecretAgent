import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;
import java.util.*;
import java.util.logging.*;

public class Model extends GridWorldModel
{
	//constants ---------------------------------------
	
	public static final int TABLE = 1 << 3;
	public static final int BOARD = 1 << 4;
	public static final int VIRUS_CARD = 1 << 5;
	public static final int ANTI_VIRUS_CARD = 1 << 6;
	
	public static final int VIRUS_ROLE = 0;
	public static final int ROGUE_ROLE = 1;
	public static final int ANTI_VIRUS_ROLE = 2;
	
	public static final int NUM_PLAYERS = 6;
	
	private static final int GRID_WIDTH = 13;
	private static final int GRID_HEIGHT = 13;
	
	private static final int VIRUS_CARDS = 11;
	private static final int ANTI_VIRUS_CARDS = 6;
	private static final int MAX_HAND = 3;
	
	private static final int NO_ABILITY = 0;
	private static final int CHECK_STACK_3 = 1;
	private static final int INSPECT_ELEMENT = 2;
	private static final int ASSIGN_KERNEL = 3;
	private static final int DELETE_AGENT = 4;
	
	private static final int[] BOARD5_6 =	{0,0,1,4,4,0};
	private static final int[] BOARD7_8 =	{0,2,3,4,4,0};
	private static final int[] BOARD9_10 =	{2,2,3,4,4,0};
	
	//variables ---------------------------------------
	
	private int numVirusCards;
	private int numAntiVirusCards;
	private List<Integer> deck;
	private List<Integer> discard;
	private List<Integer>[] hand;
	
	private int[] role;
	private int[] board;
	
	private int kernelID = -1;
	private int schedulerID = -1;
	private int exKernelID = -1;
	private int exSchedulerID = -1;
	
	//initialization ----------------------------------
	
	public Model()
	{
		super(GRID_WIDTH, GRID_HEIGHT, NUM_PLAYERS);
		//set up the board
		if(NUM_PLAYERS < 7)
			board = Arrays.copyOf(BOARD5_6,BOARD5_6.length);
		else if(NUM_PLAYERS < 9)
			board = Arrays.copyOf(BOARD7_8,BOARD7_8.length);
		else
			board = Arrays.copyOf(BOARD9_10,BOARD9_10.length);
		//set up view positions
		initPositions();
		//initially no cards are played
		numVirusCards = 0;
		numAntiVirusCards = 0;
		//set up the deck
		initDecks();
		//set up roles
		initRoles();
		//set up the hands
		hand = new List[NUM_PLAYERS];
		for(int i = 0; i < NUM_PLAYERS; i++)
			hand[i] = new ArrayList<>(MAX_HAND);
		//set the kernel
		kernelID = random.nextInt(NUM_PLAYERS);
	}
	
	private void initPositions()
	{
		//ternary operators are cool.
		//spacing changes depending on number of players
		int spacing = NUM_PLAYERS > 6 ? 2 : 4;
		int dx = 0;
		int dy = 0;
		//agents sit in static locations
		for(int i = 0; i < NUM_PLAYERS; i++){
			dx = (i < NUM_PLAYERS/2) ?
					spacing*i:
					spacing*(i-NUM_PLAYERS/2);
			dy = (i < NUM_PLAYERS/2) ?
					0:
					8;
			setAgPos(i, 2+dx, 2+dy);
		}
		for(dx = 0; dx < 9; dx++)
			for(dy = 0; dy < 5; dy++)
				set(TABLE, 2+dx, 4+dy);
		for(int i = 0; i < 6; i++)
			set(BOARD, 3+i, 5);
		for(int i = 0; i < 5; i++)
			set(BOARD, 3+i, 7);
	}
	
	private void initRoles()
	{
		List<Integer> tempRole = new ArrayList<>(NUM_PLAYERS);
		
		if(NUM_PLAYERS < 5 || NUM_PLAYERS > 10)
			throw new IndexOutOfBoundsException("Too few players");
		//there's always a virus and a rogue
		tempRole.add(VIRUS_ROLE);
		tempRole.add(ROGUE_ROLE);
		//add the correct number of rogues
		if(NUM_PLAYERS > 6)
			tempRole.add(ROGUE_ROLE);
		if(NUM_PLAYERS > 8)
			tempRole.add(ROGUE_ROLE);
		//add the rest of the players
		while(tempRole.size() < NUM_PLAYERS)
			tempRole.add(ANTI_VIRUS_ROLE);
		//assign randomly
		Collections.shuffle(tempRole);
		//fancy java 8 specific hack.
		//cant convert Integer list to int[] directly,
		//so uses a stream to read through values,
		//maps them to ints using auto unboxing,
		//then converts the mapping to an array
		//also this comment took more lines than a normal solution
		//but this line looks cool so it's all good.
		role = tempRole.stream().mapToInt(i->i).toArray();
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
	
	//helpers -----------------------------------------
	
	private void shuffleDiscard()
	{
		deck.addAll(discard);
		discard.clear();
		Collections.shuffle(deck);
	}
	
	private void updateHand(int ag)
	{
		Location loc = getAgPos(ag);
		//remove any existing cards and add in the current card
		for(int i=0; i < MAX_HAND; i++)
		{
			remove(VIRUS_CARD, loc.x-1+i, loc.y+1);
			remove(ANTI_VIRUS_CARD, loc.x-1+i, loc.y+1);
			
			if(hand[ag].size() > i)
				set(hand[ag].get(i),loc.x-1+i, loc.y+1);
		}
	}
	
	private void updateBoardCards()
	{
		for(int i = 0; i < numVirusCards; i++)
			set(VIRUS_CARD, 3+i, 5);
		for(int i = 0; i < numAntiVirusCards; i++)
			set(ANTI_VIRUS_CARD, 3+i, 7);
	}
	
	//getters -----------------------------------------
	
	public List<Integer> getHand(int ag){ return hand[ag]; }
	
	public int getRole(int ag){ return role[ag]; }
	
	public int getBoardPower(int numVirus){
		return numVirus == 0 ? NO_ABILITY : board[numVirus - 1];
	}
	
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
		if(ag != kernelID || !hand[ag].isEmpty())
			return false;
		
		for(int i = 0; i < 3; i++){
			if(deck.isEmpty())
				shuffleDiscard();
			hand[ag].add(deck.remove(0));
		}
		
		updateHand(ag);
		
		return true;
	}
	
	public boolean passCards(int ag)
	{
		if(ag != kernelID || schedulerID < 0 || schedulerID >= NUM_PLAYERS)
			return false;
		
		hand[schedulerID].addAll(hand[ag]);
		hand[ag].clear();
		
		updateHand(ag);
		updateHand(schedulerID);
		
		return true;
	}
	
	public boolean discardVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(VIRUS_CARD));
		if(success)
			discard.add(VIRUS_CARD);
		
		updateHand(ag);
		
		return success;
	}
	
	public boolean discardAntiVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(ANTI_VIRUS_CARD));
		if(success)
			discard.add(ANTI_VIRUS_CARD);
		
		updateHand(ag);
		
		return success;
	}
	
	public boolean playVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(VIRUS_CARD));
		if(success){
			numVirusCards++;
			updateBoardCards();
			updateHand(ag);
		}
		
		return success;
	}
	
	public boolean playAntiVirus(int ag)
	{
		//using big Integer here allows us
		//to call remove(Object) instead of remove(int)
		boolean success = hand[ag].remove(Integer.valueOf(ANTI_VIRUS_CARD));
		if(success){
			numAntiVirusCards++;
			updateBoardCards();
			updateHand(ag);
		}
		
		return success;	
	}
}