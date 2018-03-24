import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.bb.*;
import java.util.*;
import java.util.logging.Logger;

public class AntiVirusBeliefBase extends DefaultBeliefBase
{
	@Override
	public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u)
	{
		if (!l.getFunctor().equals("dirt"))
			return super.getCandidateBeliefs(l, u);
		
		Iterator<Literal> i ;
		
		try
		{
			i = super.getCandidateBeliefs(l ,u);
			Literal posLiteral = getMyPosition(); 
			Term[] p_terms = posLiteral.getTermsArray();
			int x = Integer.parseInt(p_terms[0].toString());
			int y = Integer.parseInt(p_terms[1].toString());
			
			Literal closestDirt = i.next();
			Term[] a_terms = closestDirt.getTermsArray();
			int x1 = Integer.parseInt(a_terms[0].toString());
			int y1 = Integer.parseInt(a_terms[1].toString());
			
			while(i.hasNext())
			{
				Literal lit = i.next();
				Term[] b_terms = lit.getTermsArray();
				int x2 = Integer.parseInt(b_terms[0].toString());
				int y2 = Integer.parseInt(b_terms[1].toString());
				
				// calculate the distance from position to each of these two dirts
				int d1 = Math.abs(x1 - x) + Math.abs(y1 - y);
				int d2 = Math.abs(x2 - x) + Math.abs(y2 - y);
				
				if (d2 < d1)
				{
					x1 = x2;
					y1 = y2;
				}
				closestDirt = Literal.parseLiteral("dirt(" + x1 + ", " + y1 + ")");
			}
			List<Literal> result = new ArrayList<Literal>();
			result.add(closestDirt);
			return result.iterator();
			
		}
		catch(NullPointerException ex)
		{
			return super.getCandidateBeliefs(l, u);
		}
		
		
	}
	
	private Literal getMyPosition()
	{
		Iterator<Literal> percepts = getPercepts();
		while (percepts.hasNext())
		{
			Literal p = percepts.next();
			if (p.getFunctor().equals("position"))
			{
				return p;
			}
		}
		return null;
	}
}