import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.Random;
import java.util.logging.Logger;

public class AntiVirusBeliefBase extends PlayerBeliefBase
{	
	public Iterator<Literal> getSchedulerCandidate(Literal l, Unifier u)
	{
		Iterator<Literal> i;
		try
		{
			i = getDefaultBeliefs(l ,u);		
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
			return getDefaultBeliefs(l, u);
		}
	}
	
	//Get the vote decision based on beliefs about the Kernel and Scheduler
	public Iterator<Literal> getVoteDecision(Literal l, Unifier u)
	{
		Iterator<Literal> i;
		int kernelID = getKernelID();
		int electedSchedulerID = getElectedSchedulerID();
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
			return getDefaultBeliefs(l, u);
		}
	}
	
	public Iterator<Literal> getDiscardDecision(Literal l, Unifier u)
	{
		Literal voteDecision = Literal.parseLiteral("discardDecision(0)");
		List<Literal> result = new ArrayList<Literal>();
		result.add(voteDecision);
		return result.iterator();
	}
	
	public Iterator<Literal> getDeleteDecision(Literal l, Unifier u)
	{		
		Iterator<Literal> i = getDefaultBeliefs(Literal.parseLiteral("deleteCandidate(X)"), null);
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
		candidate = Literal.parseLiteral("deleteDecision(" + agentID +")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(candidate);
		return result.iterator();
	}
	
	public Iterator<Literal> getHandBroadcastDecision(Literal l, Unifier u)
	{
		int heldVirus = getHeldVirus();
		int heldAntiVirus = getHeldAntiVirus();
		Literal handDecision = Literal.parseLiteral("handBroadcastDecision("+heldAntiVirus+","+heldVirus+")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(handDecision);
		return result.iterator();
	}
}