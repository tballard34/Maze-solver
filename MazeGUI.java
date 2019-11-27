import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import java.util.Date;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Queue;

public class MazeGUI extends JPanel{

	//maze components
	private JButton[][] maze;
	private int rSize;
	private int cSize;
	private int finishR;
	private int finishC;
	private MazeLocation[][] tiles;
	//tell user what to do
	private JLabel top;
	//bottom buttons
	private JComboBox<String> menu;
	private JButton run;
	private JButton reset;
	//slow program down
	private static final int MAXBREAKTIME = 500;
	private int breakTime;
	private JSlider slider;

	
	public MazeGUI() {

		setLayout(new BorderLayout());

		//start setup through the console
		//ask user to make dimensions for the maze

		//if size is less than 2 defaults to 2
		//if size is greater than 75 defaults to 75
		Scanner console = new Scanner (System.in);
		System.out.println("Enter the number of rows you want");
		int r = console.nextInt();
		if (r < 2) {
			rSize = 2;
		}
		else if (r > 75) {
			rSize = 75;
		}
		else {
			rSize = r;
		}

		System.out.println("\nEnter the number of columns you want");
		int c = console.nextInt();
		if (c < 2) {
			cSize = 2;
		}
		else if (c > 75) {
			cSize = 75;
		}
		else {
			cSize = c;
		}

		//setFinish
		//will default to (1, 1) if row and col is 0 or less
		System.out.println("\nEnter the number of row then column for the finish point");
		finishR = console.nextInt();
		finishC = console.nextInt();
		if (finishR >= rSize) {
			finishR = rSize - 1;
		}
		else if (finishR <= 0) {
			finishR = 1;
		}

		if (finishC >= cSize) {
			finishC = cSize - 1;
		}
		else if (finishC <= 0) {
			finishC = 1;
		}
		console.close();

		//construct GUI

		//center panel (maze)
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(rSize, cSize));
		add(center, BorderLayout.CENTER);

		//construct random maze using probability
		maze = new JButton[rSize][cSize];
		tiles = new MazeLocation[rSize][cSize];
		double random = 0;
		//create better odds for a non-wall if surrounded by non-walls
		double wallOddsMinus = .45;
		//odds their will be a wall
		double wallOdds = .80;
		double totalWallOdds = wallOdds;

		for (int row = 0; row < rSize; row++) {
			for (int col = 0; col < cSize; col++) {

				//create better odds for links between non-wall parts
				if (row > 0 && !tiles[row - 1][col].getWall()) {
					totalWallOdds -= wallOddsMinus;
				}
				else if (col > 0 && !tiles[row][col - 1].getWall()) {
					totalWallOdds -= wallOddsMinus;
				}

				//construct buttons
				maze[row][col] = new JButton();
				random = Math.random();
				if (random < totalWallOdds) {
					tiles[row][col] = new MazeLocation(row, col, true);
					maze[row][col].setBackground(Color.BLACK);
				}
				else {
					tiles[row][col] = new MazeLocation(row, col, false);
					maze[row][col].setBackground(Color.WHITE);
				}
				maze[row][col].setEnabled(true);
				maze[row][col].addActionListener(new MazeListener(row, col));
				center.add(maze[row][col]);

				totalWallOdds = wallOdds;
			}
		}
		//define start (0, 0) and finish points and set them up
		maze[0][0].setBackground(Color.green); //start
		tiles[0][0].setWall(false);

		maze[finishR][finishC].setBackground(Color.RED); //finish
		tiles[finishR][finishC].setFinish(true);
		tiles[finishR][finishC].setWall(false);

		//top panel
		JPanel nPanel = new JPanel();
		nPanel.setLayout(new FlowLayout());
		add(nPanel,  BorderLayout.NORTH);

		top = new JLabel("Modify Your Maze");
		top.setFont(new Font("ariel", Font.PLAIN, 18));
		nPanel.add(top);

		//bottom panel
		JPanel sPanel = new JPanel();
		sPanel.setLayout(new FlowLayout());
		add(sPanel, BorderLayout.SOUTH);

		String[] s = {"Recursion", "Breadth-first Search", "Depth-first Search"};
		menu = new JComboBox<String>(s);
		JLabel slow = new JLabel("SLOW");
		slider = new JSlider();
		JLabel fast = new JLabel("FAST");
		run = new JButton("RUN");
		reset = new JButton("RESET");

