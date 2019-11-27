import java.awt.Point;

public class MazeLocation {

	private Point p;
	private boolean wall;
	private boolean visited;
	private boolean finish;
	
	public MazeLocation(int row, int col, boolean wall) {
		p = new Point(row, col);
		this.wall = wall;
		visited = false;
	}
	
	//mutator methods
	//true means its finish
	public void setFinish(boolean b) {
		finish = b;
	}
	
	//true means its visited
	public void setVisited(boolean b) {
		visited = b;
	}
	
	//true means there is a wall
	public void setWall(boolean b) {
		wall = b;
	}
	
	//accessor methods
	public int getRow() {
		return (int) p.getX();
	}
	
	public int getColumn() {
		return (int) p.getY();
	}
	
	//returns true if their is a wall
	public boolean getVistited() {
		return visited;
	}
	
	//returns true if their is a wall
	public boolean getWall() {
		return wall;
	}
	
	//returns true if it is the finish
	public boolean getFinish() {
		return finish;
	}
	
}
