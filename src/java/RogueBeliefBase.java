import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.logging.Logger;

public class RogueBeliefBase extends PlayerBeliefBase
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
		int numVirusPlayed = getVirusPlayed();
		int NO_VOTE = 0;
		int YES_VOTE = 1;
		int vote = YES_VOTE;
		
		//X is id, Y is amount
		Iterator<Literal> notVirus = getDefaultBeliefs(Literal.parseLiteral("notVirus(X)"), null);
		ArrayList<Integer> notVirusAgents = new ArrayList<Integer>();
		Literal candidate;

		try
		{
			while(notVirus.hasNext())
			{
				candidate = notVirus.next();
				Term[] candidate_terms = candidate.getTermsArray();
				int agentID = Integer.parseInt(candidate_terms[0].toString());
				notVirusAgents.add(agentID);
			}
			//If the elected scheduler is not the virus
			if(notVirusAgents.size() > 0)
			{
				for(Integer ID: notVirusAgents)
				{
					if(ID == electedSchedulerID)
					{
						vote = YES_VOTE;
						break;
					}
					else if(ID != electedSchedulerID)
					{
						Iterator<Literal> trust = getDefaultBeliefs(Literal.parseLiteral("trust(X, Y)"), null);
						while(trust.hasNext())
						{
							Term[] trust_terms = trust.next().getTermsArray();
							int agentID = Integer.parseInt(trust_terms[0].toString());
							int trustAmount = Integer.parseInt(trust_terms[1].toString());
							if(agentID == electedSchedulerID && trustAmount > 20 && numVirusPlayed >= 3)
							{
								vote = YES_VOTE;
								break;
							}
							else if(agentID == electedSchedulerID && trustAmount > -1 && numVirusPlayed < 3)
							{
								vote = YES_VOTE;
								break;
							}
							else if(agentID == electedSchedulerID && trustAmount < 0)
								vote = NO_VOTE;
						}
					}
				}
			}
		}
		catch(NullPointerException ex){}
		
		//Take the final vote 
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
		Literal voteDecision = Literal.parseLiteral("discardDecision(1)");
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
		int pick = r.nextInt(candidates.size() - 1); 
		while(pick == getVirusID())
			pick = r.nextInt(candidates.size() - 1); 
			
		int agentID = candidates.get(pick);
		candidate = Literal.parseLiteral("deleteDecision(" + agentID +")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(candidate);
		return result.iterator();
	}
	
	public int getVirusID()
	{
		Iterator<Literal> percepts = getDefaultBeliefs(Literal.parseLiteral("isVirus(X)"), null);
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			//Get the Virus ID
			Term[] terms = p.getTermsArray();
			int virusID = Integer.parseInt(terms[0].toString());
			return virusID;
		}
		return -1;
	}
}