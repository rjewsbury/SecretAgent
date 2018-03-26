import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.Location;
import jason.mas2j.ClassParameters;

import java.util.List;
import java.util.logging.*;
import javax.swing.JOptionPane;

public class World extends TimeSteppedEnvironment {

	private Logger logger = Logger.getLogger("secretagent."+World.class.getName());
	
	private static final Literal passKernel = Literal.parseLiteral("passKernel");
	private static final Literal electScheduler = Literal.parseLiteral("electScheduler");
	private static final Literal drawThree = Literal.parseLiteral("drawThree");
	private static final Literal passCards = Literal.parseLiteral("passCards");
	private static final Literal discardVirus = Literal.parseLiteral("discardVirus");
	private static final Literal discardAntiVirus = Literal.parseLiteral("discardAntiVirus");
	private static final Literal playVirus = Literal.parseLiteral("playVirus");
	private static final Literal playAntiVirus = Literal.parseLiteral("playAntiVirus");
	private static final Literal voteYes = Literal.parseLiteral("voteYes");
	private static final Literal voteNo = Literal.parseLiteral("voteNo");
	private static final Literal wait = Literal.parseLiteral("wait");
	private static final Literal addMessage = Literal.parseLiteral("addMessage");
	private static final Literal deleteMessage = Literal.parseLiteral("deleteMessage");
	private static final Literal deleteAgent = Literal.parseLiteral("deleteAgent");
	
	//currently, the simulation is only designed for 6 players.
	//however the model itself can handle 5-10, for future development
	private static final int NUM_PLAYERS = 6;
	
	private boolean useGui;
	Model model;
	View view;

	/** Called before the MAS execution with the args informed in .mas2j */
	
	@Override
	public void init(String[] args) {
		super.init(args);
		
		useGui = false;
		//first optional argument is the use of a gui
		if (args.length > 0){
			useGui = args[0].equals("gui");
		}
		
		//create the model
		model = new Model(NUM_PLAYERS);
		
		if (useGui)
		{
			view = new View(model);
			model.setView(view);
		}
		try { Thread.sleep(2000); } catch (InterruptedException x) { }
		
		//Makes all the squares turn white at the start
		for(int x = 0; x < model.GRID_WIDTH; x++)
			for(int y = 0; y < model.GRID_HEIGHT; y++)
				view.update(x, y);
		
		updatePercepts();
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		
		boolean result = false;
		int agentId = model.getNameIndex(agName);
		
		if (action.equals(passKernel))
			result = model.passKernel(agentId);
		else if(action.getFunctor().equals(electScheduler.getFunctor()))
			try{
				int elected = Integer.parseInt(action.getTerm(0).toString());
				result = model.electScheduler(agentId, elected);
			}catch(NumberFormatException e){}
		else if (action.equals(passCards))
			result = model.passCards(agentId);
		else if (action.equals(drawThree))
			result = model.drawThree(agentId);
		else if (action.equals(playAntiVirus))
			result = model.playAntiVirus(agentId);
		else if (action.equals(discardAntiVirus))
			result = model.discardAntiVirus(agentId);
		else if (action.equals(playVirus))
			result = model.playVirus(agentId);
		else if (action.equals(discardVirus))
			result = model.discardVirus(agentId);
		else if (action.equals(voteYes))
			result = model.voteYes(agentId);
		else if (action.equals(voteNo))
			result = model.voteNo(agentId);
		else if(action.getFunctor().equals(addMessage.getFunctor()))
		{
			String msg = "";
			for(Term term: action.getTerms()){
				msg += term.toString();
			}
			//System.out.println("-----------------------"+msg);
			result = model.addMessage(agentId, msg);
		}
		else if (action.equals(deleteMessage))
			result = model.deleteMessage(agentId);
		else if(action.getFunctor().equals(deleteAgent.getFunctor()))
			try{
				int target = Integer.parseInt(action.getTerm(0).toString());
				result = model.deleteAgent(agentId, target);
			}catch(NumberFormatException e){}
		else if (action.equals(wait)){
			result = true;//do nothing
		}
		else
			logger.info("executing: "+action+", but not implemented!");
		
		//logger.info(model.getName(i)+" did something!");
		//if(!action.equals(wait))
		//	System.out.println("-----"+agName+"-----"+action+"------"+result);

		if (result)
		{
			if(view != null & model.checkRequireViewUpdate()){
				view.updateAgents();
				view.updateMessages();
			}
			try { Thread.sleep(1); } catch (InterruptedException x) { }
			updatePercepts();
		}
		
		return result;
	}

	/** Called before the end of MAS execution */
	@Override
	public void stop() {
		super.stop();
	}
	
