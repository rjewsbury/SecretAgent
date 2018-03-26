import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.Random;
import java.util.logging.Logger;

public abstract class PlayerBeliefBase extends DefaultBeliefBase
{
	@Override
	public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u)
	{
		if (l.getFunctor().equals("schedulerCandidate"))
			return getSchedulerCandidate(l, u);
		else if(l.getFunctor().equals("voteDecision"))
			return getVoteDecision(l, u);
		else if(l.getFunctor().equals("discardDecision"))
			return getDiscardDecision(l, u);
		else if(l.getFunctor().equals("deleteDecision"))
			return getDeleteDecision(l, u);
		else if(l.getFunctor().equals("handBroadcastDecision"))
			return getHandBroadcastDecision(l, u);
		else	
			return super.getCandidateBeliefs(l, u);
	}
	
	public Iterator<Literal> getDefaultBeliefs(Literal l, Unifier u){
		return super.getCandidateBeliefs(l, u);
	}
	
	//Get the scheduler candidate that should be elected
	public abstract Iterator<Literal> getSchedulerCandidate(Literal l, Unifier u);
	
	//Get the vote decision based on beliefs about the Kernel and Scheduler
	public abstract Iterator<Literal> getVoteDecision(Literal l, Unifier u);
	
	public abstract Iterator<Literal> getDiscardDecision(Literal l, Unifier u);
	
	public abstract Iterator<Literal> getDeleteDecision(Literal l, Unifier u);
	
	public abstract Iterator<Literal> getHandBroadcastDecision(Literal l, Unifier u);
	
	//Get your belief on the rolea of the Kernel
	public int getKernelID()
	{
		Iterator<Literal> percepts = getDefaultBeliefs(Literal.parseLiteral("kernel(X)"), null);
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			//Get the Kernel ID
			Term[] terms = p.getTermsArray();
			int kernelID = Integer.parseInt(terms[0].toString());
			return kernelID;
		}
		return -1;
	}
	
	//Get your belief on the role of the Scheduler
	public int getElectedSchedulerID()
	{
		Iterator<Literal> percepts = getDefaultBeliefs(Literal.parseLiteral("electedScheduler(X)"), null);
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			//Get the Elected Scheduler ID
			Term[] terms = p.getTermsArray();
			int electedSchedulerID = Integer.parseInt(terms[0].toString());
			return electedSchedulerID;
		}
		return -1;
	}
	
	public int getHeldVirus()
	{
		Iterator<Literal> percepts = getDefaultBeliefs(Literal.parseLiteral("heldVirus(X)"), null);
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			//Get the number of held virus
			Term[] terms = p.getTermsArray();
			int heldVirus = Integer.parseInt(terms[0].toString());
			return heldVirus;
		}
		return -1;
	}
	
	public int getHeldAntiVirus()
	{
		Iterator<Literal> percepts = getDefaultBeliefs(Literal.parseLiteral("heldAntiVirus(X)"), null);
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			//Get the number of held virus
			Term[] terms = p.getTermsArray();
			int heldAntiVirus = Integer.parseInt(terms[0].toString());
			return heldAntiVirus;
		}
		return -1;
	}
	
	public Literal getRoleBelief(int ag)
	{
		Iterator<Literal> percepts = getPercepts();
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			int agentID = 0;
			//If believes agent is Virus
			if (p.getFunctor().equals("isVirus"))
			{
				Term[] terms = p.getTermsArray();
				agentID = Integer.parseInt(terms[0].toString());
			}
			//If believes agent is Rogue
			else if (p.getFunctor().equals("isRogue"))
			{
				Term[] terms = p.getTermsArray();
				agentID = Integer.parseInt(terms[0].toString());
			}
			else if(p.getFunctor().equals("isAntiVirus"))
			{
				Term[] terms = p.getTermsArray();
				agentID = Integer.parseInt(terms[0].toString());
			}
			
			if(ag == agentID)
			{
				return p;
			}
		}
		return null;
	}
}