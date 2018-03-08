import jason.environment.*;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.*;

public class View extends GridWorldView
{
	private static final Color VIRUS_COLOR = new Color(188, 15, 15);
	private static final Color ROGUE_COLOR = new Color(244, 66, 66);
	private static final Color ANTI_VIRUS_COLOR = new Color(66, 134, 244);
	private static final Color TABLE_COLOR = new Color(188, 99, 43);
	
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
	public void update(int x, int y){
		//to fix drawing bugs, updates the entire board every update
		update();
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
	}
	
	public void drawAgent(Graphics g, int x, int y, Color c, int id){
		
		if(model.getRole(id) == model.VIRUS_ROLE)
			super.drawAgent(g,x,y,VIRUS_COLOR,id);
		if(model.getRole(id) == model.ROGUE_ROLE)
			super.drawAgent(g,x,y,ROGUE_COLOR,id);
		if(model.getRole(id) == model.ANTI_VIRUS_ROLE)
			super.drawAgent(g,x,y,ANTI_VIRUS_COLOR,id);
	}
}