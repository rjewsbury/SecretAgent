import jason.environment.*;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;

public class View extends GridWorldView
{
	private static final Color VIRUS_COLOR = new Color(188, 15, 15);
	private static final Color ROGUE_COLOR = new Color(244, 66, 66);
	private static final Color DEAD_COLOR = Color.GRAY;
	private static final Color ANTI_VIRUS_COLOR = new Color(66, 134, 244);
	private static final Color TABLE_COLOR = new Color(188, 99, 43);
	private static final Color DECK_COLOR = new Color(255, 165, 0);
	
	Font defaultFont = new Font("Arial", Font.BOLD, 15);
	Model model;
	
	public View(Model _model, ChangeListener l, int initialVal)
	{
		//creates a 700x700 view for the model
		super(_model, "My View", 700);
		this.model = _model;
		
		//adds a speed slider to the view
		JSlider slider = new JSlider(1,300,initialVal);
		slider.addChangeListener(l);
		getContentPane().add(slider,BorderLayout.SOUTH);
		setVisible(true);
		repaint();
	}
	
	public void updateAgents(){
		//this updates the roles of the agents displayed on screen
		//no cells actually change in the environment,
		//so it doesnt update automatically
		for(int y = 0; y < Model.GRID_HEIGHT; y++)
			for(int x = 0; x < Model.GRID_WIDTH; x++)
				if(model.getAgAtPos(x,y) != -1)
					update(x,y);
	}
	
	public void updateMessages()
	{
		//clears previous messages, and draws new ones
		//messages can be at most 3 tiles wide
		final int MAX_WIDTH = 3;
		
		for(int y = 0; y < Model.GRID_HEIGHT; y++)
			for(int x = 0; x < Model.GRID_WIDTH; x++)
				if(model.hasObject(Model.MESSAGE,x,y)){
					//clear the surrounding cells of old messages
					//this is necessary because messages are larger than a single cell
					for(int i = -MAX_WIDTH/2; i <= MAX_WIDTH/2; i++)
						update(x+i,y);
					//then draw the new message
					update(x,y);
				}
	}
	
	public void updateDecks(){
		update(10,5);
		update(10,7);
	}
	
	@Override
	public void draw(Graphics g, int x , int y, int object)
	{
		//all blocks are filled in, and given a darker outline
		if(object == Model.TABLE){
			g.setColor(TABLE_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(TABLE_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
		}
		//the board is given a letter for powers
		if(object == Model.BOARD){
			g.setColor(Color.GRAY);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(Color.DARK_GRAY);
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			
			if(y==5 && model.getBoardAbility(x-3) == Model.DELETE_AGENT)
				drawString(g, x, y, defaultFont, "B");
		}
		//cards are given a letter
		if(object == Model.VIRUS_CARD){
			g.setColor(ROGUE_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(ROGUE_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(Color.BLACK);
			drawString(g, x, y, defaultFont, "V");
		}
		if(object == Model.ANTI_VIRUS_CARD){
			g.setColor(ANTI_VIRUS_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(ANTI_VIRUS_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(Color.BLACK);
			drawString(g, x, y, defaultFont, "A");
		}
		//messages are drawn above their player
		if(object == Model.MESSAGE)
		{
			int ag = model.getAgAtPos(x, y + 1);
			String msg = model.getMessage(ag);
			g.setColor(Color.BLACK);
			if(msg != null)
				drawString(g, x, y, defaultFont, msg);
		}
		if(object == Model.DECK){
			g.setColor(DECK_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(DECK_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			
			g.setColor(Color.BLACK);
			drawString(g, x, y, defaultFont, ""+model.getDeckSize());
		}
		
		if(object == Model.DISCARD){
			g.setColor(DECK_COLOR);
			g.fillRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			g.setColor(DECK_COLOR.darker());
			g.drawRect(x*cellSizeW,y*cellSizeH,cellSizeW-1,cellSizeH-1);
			
			g.setColor(Color.BLACK);
			drawString(g, x, y, defaultFont, ""+model.getDiscardSize());
		}
	}
	
	public void drawAgent(Graphics g, int x, int y, Color c, int id){
		//agents are given different colors depending on their role
		//and change color once they die
		if(!model.getAlive(id))
			super.drawAgent(g,x,y,DEAD_COLOR,-1);
		else if(model.getRole(id) == model.VIRUS_ROLE)
			super.drawAgent(g,x,y,VIRUS_COLOR,-1);
		else if(model.getRole(id) == model.ROGUE_ROLE)
			super.drawAgent(g,x,y,ROGUE_COLOR,-1);
		else if(model.getRole(id) == model.ANTI_VIRUS_ROLE)
			super.drawAgent(g,x,y,ANTI_VIRUS_COLOR,-1);
		
		//the agent's role is displayed in their center
		String display = "";
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
		
		//draws the agent's number in the corner of their cell
		g.drawString(""+id, x*cellSizeW+5,(y+1)*cellSizeH-5);
	}
}