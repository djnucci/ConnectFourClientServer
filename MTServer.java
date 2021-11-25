import java.io.*;
import java.net.*;
import java.util.ArrayList;

// @author Daniel Nucci
// Assignment #1 for Dr. Q SOFE 4790
// Multi-threaded Connect Four Server (MTServer.java) / Client (ConnectFourClient.java)

public class MTServer {
	public final int THREAD_LIMIT 		= 20; 		// limits number of players allowed to be connect to the server
	public final int SERVER_PORT_NUMBER = 3500;	 	// the port the server opens on, must be the same as the clients port
	public int numThreads 				= 0;		// the number of threads currently active on the server
	public boolean isVerboseServer 		= false; 	// controls whether or not the server prints out error messages as they occur
	
	ServerSocket serverSocket; 
	Socket socket;
	ArrayList<Match> ServerMatches = new ArrayList<Match>(0); // holds all the currently active matches

	public MTServer(boolean isVerbose) {
		isVerboseServer = isVerbose;
	}
	
	public static void main(String args[]) throws Exception {

		// args from the command line and handling for said args
		boolean vArg = false;
		boolean uArg = false;
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-u") && !uArg) {
					BufferedReader bR = new BufferedReader(new FileReader("README.md"));
					String nextLine = "", readMeString = "";
					while ((nextLine = bR.readLine()) != null) {
						readMeString += nextLine.replaceAll("#", "") + "\n";
					}
					System.out.print("\033[H\033[2J");
					System.out.println("You requested the README.md file, listing now...\n" + readMeString);
					uArg = true;
					bR.close();
					break;
				}
				 else if (args[i].equalsIgnoreCase("-v") && !vArg) {
					System.out.println("You have enabled verbose mode.");
					vArg = true;
				}
			}
		}

		// make a server and start it
		if (!uArg) { // if the README file is printed, no need to start the server
			MTServer mms = new MTServer(vArg);
			mms.run();
		}
	}

	// listen for new connections
	public void run() {
		try {
			serverSocket = new ServerSocket(SERVER_PORT_NUMBER);
			System.out.println("Server: listening on port " + serverSocket.getLocalPort() + " ...");
			while (true) {
				socket = serverSocket.accept();// Wait for a connection
				System.out.println("Server: Connection made with " + socket.getRemoteSocketAddress());
				new Connection(socket, this);
			}
		} catch (Exception e) {
			if (this.isVerboseServer) {
				e.printStackTrace();
			}
		}
	}

	public void makeBot(char colour) throws Exception {
		new ConnectFourAIClient(colour);
	}
}

// the main class that deals with the gameboard
class Connect4Board {

	// ANSI escape codes for text manipulation
	public static final String ANSI_RESET 				= "\u001B[0m";

	public static final String ANSI_BLACK 				= "\u001B[30m";
	public static final String ANSI_RED 				= "\u001B[31m";
	public static final String ANSI_YELLOW 				= "\u001B[33m";

	public static final String ANSI_BLUE_BACKGROUND 	= "\u001B[44m";
	public static final String ANSI_GREEN_BACKGROUND 	= "\u001B[42m";
	public static final String ANSI_PURPLE_BACKGROUND 	= "\u001B[45m";

	int[][] gameGrid = new int[7][6];
	boolean[][] polarizedYellowBoard = new boolean[7][6]; 	// true if there is a yellow in the curr. pos.
	boolean[][] polarizedRedBoard = new boolean[7][6];		// true if there is a red in the curr. pos.
	int[] LRP = {-1, -1}; //last recently placed
	ArrayList<int[]> victoryChips = new ArrayList<int[]>(); // chips used in the victory when the match ends

