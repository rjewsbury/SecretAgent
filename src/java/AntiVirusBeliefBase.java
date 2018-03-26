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
		Literal candidate = null;
		try
		{
			i = getDefaultBeliefs(l ,u);
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			
			ArrayList<Integer> notVirusAgents = getNotVirusAgents();
			
			while(i.hasNext())
			{
				candidate = i.next();
				Term[] candidate_terms = candidate.getTermsArray();
				int agentID = Integer.parseInt(candidate_terms[0].toString());
				candidates.add(agentID);
			}
			//A default scheduler if there is no notVirus agent
			Random r = new Random();
			int pick = r.nextInt(candidates.size()); 
			int agentID = candidates.get(pick);
			candidate = Literal.parseLiteral("schedulerCandidate(" + agentID +")");
			
			//Just a double check
			if(notVirusAgents != null)
			{
				int trust = -50;
				for(Integer id: notVirusAgents)
				{
					for(Integer id2: candidates)
					{
						if(id == id2 && getTrust(id) >= trust)
						{
							candidate = Literal.parseLiteral("schedulerCandidate(" + id +")");
							trust = getTrust(id);
						}
					}
				}
			}
		}catch(NullPointerException ex){}
		
			List<Literal> result = new ArrayList<Literal>();
			result.add(candidate);
			return result.iterator();
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
		
		Iterator<Literal> notVirus = getDefaultBeliefs(Literal.parseLiteral("notVirus(X)"), null);
		ArrayList<Integer> notVirusAgents = getNotVirusAgents();
		Literal candidate;
		//X is id, Y is amount
		
			//If the elected scheduler is not the virus
		try{
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
		}catch(NullPointerException e){}
		
		//Take the final vote 
		Literal voteDecision = Literal.parseLiteral("voteDecision("+ vote +")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(voteDecision);
		return result.iterator();
	}
	
	public Iterator<Literal> getTrustDecision(Literal l, Unifier u)
	{
		int agentID = Integer.parseInt(l.getTermsArray()[0].toString());
		int trust = getTrust(agentID);
		
		int kernelID = getKernelID();
		int schedulerID = getSchedulerID();
		
		
		/*//If Kernel
		if(agentID == kernelID)
		{
			
		}
		
		//If Scheduler
		if(agentID == schedulerID)
		{
				
		}*/
		
		List<Literal> result = new ArrayList<Literal>();
		Literal trustDecision = Literal.parseLiteral("trustDecision("+ agentID +","+ trust +")");
		result.add(trustDecision);
		return result.iterator();
	}
	
	public Iterator<Literal> getDiscardDecision(Literal l, Unifier u)
	{
		Literal voteDecision = Literal.parseLiteral("discardDecision(virus)");
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
		
		Iterator<Literal> notVirus = getDefaultBeliefs(Literal.parseLiteral("notVirus(X)"), null);
		ArrayList<Integer> notVirusAgents = new ArrayList<Integer>();
		
		while(notVirus.hasNext())
		{
			candidate = notVirus.next();
			Term[] candidate_terms = candidate.getTermsArray();
			int agentID = Integer.parseInt(candidate_terms[0].toString());
			notVirusAgents.add(agentID);
		}
		
		//int pick = r.nextInt(candidates.size());
		
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