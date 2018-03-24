import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.logging.Logger;

public class VirusBeliefBase extends DefaultBeliefBase
{
	@Override
	public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u)
	{
		if (!l.getFunctor().equals("schedulerCandidate"))
			return super.getCandidateBeliefs(l, u);
		
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
}