import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.Random;
import java.util.logging.Logger;

public class AntiVirusBeliefBase extends DefaultBeliefBase
{
	@Override
	public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u)
	{
		if (l.getFunctor().equals("schedulerCandidate"))
			return getSchedulerCandidate(l, u);
		else if(l.getFunctor().equals("voteDecision"))
			return getVoteDecision(l, u);
		else	
			return super.getCandidateBeliefs(l, u);
	}
	
	
	public Iterator<Literal> getSchedulerCandidate(Literal l, Unifier u)
	{
		Iterator<Literal> i;
		
		try
		{
			i = super.getCandidateBeliefs(l ,u);		
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			Literal candidate;
			
			while(i.hasNext())
			{
				candidate = i.next();
				Term[] candidate_terms = candidate.getTermsArray();
				int agentID = Integer.parseInt(candidate_terms[0].toString());
				candidates.add(agentID);
			}
			Random r = new Random();
			int pick = r.nextInt(candidates.size()); 
			int agentID = candidates.get(pick);
			candidate = Literal.parseLiteral("schedulerCandidate(" + agentID +")");
			List<Literal> result = new ArrayList<Literal>();
			result.add(candidate);
			return result.iterator();
		}
		catch(NullPointerException ex)
		{
			return super.getCandidateBeliefs(l, u);
		}
	}
	
	//Get the vote decision based on beliefs about the Kernel and Scheduler
	public Iterator<Literal> getVoteDecision(Literal l, Unifier u)
	{
		Iterator<Literal> i;
		int kernelID = getKernelID();
		int schedulerDID = getSchedulerID();
		int NO_VOTE = 0;
		int YES_VOTE = 1;
		int vote = -1;
		
		//If believe both are safe
		if(true)
			vote = YES_VOTE;
		//Don't believe the Kernel is safe
		/*else if(true)
			vote = YES_VOTE;
		//Don't believe Scheduler is safe
		else if(true) 
			vote = NO_VOTE;
		else
			vote = NO_VOTE;*/
		
		try
		{	
			Literal voteDecision = Literal.parseLiteral("voteDecision("+ vote +")");
			List<Literal> result = new ArrayList<Literal>();
			result.add(voteDecision);
			return result.iterator();
		}
		catch(NullPointerException ex)
		{
			return super.getCandidateBeliefs(l, u);
		}
	}
	
	//Get your belief on the rolea of the Kernel
	private int getKernelID()
	{
		Iterator<Literal> percepts = getPercepts();
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			//Get the Kernel ID
			if (p.getFunctor().equals("kernel"))
			{
				Term[] terms = p.getTermsArray();
				int kernelID = Integer.parseInt(terms[0].toString());
				return kernelID;
			}
		}
		return -1;
	}
	
	//Get your belief on the role of the Scheduler
	private int getSchedulerID()
	{
		Iterator<Literal> percepts = getPercepts();
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			//Get the Scheduler ID
			if (p.getFunctor().equals("scheduler"))
			{
				Term[] terms = p.getTermsArray();
				int schedulerID = Integer.parseInt(terms[0].toString());
				return schedulerID;
			}
		}
		return -1;
	}
	
	private Literal getRoleBelief(int ag)
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