	/**
	 * List of percepts given:
	 *
	 * GLOBALLY VISIBLE:
	 * virusPlayed(X)		- the number of virus cards on the board
	 * antiVirusPlayed(X)	- the number of antivirus cards on the board
	 * kernel(X)			- the player ID of the kernel
	 * exKernel(X)			- the player ID of the previous kernel
	 * scheduler(X)			- the player ID of the scheduler
	 * exScheduler(X)		- the player ID of the previous scheduler
	 * electedScheduler(X)	- the player ID of the proposed new scheduler
	 *
	 * INDIVIDUALLY VISIBLE:
	 * role(X)				- 0 for virus, 1 for rogue, 2 for antivirus
	 * player(X)			- tells the player what their ID is
	 * heldVirus(X)			- the number of virus cards the agent holds
	 * heldAntiVirus(X)		- the number of antivirus cards held
	 */
	private void updatePercepts()
	{
		clearAllPercepts();
		
		//tell all players about the board state
		addPercept(Literal.parseLiteral("virusPlayed(" + model.getNumVirus() + ")"));
		addPercept(Literal.parseLiteral("antiVirusPlayed(" + model.getNumAntiVirus() + ")"));
		
		//tell all players about the vote state
		if(model.getVoteComplete()){
			addPercept(Literal.parseLiteral("voteComplete"));
			for(int i = 0; i < model.getNumPlayers(); i++)
				addPercept(Literal.parseLiteral("vote(" + i + ", " + model.getVote(i) + ")"));
		}
		
		if(model.getCardPlayed() > 0){
			addPercept(Literal.parseLiteral("cardPlayed"));
			if(model.getCardPlayed() == Model.VIRUS_CARD)
				addPercept(Literal.parseLiteral("virusPlayed"));
			else if(model.getCardPlayed() == Model.ANTI_VIRUS_CARD)
				addPercept(Literal.parseLiteral("antiVirusPlayed"));
		}
		
		//tell all players who the kernel is
		int kernelID = model.getKernel();
		if(kernelID != -1){
			addPercept(Literal.parseLiteral("kernel(" + kernelID + ")"));
			//tell the kernel about the contents of their hand
			if(model.getHandSize(kernelID) > 0){
				addPercept(model.getName(kernelID), Literal.parseLiteral("heldVirus(" + model.getHeldVirus(kernelID) + ")"));
				addPercept(model.getName(kernelID), Literal.parseLiteral("heldAntiVirus(" + model.getHeldAntiVirus(kernelID) + ")"));
			}
		}
		//tell all players who the ex kernel is
		int exKernelID = model.getExKernel();
		if(exKernelID != -1)
			addPercept(Literal.parseLiteral("exKernel(" + exKernelID + ")"));
		
		//tell all players who the scheduler is
		int schedulerID = model.getScheduler();
		if(schedulerID != -1){
			addPercept(Literal.parseLiteral("scheduler(" + schedulerID + ")"));
			if(model.getHandSize(schedulerID) > 0){
				addPercept(model.getName(schedulerID), Literal.parseLiteral("heldVirus(" + model.getHeldVirus(schedulerID) + ")"));
				addPercept(model.getName(schedulerID), Literal.parseLiteral("heldAntiVirus(" + model.getHeldAntiVirus(schedulerID) + ")"));
			}
		}
		
		//tell players who the ex scheduler is
		int exSchedulerID = model.getExScheduler();
		if(exSchedulerID != -1)
			addPercept(Literal.parseLiteral("exScheduler(" + exSchedulerID + ")"));
		
		//tell players who the elected scheduler is
		int electedSchedulerID = model.getElectedScheduler();
		if(electedSchedulerID != -1)
			addPercept(Literal.parseLiteral("electedScheduler(" + electedSchedulerID + ")"));
		
		//tell each player what their role is
		for(int i = 0; i < model.getNumPlayers(); i++)
			addPercept(model.getName(i), Literal.parseLiteral("role("+model.getRole(i)+")"));
		
		//tell each player what their number is
		for(int i = 0; i < model.getNumPlayers(); i++)
			addPercept(model.getName(i), Literal.parseLiteral("player("+i+")"));
		
		//tell players which players are eligible to be elected
		for(int i = 0; i < model.getNumPlayers(); i++)
		{
			if(i != kernelID && (i != exKernelID || model.getNumAlive() < 6) && i != exSchedulerID && model.getAlive(i))
				addPercept(Literal.parseLiteral("schedulerCandidate("+i+")"));
		}
		
		//Giving the Kernel a bullet
		if(model.getBoardAbility() == model.DELETE_AGENT)
		{
			addPercept(model.getName(kernelID), Literal.parseLiteral("hasBullet"));
		}
		
		//Bullet Candidates
		for(int i = 0; i < model.getNumPlayers(); i++)
		{
			if(i != kernelID && model.getAlive(i))
				addPercept(Literal.parseLiteral("deleteCandidate("+i+")"));
		}
		
		//All agents know if an agent is dead
		for(int i = 0; i < model.getNumPlayers(); i++)
		{
			if(!model.getAlive(i))
				addPercept(Literal.parseLiteral("dead("+i+")"));
		}
		
		//Rogue and Virus know who all others are
		for(int i = 0; i < model.getNumPlayers(); i++)
		{
			if(model.getRole(i) != model.ANTI_VIRUS_ROLE)
				for(int j = 0; j < model.getNumPlayers(); j++)
				{
					if(model.getRole(j) == model.VIRUS_ROLE)
						addPercept(model.getName(i), Literal.parseLiteral("isVirus("+j+")"));
					else if(model.getRole(j) == model.ROGUE_ROLE)
						addPercept(model.getName(i), Literal.parseLiteral("isRogue("+j+")"));
					else
						addPercept(model.getName(i), Literal.parseLiteral("isAntiVirus("+j+")"));
				}
		}
	}
}
