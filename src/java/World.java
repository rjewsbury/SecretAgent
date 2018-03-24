import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.Location;

import java.util.List;
import java.util.logging.*;

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
	
	Model model;
	View view;

	/** Called before the MAS execution with the args informed in .mas2j */
	
	@Override
	public void init(String[] args) {
		super.init(args);
		
		int num_players;
		boolean use_gui = false;
		//first optional argument is the number of players
		//second optional argument is the 
		if (args.length > 0){
			try{
				num_players = Integer.valueOf(args[0]);
				use_gui = (args.length > 1 && args[1].equals("gui"));
			}catch(NumberFormatException e){
				//if it wasnt a number, use the default number
				num_players = Model.DEFAULT_PLAYERS;
				//check if the argument was to use a gui
				use_gui = args[0].equals("gui");
			}
		}
		else{
			num_players = Model.DEFAULT_PLAYERS;
		}
		
		//create the model
		model = new Model(num_players);
		
		if (use_gui)
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
		int agentId = Integer.parseInt(agName.substring(agName.length() - 1));
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
		else if (action.equals(voteYes)){
			result = model.voteYes(agentId);
		}
		else if (action.equals(voteNo)){
			result = model.voteNo(agentId);
		}
		else if (action.equals(wait))
			result = true;//do nothing
		
		else
			logger.info("executing: "+action+", but not implemented!");
		
		//logger.info("player "+agentId+" did something!");
		
		if (result)
		{
			if(view != null)
				view.updateAgents();
			try { Thread.sleep(300); } catch (InterruptedException x) { }
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
		clearPercepts();
		
		//tell all players about the board state
		addPercept(Literal.parseLiteral("virusPlayed(" + model.getNumVirus() + ")"));
		addPercept(Literal.parseLiteral("antiVirusPlayed(" + model.getNumAntiVirus() + ")"));
		
		//tell all players about the vote state
		if(model.getVoteComplete()){
			addPercept(Literal.parseLiteral("voteComplete"));
			for(int i = 0; i < model.getNumPlayers(); i++)
				addPercept(Literal.parseLiteral("vote(" + i + ", " + model.getVote(i) + ")"));
		}
		
		//tell all players who the kernel is
		int kernelID = model.getKernel();
		if(kernelID != -1){
			addPercept(Literal.parseLiteral("kernel(" + kernelID + ")"));
			//tell the kernel about the contents of their hand
			if(model.getHandSize(kernelID) > 0){
				addPercept("player"+kernelID, Literal.parseLiteral("heldVirus(" + model.getHeldVirus(kernelID) + ")"));
				addPercept("player"+kernelID, Literal.parseLiteral("heldAntiVirus(" + model.getHeldAntiVirus(kernelID) + ")"));
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
				addPercept("player"+schedulerID, Literal.parseLiteral("heldVirus(" + model.getHeldVirus(schedulerID) + ")"));
				addPercept("player"+schedulerID, Literal.parseLiteral("heldAntiVirus(" + model.getHeldAntiVirus(schedulerID) + ")"));
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
			addPercept("player"+i, Literal.parseLiteral("role("+model.getRole(i)+")"));
		
		//tell each player what their number is
		for(int i = 0; i < model.getNumPlayers(); i++)
			addPercept("player"+i, Literal.parseLiteral("player("+i+")"));
	}
	
}
