import javax.swing.JFrame;

public class MazeDriver {
	
	//main method
	public static void main (String [] args) {
		
		JFrame maze = new JFrame("Maze Solver");
		
		maze.setContentPane(new MazeGUI());
		maze.setLocation(200, 200);
		maze.setSize(800, 800);
		maze.setVisible(true);
		maze.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}//main

}
