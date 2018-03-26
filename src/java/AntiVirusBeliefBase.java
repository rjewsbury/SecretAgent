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
		int vote = -1;
		
		Iterator<Literal> notVirus = getDefaultBeliefs(Literal.parseLiteral("notVirus(X)"), null);
		ArrayList<Integer> notVirusAgents = getNotVirusAgents();
		Literal candidate;
		//X is id, Y is amount
		
		//If the elected scheduler is not the virus
		try{
			//if the person being elected is confirmed not virus,
			//elect them
			for(Integer ID: notVirusAgents)
			{
				if(ID == electedSchedulerID)
				{
					vote = YES_VOTE;
					break;
				}
			}
			//if we didnt find them in there, we have to base the decision on trust
			if(vote < 0)
			{
				int trustAmount = getTrust(electedSchedulerID);
				if(trustAmount > 5 && numVirusPlayed >= 3)
				{
					vote = YES_VOTE;
				}
				else if(numVirusPlayed < 3)
				{
					vote = YES_VOTE;
				}
				else
				{
					vote = NO_VOTE;
				}
			}
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		
		//Take the final vote 
		Literal voteDecision = Literal.parseLiteral("voteDecision("+ vote +")");
		List<Literal> result = new ArrayList<Literal>();
		result.add(voteDecision);
		return result.iterator();
	}
	
	//Get the decision of which card to discard
	public Iterator<Literal> getDiscardDecision(Literal l, Unifier u)
	{
		Literal voteDecision = Literal.parseLiteral("discardDecision(virus)");
		List<Literal> result = new ArrayList<Literal>();
		result.add(voteDecision);
		return result.iterator();
	}
	
	//Get the decision about which agent to kill
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
	
	//Get the decision about which agent to kill
	public Iterator<Literal> getHandBroadcastDecision(Literal l, Unifier u)
	{
		int heldVirus = getHeldVirus();
		int heldAntiVirus = getHeldAntiVirus();
		Literal handDecision = Literal.parseLiteral("handBroadcastDecision("+heldAntiVirus+","+heldVirus+")");
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