		slow.setFont(new Font("ariel", Font.PLAIN, 8));
		fast.setFont(new Font("ariel", Font.PLAIN, 8));

		menu.setEnabled(true);
		run.setEnabled(true);
		reset.setEnabled(true);

		sPanel.add(menu);
		sPanel.add(slow);
		sPanel.add(slider);
		sPanel.add(fast);
		sPanel.add(run);
		sPanel.add(reset);

		run.addActionListener(new RunListener());
		reset.addActionListener(new ResetListener());

	}//MazeGUI
	
	
	//maze (grid) ActionListener
	private class MazeListener implements ActionListener{
		
		int row;
		int col;

		public MazeListener(int r, int c) {
			row = r;
			col = c;
		}

		public void actionPerformed (ActionEvent e) {
			
			if ((row != 0 || col != 0) && !tiles[row][col].getFinish()) { //check not finish or start point
				if (tiles[row][col].getWall()) {
					maze[row][col].setBackground(Color.WHITE);
					tiles[row][col].setWall(false);
				}
				else {
					maze[row][col].setBackground(Color.BLACK);
					tiles[row][col].setWall(true);
				}
			}
		}
	}//MazeListener

	
	//run button ActionListener 
	private class RunListener implements ActionListener{

		public void actionPerformed (ActionEvent e) {

			//change text in top panel
			top.setText("Running");

			//disable buttons during run
			for (int r = 0; r < rSize; r++) {
				for (int c = 0; c < cSize; c++) {
					maze[r][c].setEnabled(false);
				}
			}
			
			//disable reset button during run
			reset.setEnabled(false);

			//set the break time in between moves
			setBreakTime();

			//run specified algorithm
			new Thread(new Runnable( ) {
				
				public void run() {
					
					if (menu.getSelectedIndex() == 0) {
						recursionSolver();
					}
					else if (menu.getSelectedIndex() == 1) {
						breadthSolver();
					}
					else {
						depthSolver();
					}
					//enable reset button
					reset.setEnabled(true);
				}
			}).start();
			
		}
	}//RunListener

	
	//reset button ActionListener
	private class ResetListener implements ActionListener{

		public void actionPerformed (ActionEvent e) {

			//reset board
			for (int r = 0; r < rSize; r++) {
				for (int c = 0; c < cSize; c++) {
					maze[r][c].setEnabled(true);
					tiles[r][c].setVisited(false);
					if (!tiles[r][c].getWall()) {
						maze[r][c].setBackground(Color.WHITE);
					}
				}
			}
			maze[0][0].setBackground(Color.GREEN);
			maze[finishR][finishC].setBackground(Color.RED);

			//reset top label
			top.setText("Modify Your Maze");
		}
	}//ResetListener

	//makes computer stall for a inputed time in milliseconds
	public void sleep(long milliseconds) {

		Date d ;
		long start, now ;
		d = new Date() ;
		start = d.getTime() ;
		do { d = new Date() ; now = d.getTime() ; }
		while ( (now - start) < milliseconds ) ;
	}
	
	//sets the break time for program speed
	public void setBreakTime() {
		
		int value = slider.getValue();
		//function to determine breakTime from slider
		breakTime = ((100 - value) * MAXBREAKTIME / 100);
	}
	

	//recursive maze solver method
	private void recursionSolver() {

		boolean b = recursionSolverHelper(0, 0);
		if (b) {
			top.setText("ESCAPED MAZE");
		}
		else {
			top.setText("NO PATH");
		}
	}
	private boolean recursionSolverHelper(int r, int c) {
		
		if (r >=  0 && r < rSize && c >= 0 && c < cSize && !tiles[r][c].getVistited() && !tiles[r][c].getWall()) {

			tiles[r][c].setVisited(true);

			if (tiles[r][c].getFinish()) {
				return true;
			}
			
			if (r != 0 || c != 0) {
				maze[r][c].setBackground(Color.CYAN);
			}
			
			//stall program in between moves
			setBreakTime();
			sleep(breakTime);

			if (recursionSolverHelper(r + 1, c)  ||
				recursionSolverHelper(r, c + 1)  ||
				recursionSolverHelper(r - 1, c)  ||
				recursionSolverHelper(r, c - 1)    ){
				return true;
			}
		}

		return false;
	}//recursiveSolver

	
	//breadth-first Solver method
	private void breadthSolver() {

		Queue<MazeLocation> que = new LinkedList<>();
		que.add(tiles[0][0]);

		int r;
		int c;

		//first iteration
		MazeLocation copy = que.remove();
		r = copy.getRow();
		c = copy.getColumn();
		tiles[r][c].setVisited(true);
		if (!tiles[0][1].getWall()) { //right
			que.add(tiles[0][1]);
		}
		if (!tiles[1][0].getWall()) { //down
			que.add(tiles[1][0]);
		}

		//iterate through rest of maze
		while (!que.isEmpty() && !que.peek().getFinish()) {

			copy = que.remove();

			r = copy.getRow();
			c = copy.getColumn();

			maze[r][c].setBackground(Color.CYAN);
			tiles[r][c].setVisited(true);

			//stall program in between moves
			setBreakTime();
			sleep(breakTime);

			//checks down, right, up, left areas in that order and pushes them onto the queue
			
			if (c > 0 && !tiles[r][c - 1].getWall() && !tiles[r][c - 1].getVistited() && !que.contains(tiles[r][c - 1])) {//left
				que.add(tiles[r][c - 1]);
			}
			if (r > 0 && !tiles[r - 1][c].getWall() && !tiles[r - 1][c].getVistited() && !que.contains(tiles[r - 1][c])) {//up
				que.add(tiles[r - 1][c]);
			}
			if (c < cSize - 1 && !tiles[r][c + 1].getWall() && !tiles[r][c + 1].getVistited() && !que.contains(tiles[r][c + 1])) {//right
				que.add(tiles[r][c + 1]);
			}
			if (r < rSize - 1 && !tiles[r + 1][c].getWall() && !tiles[r + 1][c].getVistited() && !que.contains(tiles[r + 1][c])) {//down
				que.add(tiles[r + 1][c]);
			}
		}

		if (!que.isEmpty() && que.peek().getFinish()) {
			top.setText("ESCAPED MAZE");
		}
		else {
			top.setText("NO PATH!");
		}

	}//breadthSolver

	
	//depth-first solver method
	private void depthSolver() {

		Stack<MazeLocation> stk = new Stack<MazeLocation>();
		stk.push(tiles[0][0]);

		int r;
		int c;
		
		//first iteration
		MazeLocation copy = stk.pop();
		r = copy.getRow();
		c = copy.getColumn();
		tiles[r][c].setVisited(true);
		if (!tiles[0][1].getWall()) { //right
			stk.push(tiles[0][1]);
		}
		if (!tiles[1][0].getWall()) { //down
			stk.push(tiles[1][0]);
		}

		//iterate through rest of maze
		while (!stk.isEmpty() && !stk.peek().getFinish()) {

			copy = stk.pop();

			r = copy.getRow();
			c = copy.getColumn();

			maze[r][c].setBackground(Color.CYAN);
			tiles[r][c].setVisited(true);
			
			//stall program in between moves
			setBreakTime();
			sleep(breakTime);
			
			//checks down, right, up, left areas in that order and pushes them onto the stack
			if (c > 0 && !tiles[r][c - 1].getWall() && !tiles[r][c - 1].getVistited()) {//left
				stk.push(tiles[r][c - 1]);
			}
			if (r > 0 && !tiles[r - 1][c].getWall() && !tiles[r - 1][c].getVistited()) {//up
				stk.push(tiles[r - 1][c]);
			}
			if (c < cSize - 1 && !tiles[r][c + 1].getWall() && !tiles[r][c + 1].getVistited()) {//right
				stk.push(tiles[r][c + 1]);
			}
			if (r < rSize - 1 && !tiles[r + 1][c].getWall() && !tiles[r + 1][c].getVistited()) {//down
				stk.push(tiles[r + 1][c]);
			}
		}

		if (!stk.isEmpty() && stk.peek().getFinish()) {
			top.setText("ESCAPED MAZE");
		}
		else {
			top.setText("NO PATH!");
		}
	}//depthSolver
}//MazeGui
