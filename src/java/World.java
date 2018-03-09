import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.Location;

import java.util.List;
import java.util.logging.*;

public class World extends Environment {

	private Logger logger = Logger.getLogger("secretagent."+World.class.getName());
	
	private static final Literal drawThree = Literal.parseLiteral("drawThree");
	private static final Literal passCards = Literal.parseLiteral("passCards");
	private static final Literal passKernel = Literal.parseLiteral("passKernel");
	private static final Literal playAntiVirus = Literal.parseLiteral("playAntiVirus");
	private static final Literal discardAntiVirus = Literal.parseLiteral("discardAntiVirus");
	private static final Literal playVirus = Literal.parseLiteral("playVirus");
	private static final Literal discardVirus = Literal.parseLiteral("discardVirus");
	private static final Literal vote = Literal.parseLiteral("vote");
	
	Model model;

	/** Called before the MAS execution with the args informed in .mas2j */
	
	@Override
	public void init(String[] args) {
		super.init(args);
		model = new Model();
		
		if (args.length > 0 && args[0].equals("gui"))
		{
			View view = new View(model);
			model.setView(view);
		}
		try { Thread.sleep(2000); } catch (InterruptedException x) { }
		updatePercepts();
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		
		boolean result = false;
		int agentId = 0;
		
		if (action.equals(drawThree))
			result = model.drawThree(agentId);
		if (action.equals(passCards))
			result = model.passCards(agentId);
		if (action.equals(passKernel))
			result = model.passKernel(agentId);
		if (action.equals(playAntiVirus))
			result = model.playAntiVirus(agentId);
		if (action.equals(discardAntiVirus))
			result = model.discardAntiVirus(agentId);
		if (action.equals(playVirus))
			result = model.playVirus(agentId);
		if (action.equals(discardVirus))
			result = model.discardVirus(agentId);
		
		else
			logger.info("executing: "+action+", but not implemented!");
		
		if (result)
		{
			updatePercepts();
			try { Thread.sleep(100); } catch (InterruptedException x) { }
		}
		
		return result;
	}

	/** Called before the end of MAS execution */
	@Override
	public void stop() {
		super.stop();
	}
	
	private void updatePercepts()
	{
		clearPercepts();
		addPercept(Literal.parseLiteral("virusPlayed(" + model.getNumVirus() + ")"));
		addPercept(Literal.parseLiteral("antiVirusPlayed(" + model.getNumAntiVirus() + ")"));
		
		for(int i = 0; i < 6; i++)
		 addPercept("player"+i+"", Literal.parseLiteral("role("+model.getRole(i)+")"));
	}
	
}
