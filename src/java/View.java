import jason.environment.*;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.*;

public class View extends GridWorldView
{
	
	Model model;
	
	public View(Model _model)
	{
		super(_model, "My View", 800);
		this.model = _model;
		Font defaultFont = new Font("Arial", Font.BOLD, 15);
		setVisible(true);
		repaint();
	}
	
	@Override
	public void draw(Graphics g, int x , int y, int object)
	{
	}
	
}