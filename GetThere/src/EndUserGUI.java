import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

///**
//* Created by Lumbini on 11/7/2015.
//* modified by Jeff
// * */
//

public class EndUserGUI extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 2270760135813536905L;
	private static LinkedList<Map> maps = new LinkedList<Map>();
	private static LinkedList<Node> currentStartNodes = new LinkedList<Node>();
	private static LinkedList<Edge> currentStartEdges = new LinkedList<Edge>();
	private static LinkedList<Node> currentEndNodes = new LinkedList<Node>();
	private String[] startRooms;
	private String[] endRooms = new String[100];
	private BufferedImage currentMapFile;

	private JTextArea directions;

	private JFrame frame;		//Creates the main frame for the GUI
	private JPanel uiPanel;		//Panel to hold the interface buttons
	private JPanel mapPanel;	//Panel to hold the map
	private Image mapImage;		//Represents the map to be chosen

	//Labels on the GUI
	private JLabel startPoint;
	private JLabel buildingStart;
	private JLabel roomStart;
	private JLabel endPoint;
	private JLabel buildingEnd;
	private JLabel roomEnd;

	//Combo Boxes on the GUI
	private JComboBox<String> startBuildingSEL;
	private JComboBox<String> startRoomSEL;
	private JComboBox<String> endBuildingSEL;
	private JComboBox<String> endRoomSEL;

	//Buttons on the UI
	private JButton searchButton;
	Graphics g;
	boolean updatePath = false;
	private JButton leftArrow;
	private JButton rightArrow;

	//Start-End Nodes
	private Node startNode;
	private Node endNode;
	private LinkedList<Node> listPath = new LinkedList<Node>();
	private Djikstra pathCalc;

	//List of buildings to be shown to the user
	private String buildingSelectedSTART;	//track which building is selected to start in.
	private String buildingSelectedEND;		//track which building is selected to end in.
	public ImageIcon mapIcon;


	private int floor = -1;


	/**
	 * Create the application.
	 */
	public EndUserGUI(){
		initialize();
	}

	//Launch the application. 

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		maps = (LinkedList<Map>) deserialize("MapList");
		
		EventQueue.invokeLater(new Runnable() {
			EndUserGUI window = new EndUserGUI();
			public void run() {
				try {
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// saves Map object "m" in a file named "s"
		public void serialize(String s, LinkedList<Map> maplist){
			
			try {
				FileOutputStream fileOut = new FileOutputStream(s + ".ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(maplist);
				out.close();
				fileOut.close();
				System.out.println("Serialized data is saved in " + s + ".ser");
			} catch(IOException i){
				i.printStackTrace();
			}
		}
		
		// loads the map stored in file name "s"
		public static Object deserialize(String s){
			Object m = null;
			try
			{
				FileInputStream fileIn = new FileInputStream(s + ".ser");
				ObjectInputStream in = new ObjectInputStream(fileIn);
				m = in.readObject();
				in.close();
				fileIn.close();
			}catch(IOException i)
			{
				i.printStackTrace();

			}catch(ClassNotFoundException c)
			{
				System.out.println("Map class not found");
				c.printStackTrace();
		
			}
		return m;
		}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		//Frame operations
		frame = new JFrame();
		frame.setBounds(100, 100, 1030, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
		frame.setTitle("Get There");
		frame.setResizable(false);
		frame.setVisible(true);

		//Panel Operations
		uiPanel = new JPanel();
		frame.getContentPane().add(uiPanel);
		uiPanel.setLayout(null);

		mapPanel = new JPanel();
		mapPanel.setBounds(5, 5, 750, 620);
		uiPanel.add(mapPanel);
		mapPanel.add(new MyGraphics());

		//Creating Labels
		startPoint = new JLabel("FROM");
		startPoint.setBounds(780, 6, 132, 29);

		buildingStart = new JLabel("Select Building:");
		buildingStart.setBounds(762, 26, 132, 29);

		roomStart = new JLabel("Select Room:");
		roomStart.setBounds(900, 26, 132, 29);

		endPoint = new JLabel("TO");
		endPoint.setBounds(780, 72, 132, 29);

		buildingEnd = new JLabel("Select Building:");
		buildingEnd.setBounds(762, 92, 132, 29);

		roomEnd = new JLabel("Select Room:");
		roomEnd.setBounds(900, 92, 132, 29);


		//Add Labels to the uiPanel
		uiPanel.add(startPoint);
		uiPanel.add(buildingStart);
		uiPanel.add(roomStart);
		uiPanel.add(endPoint);
		uiPanel.add(buildingEnd);
		uiPanel.add(roomEnd);
		
		startRoomSEL = new JComboBox<String>();
		startRoomSEL.setBounds(893, 50, 132, 29);
		startRoomSEL.setEditable(false);
		startRoomSEL.setVisible(true);

		//Construct Combo boxes to select start point
		startBuildingSEL = new JComboBox<String>();
		startBuildingSEL.setBounds(755, 50, 132, 29);
		startBuildingSEL.setEditable(false);
		startBuildingSEL.setVisible(true);
		startBuildingSEL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int indexOfCurrentMap;
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)e.getSource();
				buildingSelectedSTART = (String)cb.getSelectedItem();
				for(indexOfCurrentMap = 0; indexOfCurrentMap < maps.size(); ++indexOfCurrentMap){
					if(buildingSelectedSTART.equals(maps.get(indexOfCurrentMap).getMapName()))
						break;
				}
				currentStartNodes = maps.get(indexOfCurrentMap).getNodes();
				startRooms = new String[currentStartNodes.size()];
				currentStartEdges = maps.get(indexOfCurrentMap).getEdges();
				currentMapFile = maps.get(indexOfCurrentMap).getImage();
				startRoomSEL.removeAllItems();
				for(int i = 0; i < currentStartNodes.size(); ++i){
					startRooms[i] = currentStartNodes.get(i).getName();
					if(startRooms[i] != "")
						startRoomSEL.addItem(startRooms[i]);
				}
				uiPanel.repaint();
		        frame.repaint();
			}
		});
		
		for (int i = 0; i < maps.size(); i++) {
	        if(maps.get(i).getMapName() != null)
	        	startBuildingSEL.addItem(maps.get(i).getMapName());
	    }

		endRoomSEL = new JComboBox<String>();
		endRoomSEL.setBounds(893, 116, 132, 29);
		endRoomSEL.setEditable(false);
		endRoomSEL.setVisible(true);

		//Construct Combo boxes to select end point
		endBuildingSEL = new JComboBox<String>();
		endBuildingSEL.setBounds(755, 116, 132, 29);
		endBuildingSEL.setEditable(false);
		endBuildingSEL.setVisible(true);
		endBuildingSEL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int indexOfCurrentMap;
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)e.getSource();
				buildingSelectedEND = (String)cb.getSelectedItem();
				for(indexOfCurrentMap = 0; indexOfCurrentMap < maps.size(); ++indexOfCurrentMap){
					if(buildingSelectedEND.equals(maps.get(indexOfCurrentMap).getMapName()))
						break;
				}
				currentEndNodes = maps.get(indexOfCurrentMap).getNodes();
				currentMapFile = maps.get(indexOfCurrentMap).getImage();
				endRoomSEL.removeAllItems();
				for(int i = 0; i < currentEndNodes.size(); ++i){
					endRooms[i] = currentEndNodes.get(i).getName();
					if(endRooms[i] != "")
						endRoomSEL.addItem(endRooms[i]);
				}
				uiPanel.repaint();
		        frame.repaint();
			}
		});
		
		for (int i = 0; i < maps.size(); i++) {
	           if(maps.get(i).getMapName() != null)
	        	   endBuildingSEL.addItem(maps.get(i).getMapName());
	    }

		

		//Add Combo Boxes to UIPanel

		uiPanel.add(startBuildingSEL);
		uiPanel.add(startRoomSEL);
		uiPanel.add(endBuildingSEL);
		uiPanel.add(endRoomSEL);

		//Construct button and add button to uiPanel
		searchButton = new JButton ("Search");
		searchButton.setBounds(820, 150, 132, 29);
		uiPanel.add(searchButton);


		leftArrow = new JButton("<<");
		leftArrow.setBounds(275, 630, 80, 29);
		uiPanel.add(leftArrow);

		rightArrow = new JButton(">>");
		rightArrow.setBounds(365, 630, 80, 29);
		uiPanel.add(rightArrow);

		JLabel instructions = new JLabel("How to get there?");
		instructions.setBounds(762, 180, 132, 29);
		uiPanel.add(instructions);

		directions = new JTextArea();
		directions.setBounds(762, 210, 255, 450);
		directions.setEditable(false);
		JScrollPane scrollDire = new JScrollPane(directions);
		scrollDire.setBounds(762, 210, 255, 450);
		scrollDire.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		uiPanel.add(scrollDire);


		//Construct buttons and add action listener
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				int i;
				updatePath = true;
				uiPanel.setVisible(true);
				frame.setVisible(true);
				pathCalc = new Djikstra();

				for (i = 0; i < currentStartNodes.size(); i++){
					if(startRoomSEL.getSelectedItem() == currentStartNodes.get(i).getName())
						startNode = currentStartNodes.get(i);
				}
				for(i = 0; i < currentEndNodes.size(); i++){
					if(endRoomSEL.getSelectedItem() == currentEndNodes.get(i).getName())
						endNode = currentEndNodes.get(i);
				}

				System.out.println(startBuildingSEL.getSelectedItem());
				System.out.println(floor);
				listPath = pathCalc.navigate(startNode, endNode);
				directions.setText(pathCalc.gpsInstructions(pathCalc.navigate(startNode, endNode)));
				System.out.println("check List: " + listPath.size());
				repaint();
				revalidate();
			}
		});

		uiPanel.setVisible(true);
		frame.setVisible(true);
	}

	public class MyGraphics extends JComponent implements MouseMotionListener{

		private static final long serialVersionUID = 1L;
		private static final int SquareWidth = 5;
		MyGraphics() {
			setPreferredSize(new Dimension(760, 666));
			addMouseMotionListener(this);
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(currentMapFile, 0, 0, this);
			repaint();
			revalidate();
			
			GeneralPath path = null;

			repaint();
			revalidate();
			
			g.drawImage(mapImage, 0, 0, this);
			
			for (int i = 0; i < currentStartNodes.size(); i++){
				((Graphics2D)g).draw(new Rectangle (currentStartNodes.get(i).getX(), currentStartNodes.get(i).getY(), SquareWidth, SquareWidth));
			}

			for (int i = 0; i < currentStartEdges.size(); i++){
				((Graphics2D)g).draw(new Line2D.Double(currentStartEdges.get(i).getNode1().getX(), currentStartEdges.get(i).getNode1().getY(),currentStartEdges.get(i).getNode2().getX(),currentStartEdges.get(i).getNode2().getY() ));
			}

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			BasicStroke s = new BasicStroke(
					5.5f, 
					BasicStroke.CAP_ROUND, 
					BasicStroke.JOIN_ROUND);
			g2d.setStroke(s);
			g2d.setColor(Color.BLACK);
			if (path==null && updatePath == true) {
				removeAll();
				int i;
				path = new GeneralPath();
				path.moveTo(listPath.getFirst().getX(), listPath.getFirst().getY()); 
				for (i=0; i<listPath.size(); i++){
					path.lineTo(listPath.get(i).getX(),listPath.get(i).getY());
					g2d.draw(path);
				}
				g2d.draw(path);
				g2d.setStroke(new BasicStroke(3));
				g2d.setColor(Color.RED);
				g2d.draw(path);
				repaint();
				revalidate();
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			int x = e.getX();
			int y = e.getY();
			System.out.println("X: " + x + " Y: " +y);

		}
		public void mousePressed(MouseEvent evt) {
			int x = evt.getX();
			int y = evt.getY();
			System.out.println("X: " + x + " Y: " +y);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}