	// adds a chip to the board off the indicated pos and colour variables
	// after the chip has been added, a '-1' represtents red and a '1' reps yellow
	public boolean addChip(int pos, char colour) {
		for (int i = 0; i < gameGrid[pos].length; i++) {
			if (gameGrid[pos][i] == 0) {
				if (colour == 'r') {
					gameGrid[pos][i] = -1;
					LRP[0] = pos;
					LRP[1] = i;
					return true;
				} else if (colour == 'y') {
					gameGrid[pos][i] = 1;
					LRP[0] = pos;
					LRP[1] = i;
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	// returns a comma seperated per value, semi colon seperated serialized version of the game board (bottom first, top last)
	public String serializeBoard() {
		String returnString = "";

		for (int i = 0; i < gameGrid.length; i++) {
			for (int j = 0; j < gameGrid[i].length; j++) {
				returnString += gameGrid[i][j] + ",";
			}
			returnString = returnString.substring(0, returnString.length() - 1) + ";";
		}

		return returnString;
	}

	// returns, using ANSI escape codes, each connection's individual needs for a board render
	public String visualiseChips(boolean isNotSimpleConnection) {
		String returnStr = "";

		// i = yCoord, j = xCoord
		for (int i = gameGrid[0].length - 1; i >= 0; i--) {
			for (int j = 0; j < gameGrid.length; j++) {
				String strTempBuilder = ANSI_BLUE_BACKGROUND + " ";

				// highlight the last placed chip
				if (j == LRP[0] && i == LRP[1] && getVictoryState() == 0) {
					strTempBuilder = ANSI_GREEN_BACKGROUND + " ";
				}

				// highlight the chips used in the victory
				for (int k = 0; k < victoryChips.size(); k++) {
					if (j == victoryChips.get(k)[0] && i == victoryChips.get(k)[1] && getVictoryState() == -1) {
						strTempBuilder = ANSI_GREEN_BACKGROUND + " ";
					}
					else if (j == victoryChips.get(k)[0] && i == victoryChips.get(k)[1] && getVictoryState() == 1) {
						strTempBuilder = ANSI_PURPLE_BACKGROUND + " ";
					}
				}
				
				if (gameGrid[j][i] == -1) {
					strTempBuilder += ANSI_RED;
				} 
				else if (gameGrid[j][i] == 1) {
					strTempBuilder += ANSI_YELLOW;
				} 
				else {
					strTempBuilder += ANSI_BLACK;
				}

				if (!isNotSimpleConnection) {
					strTempBuilder += "●";
				} 
				else {
					if (gameGrid[j][i] == -1) {
						strTempBuilder += "r";
					} else if (gameGrid[j][i] == 1) {
						strTempBuilder += "y";
					} else {
						strTempBuilder += "x";
					}
				}

				returnStr += strTempBuilder + " " + ANSI_RESET;
			}
			returnStr = returnStr.substring(0, returnStr.length() - 1) + ANSI_RESET + ";";
		}
		return returnStr;
	}

	// returns if yellow, red or nobody is in a victory position
	public int getVictoryState() {
		// populate the polarized gameboards
		for (int i = 0; i < gameGrid.length; i++) {
			for (int j = 0; j < gameGrid[i].length; j++) {
				if (gameGrid[i][j] == 1) {
					polarizedYellowBoard[i][j] = true;
				} 
				else if (gameGrid[i][j] == -1) {
					polarizedRedBoard[i][j] = true;
				}
			}
		}

		// check the vertical victory line
		for (int i = 0; i < gameGrid[3].length; i++) {
			int posX = 3, posY = i;

			int currChipState = getChipVictoryState(gameGrid[posX][posY], posX, posY);
			if (currChipState == 1 || currChipState == -1) {
				return currChipState;
			}
		}

		// check the horizontal victory line
		for (int i = 0; i < gameGrid.length; i++) {
			int posX = i, posY = 3;

			int currChipState = getChipVictoryState(gameGrid[posX][posY], posX, posY);
			if (currChipState == 1 || currChipState == -1) {
				return currChipState;
			}
		}
		return 0;
	}

	public int getChipVictoryState(int chipColour, int posX, int posY) {
		victoryChips = new ArrayList<int[]>();

		// if a chip exists at the current position
		if (polarizedYellowBoard[posX][posY] || polarizedRedBoard[posX][posY]) {
			int cursorPosX = posX, cursorPosY = posY;

			// check all chips in a vertical path to the chip you are checking
			ArrayList<int[]> vertVictoryChips = new ArrayList<int[]>();
			int N_StreakYellow = 0, N_StreakRed = 0;
			while (cursorPosY < 6) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && N_StreakRed == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					vertVictoryChips.add(possVictoryChip);
					N_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && N_StreakYellow == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					vertVictoryChips.add(possVictoryChip);
					N_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosY++;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			int S_StreakYellow = 0, S_StreakRed = 0;
			while (cursorPosY > -1) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && S_StreakRed == 0) {
					if (N_StreakRed != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					vertVictoryChips.add(possVictoryChip);
					S_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && S_StreakYellow == 0) {
					if (N_StreakYellow != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					vertVictoryChips.add(possVictoryChip);
					S_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosY--;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			// add together all vertical streaks of the same colour (and then check if they are 4 or longer to check for a victory state)
			int verticalStreakYellow = N_StreakYellow + S_StreakYellow - 1, verticalStreakRed = N_StreakRed + S_StreakRed - 1;
			if (verticalStreakYellow >= 4 && chipColour == 1) {
				victoryChips.clear();
				for (int i = 0; i < vertVictoryChips.size(); i++) { 
					victoryChips.add(vertVictoryChips.get(i));
				}
				return 1;
			} 
			else if (verticalStreakRed >= 4 && chipColour == -1) {
				victoryChips.clear();
				for (int i = 0; i < vertVictoryChips.size(); i++) { 
					victoryChips.add(vertVictoryChips.get(i));
				}
				return -1;
			}

			// check all chips in a horizontal path to the chip you are checking
			ArrayList<int[]> horizVictoryChips = new ArrayList<int[]>();
			int E_StreakYellow = 0, E_StreakRed = 0;
			while (cursorPosX < 7) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && E_StreakRed == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					horizVictoryChips.add(possVictoryChip);
					E_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && E_StreakYellow == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					horizVictoryChips.add(possVictoryChip);
					E_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosX++;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			int W_StreakYellow = 0, W_StreakRed = 0;
			while (cursorPosX > -1) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && W_StreakRed == 0) {
					if (E_StreakRed != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					horizVictoryChips.add(possVictoryChip);
					W_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && W_StreakYellow == 0) {
					if (E_StreakYellow != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					horizVictoryChips.add(possVictoryChip);
					W_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosX--;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			// add together all horizontal streaks of the same colour (and then check if they are 4 or longer to check for a victory state)
			int horizontalStreakYellow = E_StreakYellow + W_StreakYellow - 1, horizontalStreakRed = E_StreakRed + W_StreakRed - 1;
			if (horizontalStreakYellow >= 4 && chipColour == 1) {
				victoryChips.clear();
				for (int i = 0; i < horizVictoryChips.size(); i++) { 
					victoryChips.add(horizVictoryChips.get(i));
				}
				return 1;
			} 
			else if (horizontalStreakRed >= 4 && chipColour == -1) {
				victoryChips.clear();
				for (int i = 0; i < horizVictoryChips.size(); i++) { 
					victoryChips.add(horizVictoryChips.get(i));
				}
				return -1;
			}

			// check all chips in a diagonal (positive slope) path to the chip you are checking
			ArrayList<int[]> diagUpVictoryChips = new ArrayList<int[]>();
			int NE_StreakYellow = 0, NE_StreakRed = 0;
			while (cursorPosX < 7 && cursorPosY < 6) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && NE_StreakRed == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagUpVictoryChips.add(possVictoryChip);
					NE_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && NE_StreakYellow == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagUpVictoryChips.add(possVictoryChip);
					NE_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosX++;
				cursorPosY++;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			int SW_StreakYellow = 0, SW_StreakRed = 0;
			while (cursorPosY > -1 && cursorPosX > -1) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && SW_StreakRed == 0) {
					if (NE_StreakRed != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagUpVictoryChips.add(possVictoryChip);
					SW_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && SW_StreakYellow == 0) {
					if (NE_StreakYellow != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagUpVictoryChips.add(possVictoryChip);
					SW_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosX--;
				cursorPosY--;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			// add together all diagonal (positive slope) streaks of the same colour (and then check if they are 4 or longer to check for a victory state)
			int diagUpStreakYellow = NE_StreakYellow + SW_StreakYellow - 1, diagUpStreakRed = NE_StreakRed + SW_StreakRed - 1;
			if (diagUpStreakYellow >= 4 && chipColour == 1) {
				victoryChips.clear();
				for (int i = 0; i < diagUpVictoryChips.size(); i++) { 
					victoryChips.add(diagUpVictoryChips.get(i));
				}
				return 1;
			} 
			else if (diagUpStreakRed >= 4 && chipColour == -1) {
				victoryChips.clear();
				for (int i = 0; i < diagUpVictoryChips.size(); i++) { 
					victoryChips.add(diagUpVictoryChips.get(i));
				}
				return -1;
			}

			// check all chips in a diagonal (negative slope) path to the chip you are checking
			ArrayList<int[]> diagDownVictoryChips = new ArrayList<int[]>();
			int NW_StreakYellow = 0, NW_StreakRed = 0;
			while (cursorPosX > -1 && cursorPosY < 6) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && NW_StreakRed == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagDownVictoryChips.add(possVictoryChip);
					NW_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && NW_StreakYellow == 0) {
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagDownVictoryChips.add(possVictoryChip);
					NW_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosX--;
				cursorPosY++;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			int SE_StreakYellow = 0, SE_StreakRed = 0;
			while (cursorPosX < 7 && cursorPosY > -1) {
				if (polarizedYellowBoard[cursorPosX][cursorPosY] && SE_StreakRed == 0) {
					if (NW_StreakRed != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagDownVictoryChips.add(possVictoryChip);
					SE_StreakYellow++;
				} 
				else if (polarizedRedBoard[cursorPosX][cursorPosY] && SE_StreakYellow == 0) {
					if (NW_StreakYellow != 0) {
						break;
					}
					int[] possVictoryChip = {cursorPosX, cursorPosY};
					diagDownVictoryChips.add(possVictoryChip);
					SE_StreakRed++;
				} 
				else {
					break;
				}
				cursorPosX++;
				cursorPosY--;
			}
			cursorPosX = posX;
			cursorPosY = posY;

			// add together all diagonal (negative slope) streaks of the same colour (and then check if they are 4 or longer to check for a victory state)
			int diagDownStreakYellow = NW_StreakYellow + SE_StreakYellow - 1, diagDownStreakRed = NW_StreakRed + SE_StreakRed - 1;
			if (diagDownStreakYellow >= 4 && chipColour == 1) {
				victoryChips.clear();
				for (int i = 0; i < diagDownVictoryChips.size(); i++) { 
					victoryChips.add(diagDownVictoryChips.get(i));
				}
				return 1;
			} 
			else if (diagDownStreakRed >= 4 && chipColour == -1) {
				victoryChips.clear();
				for (int i = 0; i < diagDownVictoryChips.size(); i++) { 
					victoryChips.add(diagDownVictoryChips.get(i));
				}
				return -1;
			}
		}
		return 0;
	}
}

// 1 player of each colour gets paired together into a match
// they then alternate turns till there is a victor and a loser or a cats game
class Match {
	Connection yPlayer;
	Connection rPlayer;
	Connect4Board cfb;
	boolean hasYellowPlayer = false, hasRedPlayer = false;
	boolean isRedFirst = false;
	final int TURN_LIMIT = 42; // 6 rows by 7 coloumns
	int turnNum = 1;

	public int getTurnNum() {
		return turnNum;
	}

	// create a match and populate attributes
	public Match(Connection player, char colour) {
		isRedFirst = Math.random() < 0.5 ? true : false;
		cfb = new Connect4Board();
		if (colour == 'r') {
			rPlayer = player;
			hasRedPlayer = true;
		} 
		else if (colour == 'y') {
			yPlayer = player;
			hasYellowPlayer = true;
		}
	}

	// gets the colour that this match needs to be one of each colour
	// r = red, y = yellow
	char getMatchAvailability() {
		if (hasRedPlayer && hasYellowPlayer) {
			return 'x';
		} 
		else if (hasRedPlayer) {
			return 'y';
		} 
		else if (hasYellowPlayer) {
			return 'r';
		} 
		else {
			return '0';
		}
	}

	// add a player to the match
	void addConnection(Connection player) {
		char availColour = this.getMatchAvailability();

		if (availColour == 'r') {
			rPlayer = player;
			hasRedPlayer = true;
		} 
		else if (availColour == 'y') {
			yPlayer = player;
			hasYellowPlayer = true;
		} 
		else {
			System.out.println("Connection no longer available...");
			return;
		}
	}

	// called to start each turn, checks if the turn limit is reached or if there is a victory
	public boolean willStartTurn() {
		return (turnNum <= TURN_LIMIT) && (getVictory() == 0) ? true : false;
	}

	// adds a turn
	public void addTurn() {
		turnNum++;
	}

	// checks board victory state
	public int getVictory() {
		return cfb.getVictoryState();
	}

	// place a chip of desired colour to the board
	public boolean place(int pos, char colour) {
		return cfb.addChip(pos, colour);
	}
}

class Connection extends Thread {
	Socket socket;
	BufferedReader bReader;
	DataOutputStream outStream;
	MTServer MMServer;
	Match connectedMatch;
	char connectionColour = 'x';// to prevent NullPointerException

	// passthrough constructor that also starts the run
	public Connection(Socket s, MTServer mms) { // constructor
		socket = s;
		MMServer = mms;
		this.start(); // Thread starts here...this start() will call run()
	}

	// the thread logic
	public void run() {
		try {
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outStream = new DataOutputStream(socket.getOutputStream());

			// If the number of threads is above the threashhold
			if (MMServer.numThreads >= MMServer.THREAD_LIMIT) {
				outStream.writeBytes("Busy!\n"); // 'busy' branch
				outStream.flush();
				socket.close();
			} 
			else { // 'available' branch (needs to .writeBytes something here)
				outStream.writeBytes("ACK!\n"); // available branch
				outStream.flush();
				MMServer.numThreads++;
				bReader.read();
				connectionColour = (char) bReader.read(); // collect player colour
				String isConnectionSimpleString = bReader.readLine(); // collect if the connection is simple
				boolean isConnectionSimple;
				if (isConnectionSimpleString.contains("t")) {
					isConnectionSimple = true;
				}
				else {
					isConnectionSimple = false;
				}

				// Check for available matches to connect to
				boolean matchMade = false;
				for (Match m : MMServer.ServerMatches) {
					if (m.getMatchAvailability() == connectionColour) {
						m.addConnection(this);
						connectedMatch = m;
						matchMade = true;
						break;
					}
				}

				// tell the client if there was a match made or not
				if (!matchMade) { // 'no match' code
					outStream.writeBytes("NOMATCH\n");
					outStream.flush();

					// gets back 'w' for "wait for a human partner" and 'b' for "bot player opponent
					// request"
					bReader.read();
					int humanOrBot = bReader.read();
					if (humanOrBot == (int) 'w') { // 'wait for partner' code
						connectedMatch = new Match(this, connectionColour);
						MMServer.ServerMatches.add(connectedMatch);

						// wait for the match that is create to get populated
						while (true) {
							socket.getRemoteSocketAddress();
							if (connectedMatch.getMatchAvailability() == 'x') {
								break;
							}
							for (Match m : MMServer.ServerMatches) {
								if (m.getMatchAvailability() == connectionColour) {
									m.addConnection(this);
									connectedMatch = m;
									matchMade = true;
									break;
								}
							}
							Thread.sleep(100);
						}
					} 
					else if (humanOrBot == (int) 'b') { // 'pair with a bot' code
						connectedMatch = new Match(this, connectionColour);
						MMServer.ServerMatches.add(connectedMatch);

						// make a new thread containing the bot object
						if (connectionColour == 'y') {
							MMServer.makeBot('r');
						} 
						else {
							MMServer.makeBot('y');
						}

						while (true) {
							socket.getRemoteSocketAddress();
							if (connectedMatch.getMatchAvailability() == 'x') {
								break;
							}
							for (Match m : MMServer.ServerMatches) {
								if (m.getMatchAvailability() == connectionColour) {
									m.addConnection(this);
									connectedMatch = m;
									matchMade = true;
									break;
								}
							}
							Thread.sleep(100);
						}
					}
				} else { // 'matched' code
					outStream.writeBytes("MATCH\n");
					outStream.flush();
				}

				// print the connected players socket to prove the handshake
				if (connectionColour == 'r') {
					outStream.writeBytes(connectedMatch.yPlayer.socket.getRemoteSocketAddress().toString() + "\n");
				} 
				else {
					outStream.writeBytes(connectedMatch.rPlayer.socket.getRemoteSocketAddress().toString() + "\n");
				}
				outStream.flush();

				bReader.readLine();

				// checks if the turn limit is reached or if there is a victory
				while (connectedMatch.willStartTurn()) {

					// warn each connection that the turn is starting
					outStream.writeBytes("START_TURN\n");
					outStream.flush();

					// tell each player if they have to wait their turn or take their turn
					int currTurnNum = connectedMatch.getTurnNum();
					if (currTurnNum % 2 == 0) {
						if ((connectedMatch.isRedFirst && connectionColour == 'r') || (!connectedMatch.isRedFirst && connectionColour == 'y')) {
							outStream.writeBytes(currTurnNum + "\n");
							outStream.flush();
							outStream.writeUTF(connectedMatch.cfb.visualiseChips(isConnectionSimple) + "\n"); // print gameboard to client
						} 
						else {
							outStream.writeBytes("wait\n");
						}
					} else {
						if (currTurnNum % 2 == 1) {
							if ((connectedMatch.isRedFirst && connectionColour == 'r') || (!connectedMatch.isRedFirst && connectionColour == 'y')) {
								outStream.writeBytes("wait\n");
							} 
							else {
								outStream.writeBytes(currTurnNum + "\n");
								outStream.flush();
								outStream.writeUTF(connectedMatch.cfb.visualiseChips(isConnectionSimple) + "\n"); // print gameboard to client
							}
						}
					}

					outStream.flush();

					// read the move location
					String moveLocation = bReader.readLine();
					if (moveLocation != null) {
						if (moveLocation.equalsIgnoreCase("-1")) {
							while (true) {
								if (currTurnNum != connectedMatch.getTurnNum()) {
									break;
								}
								Thread.sleep(50);
							}
							outStream.writeBytes("end\n");
						} else {
							if (connectedMatch.place(Integer.parseInt(moveLocation) - 1, connectionColour)) {
								connectedMatch.addTurn();
							} 
							else {
							}
							outStream.writeBytes("end\n");
						}
						outStream.flush();
					}
				}
				// print that the game is over to the client
				outStream.writeBytes("VICTORY\n");

				// print the victor to the client 
				outStream.writeUTF(connectedMatch.cfb.visualiseChips(isConnectionSimple) + "\n");

				// tell the client who the victor was
				outStream.writeBytes(connectedMatch.getVictory() + "\n");

				// remove the thread and the match
				MMServer.ServerMatches.remove(connectedMatch);
				MMServer.numThreads--;
			}
		} catch (Exception e) {
			if (MMServer.isVerboseServer) {
				e.printStackTrace();
			}
			try {
				socket.close();
			} catch (Exception ie) {
			}
		}
	}
}

// AI client has the same reads and writes as the regular client
class ConnectFourAIClient extends Thread {
	public char playerColour;

	public ConnectFourAIClient(char colour) {
		playerColour = colour;
		this.start();
	}

	public int makeMove() {
		return (int) (Math.random() * 6) + 1;
	}

	public void run() {
		final int SERVER_PORT_NUMBER = 3500;
		Socket socket;
		BufferedReader bReader;
		DataOutputStream outStream;

		// Which colour do you with to play? ([R]ed / [Y]ellow)
		String redOrYellow = playerColour + "";
		if (redOrYellow.equals("r") || redOrYellow.equals("y")) {
			playerColour = redOrYellow.charAt(0);
		} 
		else if (redOrYellow.equals("red")) {
			playerColour = 'r';
		} 
		else if (redOrYellow.equals("yellow")) {
			playerColour = 'y';
		} 
		else {
			// AI BOT BEING USED WRONG
			System.err.println("Invalid usage on ConnectFourAIClient.java");
		}

		// try to setup socket after player colour is chosen
		try {
			socket = new Socket("localhost", SERVER_PORT_NUMBER);
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outStream = new DataOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			return;
		}

		// thread number checking logic (client side)
		try {
			if (bReader.readLine().equals("Busy!")) { // (this readline binds if there is no server message)
				socket.close();
			} 
			else { // 'available' branch
				outStream.writeChar(playerColour); // Hand the player colour off to the server
				outStream.flush();
				outStream.writeChars("NOTSIMPLE\n");
                outStream.flush();

				if (bReader.readLine().equalsIgnoreCase("nomatch")) { // no open matches
					System.out.println("Bot has no partner to the dance.");
				}

				System.out.println("Bot: now connected to " + bReader.readLine());

				outStream.writeBytes("start\n");

				while (true) {
					if (bReader.readLine().equalsIgnoreCase("start_turn")) {
						if (bReader.readLine().equals("wait")) { // opponent's turn
							outStream.writeBytes("-1\n");
						} 
						else { 
							bReader.read(); // read the writeUTF header
							bReader.read(); // same deal down here
							/*String gameRender = */bReader.readLine();

							int move = this.makeMove();
							outStream.writeBytes(move + "\n");
						}
						outStream.flush();

						if (bReader.readLine().equalsIgnoreCase("end")) {}
					} else {
						bReader.read(); // read the writeUTF header
						bReader.read(); // same deal down here
						/*String gameRender = */bReader.readLine();

						/*int gameWinner = */Integer.parseInt(bReader.readLine());
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Busy!");
		}
		try {
			socket.close();
		} catch (IOException e) {
		}
    }
}
