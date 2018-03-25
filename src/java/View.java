import jason.environment.*;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.*;

public class View extends GridWorldView
{
	private static final Color VIRUS_COLOR = new Color(188, 15, 15);
	private static final Color ROGUE_COLOR = new Color(244, 66, 66);
	private static final Color DEAD_COLOR = Color.GRAY;
	private static final Color ANTI_VIRUS_COLOR = new Color(66, 134, 244);
	private static final Color TABLE_COLOR = new Color(188, 99, 43);
	
	Font defaultFont = new Font("Arial", Font.BOLD, 15);
	Model model;
	
	public View(Model _model)
	{
		super(_model, "My View", 700);
		this.model = _model;
		setVisible(true);
		repaint();
	}
	
	public void updateAgents(){
		//this is a bad hack to update the rolls of the agents
		//nothing actually changes in the environment,
		//so it doesnt update automatically
		for(int y = 0; y < Model.GRID_HEIGHT; y++)
			for(int x = 0; x < Model.GRID_WIDTH; x++)
				if(model.getAgAtPos(x,y) != -1)
					update(x,y);
	}
	
	public void updateMessages()
	{
		for(int y = 0; y < Model.GRID_HEIGHT; y++)
			for(int x = 0; x < Model.GRID_WIDTH; x++)
				if(model.hasObject(Model.MESSAGE,x,y)){
					update(x-1,y);
					update(x+1,y);
					update(x,y);
				}
	}
	
	@Override
	public void draw(Graphics g, int x , int y, int object)
	{
		if(object == Model.TABLE){
			g.setColor(TABLE_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(TABLE_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
		}
		if(object == Model.BOARD){
			g.setColor(Color.GRAY);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(Color.DARK_GRAY);
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
		}
		if(object == Model.VIRUS_CARD){
			g.setColor(ROGUE_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(ROGUE_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
		}
		if(object == Model.ANTI_VIRUS_CARD){
			g.setColor(ANTI_VIRUS_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(ANTI_VIRUS_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
		}
		if(object == Model.MESSAGE)
		{
			int ag = model.getAgAtPos(x, y + 1);
			String msg = model.getMessage(ag);
			g.setColor(Color.BLACK);
			if(msg != null)
				drawString(g, x, y, defaultFont, msg);
		}
	}
	
	public void drawAgent(Graphics g, int x, int y, Color c, int id){
		
		String display = "";
		if(!model.getAlive(id))
			super.drawAgent(g,x,y,DEAD_COLOR,-1);
		else if(model.getRole(id) == model.VIRUS_ROLE)
			super.drawAgent(g,x,y,VIRUS_COLOR,-1);
		else if(model.getRole(id) == model.ROGUE_ROLE)
			super.drawAgent(g,x,y,ROGUE_COLOR,-1);
		else if(model.getRole(id) == model.ANTI_VIRUS_ROLE)
			super.drawAgent(g,x,y,ANTI_VIRUS_COLOR,-1);
		
		if(model.getKernel() == id)
			display += "K";
		else if(model.getScheduler() == id)
			display += "S";
		else if(model.getExKernel() == id && model.getNumAlive() > 5)
			display += "Ex-K";
		else if(model.getExScheduler() == id)
			display += "Ex-S";
		else if(model.getElectedScheduler() == id)
			display += "Elect";
		
		g.setColor(Color.BLACK);
		drawString(g, x, y, defaultFont, display);
		g.drawString(""+id, x*cellSizeW+5,(y+1)*cellSizeH-5);
	}
}