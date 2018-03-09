import jason.asSyntax.*;
import jason.environment.*;
import jason.environment.grid.Location;

import java.util.List;
import java.util.logging.*;

public class World extends Environment {

	private Logger logger = Logger.getLogger("secretagent."+World.class.getName());
	
	private static final Literal drawThree = Literal.parseLiteral("drawThree");
	
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
		
		if (action.equals(drawThree))
			;
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
	}
	
}
