import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.logging.Logger;

public class RogueBeliefBase extends PlayerBeliefBase
{	
	//ROGUE SPECIFIC HELPER FUNCTIONS ---------------------------------
	//Gets your belief about who the virus is
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
	
	//DECISIONS -------------------------------------------------------
	//Get the scheduler candidate that you believe should be elected
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
			
			if(candidates.contains(getVirusID()) && getVirusPlayed() >= 3)
				agentID = getVirusID();
			
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
		getVirusID();
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
	
	//Get the decision of which card to discard
	public Iterator<Literal> getDiscardDecision(Literal l, Unifier u)
	{	
		int ownID = getID();
		int kernelID = getKernelID();
		int schedulerID = getSchedulerID();
		Literal discardDecision = Literal.parseLiteral("discardDecision(virus)");
		
		if(ownID == kernelID && getHeldVirus() == 2)
			discardDecision = Literal.parseLiteral("discardDecision(antivirus)");
		else if(ownID == kernelID && getHeldVirus() == 1)
			discardDecision = Literal.parseLiteral("discardDecision(virus)");
		else if(ownID == schedulerID && (getAntiVirusPlayed() > 2 || getVirusPlayed() > 2) && getHeldVirus() == 1)
			discardDecision = Literal.parseLiteral("discardDecision(antivirus)");
		
		List<Literal> result = new ArrayList<Literal>();
		result.add(discardDecision);
		return result.iterator();
	}
	
	//Get the decision about which agent to kill
	public Iterator<Literal> getDeleteDecision(Literal l, Unifier u)
	{		
		Iterator<Literal> i = getDefaultBeliefs(Literal.parseLiteral("deleteCandidate(X)"), null);
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		Literal candidate;
		int schedulerID = getSchedulerID();
		
		while(i.hasNext())
		{
			candidate = i.next();
			Term[] candidate_terms = candidate.getTermsArray();
			int agentID = Integer.parseInt(candidate_terms[0].toString());
			candidates.add(agentID);
		}
		
		candidates.remove(Integer.valueOf(getVirusID()));
		
		Random r = new Random();
		int pick = r.nextInt(candidates.size());
		int agentID = schedulerID;
		if(schedulerID != getVirusID())
			agentID = schedulerID;
		else
			agentID = candidates.get(pick);
		
		candidate = Literal.parseLiteral("deleteDecision(" + agentID +")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(candidate);
		return result.iterator();
	}
	
	//Get the hand broadcast decision based on beliefs about what cards you had
	public Iterator<Literal> getHandBroadcastDecision(Literal l, Unifier u)
	{
		int heldVirus = getHeldVirus();
		int heldAntiVirus = getHeldAntiVirus();
		Literal handDecision = Literal.parseLiteral("handBroadcastDecision("+heldAntiVirus+","+heldVirus+")");
		
		if(getID() == getKernelID() && heldVirus == 2)
			handDecision = Literal.parseLiteral(
				"handBroadcastDecision("+(heldAntiVirus - 1)+","+(heldVirus + 1)+")");
		else if(getID() == getSchedulerID() && 
			(getAntiVirusPlayed() > 2 || getVirusPlayed() > 2) && heldVirus == 1)
			handDecision = Literal.parseLiteral(
				"handBroadcastDecision("+(heldAntiVirus - 1)+","+(heldVirus + 1)+")");
		else
			handDecision = Literal.parseLiteral(
				"handBroadcastDecision("+heldAntiVirus+","+heldVirus+")");
		
		List<Literal> result = new ArrayList<Literal>();
		result.add(handDecision);
		return result.iterator();
	}
	
	//Interprets the votes of other players based on your vote
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
	
	//Interprets the card that was played by the scheduler and adjusts trust
	public Iterator<Literal> getInterpretCard(Literal l, Unifier u)
	{
		int ag = Integer.parseInt(u.get(l.getTermsArray()[0].toString()).toString());
		boolean playedAntiVirus = wasAntiVirusPlayed();
		int val = getTrust(ag);
		
		
		if(playedAntiVirus)
			val += 9;
		else
			val -= 3;
		
		Literal interpretVote = Literal.parseLiteral("interpretCard("+ag+","+val+")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(interpretVote);
		return result.iterator();
	}
	
}