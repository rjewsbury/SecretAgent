import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.logging.Logger;

public class VirusBeliefBase extends PlayerBeliefBase
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
		
		Iterator<Literal> notVirus = getDefaultBeliefs(Literal.parseLiteral("notVirus(X)"), null);
		ArrayList<Integer> notVirusAgents = new ArrayList<Integer>();
		Literal candidate;
		//X is id, Y is amount
		try{
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
		}catch(NullPointerException e){}
		
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
		int virusPlayed = getVirusPlayed();
		int antiVirusPlayed = getAntiVirusPlayed();
		Literal discardDecision = Literal.parseLiteral("discardDecision(virus)");
		if(virusPlayed > 4)	
			discardDecision = Literal.parseLiteral("discardDecision(antivirus)");
		else if(antiVirusPlayed > 3)
			discardDecision = Literal.parseLiteral("discardDecision(antivirus)");
		
		List<Literal> result = new ArrayList<Literal>();
		result.add(discardDecision);
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
	
	public Iterator<Literal> getInterpretVote(Literal l, Unifier u)
	{
		int ag = Integer.parseInt(u.get(l.getTermsArray()[0].toString()).toString());
		int myVote = getVote(getID());
		int agVote = getVote(ag);
		int val = getTrust(ag);
		
		if(myVote == agVote)
			val += 1;
		else
			val -= 2;
		Literal interpretVote = Literal.parseLiteral("interpretVote("+ag+","+val+")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(interpretVote);
		return result.iterator();
	}
	
	public Iterator<Literal> getInterpretCard(Literal l, Unifier u)
	{
		int ag = Integer.parseInt(u.get(l.getTermsArray()[0].toString()).toString());
		boolean playedAntiVirus = wasAntiVirusPlayed();
		int val = getTrust(ag);
		
		
		if(playedAntiVirus)
			val += 10;
		else
			val -= 12;
		
		Literal interpretVote = Literal.parseLiteral("interpretCard("+ag+","+val+")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(interpretVote);
		return result.iterator();
	}
	
}